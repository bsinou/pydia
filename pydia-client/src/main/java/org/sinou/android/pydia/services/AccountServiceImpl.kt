package org.sinou.android.pydia.services

import android.util.Log
import androidx.lifecycle.LiveData
import com.pydio.cells.api.Client
import com.pydio.cells.api.Credentials
import com.pydio.cells.api.ErrorCodes
import com.pydio.cells.api.SDKException
import com.pydio.cells.api.Server
import com.pydio.cells.api.ServerURL
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.db.accounts.AccountDB
import org.sinou.android.pydia.db.accounts.AccountDao
import org.sinou.android.pydia.db.accounts.LegacyCredentialsDao
import org.sinou.android.pydia.db.accounts.LiveSessionDao
import org.sinou.android.pydia.db.accounts.RAccount
import org.sinou.android.pydia.db.accounts.RLiveSession
import org.sinou.android.pydia.db.accounts.RSession
import org.sinou.android.pydia.db.accounts.RWorkspace
import org.sinou.android.pydia.db.accounts.SessionDao
import org.sinou.android.pydia.db.accounts.TokenDao
import org.sinou.android.pydia.db.accounts.WorkspaceDao
import org.sinou.android.pydia.db.accounts.toRAccount
import org.sinou.android.pydia.transfer.WorkspaceDiff
import org.sinou.android.pydia.utils.hasAtLeastMeteredNetwork
import org.sinou.android.pydia.utils.logException
import java.net.HttpURLConnection

/**
 * AccountService is the single source of truth for accounts, sessions and auth in the app.
 * It takes care of both local caching of session info and authentication against the remote
 * servers.
 */
class AccountServiceImpl(
    accountDB: AccountDB,
    private val sessionFactory: SessionFactory,
    private val treeNodeRepository: TreeNodeRepository
) : AccountService, KoinComponent {

    private val logTag = AccountService::class.java.simpleName

    private val accountDao: AccountDao = accountDB.accountDao()
    private val sessionDao: SessionDao = accountDB.sessionDao()
    private val liveSessionDao: LiveSessionDao = accountDB.liveSessionDao()
    private val workspaceDao: WorkspaceDao = accountDB.workspaceDao()
    private val tokenDao: TokenDao = accountDB.tokenDao()
    private val legacyCredentialsDao: LegacyCredentialsDao = accountDB.legacyCredentialsDao()

    override fun getClient(stateId: StateID): Client {
        return sessionFactory.getUnlockedClient(stateId.accountId)
    }

    override fun getLiveSession(accountID: String): LiveData<RLiveSession?> =
        liveSessionDao.getLiveSession(accountID)

    override fun getLiveWorkspaces(accountID: String): LiveData<List<RWorkspace>> =
        workspaceDao.getLiveWorkspaces(accountID)

    override val activeSessionLive: LiveData<RLiveSession?> =
        liveSessionDao.getLiveActiveSession(AppNames.LIFECYCLE_STATE_FOREGROUND)

    override val liveSessions: LiveData<List<RLiveSession>> = liveSessionDao.getLiveSessions()

    @Throws(SDKException::class)
    override suspend fun registerAccount(serverURL: ServerURL, credentials: Credentials): String {

        val state = StateID(credentials.username, serverURL.id)
        val existingAccount = accountDao.getAccount(state.accountId)

        sessionFactory.registerAccountCredentials(serverURL, credentials)
        val server: Server = sessionFactory.getServer(serverURL.id)
        val account = toRAccount(credentials.username, server)

        // At this point we assume we have been connected or an error has already been thrown
        account.authStatus = AppNames.AUTH_STATUS_CONNECTED

        if (existingAccount == null) { // creation
            accountDao.insert(account)
            safelyCreateSession(account)
        } else { // update
            accountDao.update(account)
        }

        return state.id
    }

    override fun listLiveSessions(includeLegacy: Boolean): List<RLiveSession> {
        return if (includeLegacy) {
            liveSessionDao.getSessions()
        } else {
            liveSessionDao.getCellsSessions()
        }
    }

    private suspend fun safelyCreateSession(account: RAccount) {
        // We only update the dir and db names at account creation
        // TODO add tests.
        var session = RSession.newInstance(account, 0)
        val sessionWithSameName = sessionDao.getWithDirName(session.dirName)
        if (sessionWithSameName.isNotEmpty()) {
            session = RSession.newInstance(account, sessionWithSameName.size)
        }
        sessionDao.insert(session)
        treeNodeRepository.refreshSessionCache()

        val fileService: FileService = get()
        fileService.prepareTree(StateID.fromId(account.accountID))
    }

    override suspend fun forgetAccount(accountId: String): String? = withContext(Dispatchers.IO) {
        val stateId = StateID.fromId(accountId)
        Log.d(logTag, "### About to forget $stateId")
        try {
            val oldAccount = accountDao.getAccount(accountId)
                ?: return@withContext null // nothing to forget

            // Credentials
            if (oldAccount.isLegacy) {
                legacyCredentialsDao.forgetPassword(accountId)
            } else {
                tokenDao.deleteToken(accountId)
            }

            val fileService: FileService = get()
            fileService.cleanAllLocalFiles(stateId)

            // Remove rows in the account tables
            sessionDao.forgetSession(accountId)
            workspaceDao.forgetAccount(accountId)
            accountDao.forgetAccount(accountId)

            val treeNodeRepository: TreeNodeRepository = get()
            treeNodeRepository.closeNodeDb(stateId.accountId)


            // Update local caches
            treeNodeRepository.refreshSessionCache()

            Log.i(logTag, "### $stateId has been forgotten")
            return@withContext null
        } catch (e: Exception) {
            val msg = "Could not delete account ${StateID.fromId(accountId)}"
            logException(logTag, msg, e)
            return@withContext msg
        }
    }

    override suspend fun logoutAccount(accountID: String): String? = withContext(Dispatchers.IO) {
        try {
            accountDao.getAccount(accountID)?.let {
                Log.i(logTag, "About to logout $accountID")
                Log.i(logTag, "Calling stack:")
                Thread.dumpStack()
                // There is also a token that is generated for P8:
                // In case of legacy server, we have to discard a row in **both** tables
                if (it.isLegacy) {
                    legacyCredentialsDao.forgetPassword(accountID)
                }
                tokenDao.deleteToken(accountID)
                it.authStatus = AppNames.AUTH_STATUS_NO_CREDS
                accountDao.update(it)
                return@withContext null
            }
        } catch (e: Exception) {
            val msg = "Could not delete credentials for ${StateID.fromId(accountID)}"
            logException(logTag, msg, e)
            return@withContext msg
        }
    }

    /**
     * Sets the lifecycle_state of a given session to "foreground".
     * WARNING: no check is done on the passed accountID.
     */
    override suspend fun openSession(accountID: String) {
        return withContext(Dispatchers.IO) {

            // First put other opened sessions in the background
            val tmpSessions = sessionDao.foregroundSessions()
            for (currSession in tmpSessions) {
                currSession.lifecycleState = AppNames.LIFECYCLE_STATE_BACKGROUND
                sessionDao.update(currSession)
            }

            val openSession = sessionDao.getSession(accountID)

            if (openSession == null) {
                // should never happen
                Log.e(logTag, "No session found for $accountID")
//                openSession = fromAccountID(accountID)
//                accountDB.sessionDao().insert(openSession)
            } else {
                openSession.lifecycleState = AppNames.LIFECYCLE_STATE_FOREGROUND
                sessionDao.update(openSession)
            }
        }
    }

    override suspend fun isClientConnected(stateID: String): Boolean = withContext(Dispatchers.IO) {
        val isConnected = hasAtLeastMeteredNetwork(CellsApp.instance.applicationContext)
        val accountID = StateID.fromId(stateID).accountId
        accountDao.getAccount(accountID)?.let {
            return@withContext isConnected && it.authStatus == AppNames.AUTH_STATUS_CONNECTED
        }
        return@withContext false
    }

    override suspend fun refreshWorkspaceList(accountIDStr: String): Pair<Int, String?> =
        withContext(Dispatchers.IO) {
            var result: Pair<Int, String?>

            val accountID = StateID.fromId(accountIDStr)
            try {
                val client: Client = getClient(StateID.fromId(accountIDStr))
                val wsDiff = WorkspaceDiff(accountID, client)
                val changeNb = wsDiff.compareWithRemote()
                result = Pair(changeNb, null)
            } catch (e: SDKException) {
                val msg = "could not get workspace list for $accountID"
                Log.e(logTag, msg)
                e.printStackTrace()
                notifyError(accountID, e.code)
                return@withContext Pair(0, msg)
            }
            return@withContext result

        }

    override suspend fun notifyError(stateID: StateID, code: Int) = withContext(Dispatchers.IO) {
        Log.i(logTag, "Received error $code for $stateID")
        try {
            accountDao.getAccount(stateID.accountId)?.let {
                when (code) {
                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                        if (it.authStatus == AppNames.AUTH_STATUS_CONNECTED) {
                            it.authStatus = AppNames.AUTH_STATUS_UNAUTHORIZED
                            accountDao.update(it)
                        }
                    }
                    ErrorCodes.no_token_available -> {
                        if (it.authStatus == AppNames.AUTH_STATUS_CONNECTED) {
                            it.authStatus = AppNames.AUTH_STATUS_NO_CREDS
                            accountDao.update(it)
                        }
                    }
                    // TODO unreachable host: pause session
//                    ErrorCodes.unreachable_host -> {
//                        if (it.authStatus == AppNames.AUTH_STATUS_CONNECTED) {
//                            it.authStatus = AppNames.AUTH_STATUS_NO_CREDS
//                            accountDao.update(it)
//                        }
//                    }
                }
            }
        } catch (e: Exception) {
            val msg = "Could not update account for $stateID after error $code"
            logException(logTag, msg, e)
        }
    }

}
