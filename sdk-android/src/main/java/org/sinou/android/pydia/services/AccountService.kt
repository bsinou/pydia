package org.sinou.android.pydia.services

import android.util.Log
import androidx.lifecycle.LiveData
import com.pydio.cells.api.*
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.db.accounts.*
import org.sinou.android.pydia.transfer.WorkspaceDiff
import org.sinou.android.pydia.utils.hasAtLeastMeteredNetwork
import org.sinou.android.pydia.utils.logException
import java.io.File
import java.net.HttpURLConnection

/**
 * AccountService is the single source of truth for accounts, sessions and auth in the app.
 * It takes care of both local caching of session info and authentication against the remote
 * servers. It holds a SessionFactory that gives access to the SDK client
 */
class AccountService(val accountDB: AccountDB, private val baseDir: File) {

    private val tag = AccountService::class.java.simpleName

    // Holds a map to find DB and files for a given account
    private val _sessions = mutableMapOf<String, RSession>()
    val sessions: Map<String, RSession>
        get() = _sessions

    init {
        refreshSessionCache()
    }

    private fun refreshSessionCache() {
        val sessions = accountDB.sessionDao().getSessions()
        _sessions.clear()
        for (rSession in sessions) {
            _sessions[rSession.accountID] =  rSession
        }
    }

    // Expose the session factory to retrieve unlocked clients
    val authService = AuthService.getAuthService(accountDB)
    val sessionFactory: SessionFactory = SessionFactory.getSessionFactory(authService.credentialService, accountDB.liveSessionDao())

    fun getClient(stateId: StateID): Client {
        return sessionFactory.getUnlockedClient(stateId.accountId)
    }

    fun getLiveSession(accountID: String): LiveData<RLiveSession?> = accountDB
        .liveSessionDao().getLiveSession(accountID)

    fun getLiveWorkspaces(accountID: String): LiveData<List<RWorkspace>> = accountDB
        .workspaceDao().getLiveWorkspaces(accountID)

    val activeSessionLive: LiveData<RLiveSession?> = accountDB
        .liveSessionDao().getLiveActiveSession(AppNames.LIFECYCLE_STATE_FOREGROUND)

    val liveSessions: LiveData<List<RLiveSession>> = accountDB.liveSessionDao().getLiveSessions()

    @Throws(SDKException::class)
    fun registerAccount(serverURL: ServerURL, credentials: Credentials): String {

        val state = StateID(credentials.username, serverURL.id)
        val existingAccount = accountDB.accountDao().getAccount(state.accountId)

        sessionFactory.registerAccountCredentials(serverURL, credentials)
        val server: Server = sessionFactory.getServer(serverURL.id)
        val account = toRAccount(credentials.username, server)

        // At this point we assume we have been connected or an error has already been thrown
        account.authStatus = AppNames.AUTH_STATUS_CONNECTED

        if (existingAccount == null) { // creation
            accountDB.accountDao().insert(account)
            safelyCreateSession(account)
        } else { // update
            accountDB.accountDao().update(account)
        }

        return state.id
    }

    fun listLiveSessions(includeLegacy: Boolean): List<RLiveSession>{
        val dao = accountDB.liveSessionDao()
            return if (includeLegacy){
            dao.getSessions()
        } else {
            dao.getCellsSessions()
        }
    }

    private fun safelyCreateSession(account: RAccount) {
        // We only update the dir and db names at account creation
        // FIXME finalize this
        var session = RSession.newInstance(account, 0)
        val sessionWithSameName = accountDB.sessionDao().getWithDirName(session.dirName)
        if (sessionWithSameName.isNotEmpty()) {
            session = RSession.newInstance(account, sessionWithSameName.size)
        }
        accountDB.sessionDao().insert(session)
        refreshSessionCache()

        CellsApp.instance.fileService.prepareTree(StateID.fromId(account.accountID))
    }

    suspend fun forgetAccount(accountID: String): String? = withContext(Dispatchers.IO) {
        val stateID = StateID.fromId(accountID)
        try {
            val oldAccount = accountDB.accountDao().getAccount(accountID)
                ?: return@withContext null // nothing to forget

            // Credentials
            if (oldAccount.isLegacy) {
                accountDB.legacyCredentialsDao().forgetPassword(accountID)
            } else {
                accountDB.tokenDao().deleteToken(accountID)
            }

            // Files
            CellsApp.instance.fileService.cleanAllLocalFiles(stateID)
            // The account TreeNode DB
            CellsApp.instance.nodeService.clearIndexFor(stateID)
            // Remove rows in the account tables
            accountDB.sessionDao().forgetSession(accountID)
            accountDB.workspaceDao().forgetAccount(accountID)
            accountDB.accountDao().forgetAccount(accountID)

            // Update local caches
            refreshSessionCache()

            return@withContext null
        } catch (e: Exception) {
            val msg = "Could not delete account ${StateID.fromId(accountID)}"
            logException(tag, msg, e)
            return@withContext msg
        }
    }

    suspend fun logoutAccount(accountID: String): String? = withContext(Dispatchers.IO) {
        try {
            accountDB.accountDao().getAccount(accountID)?.let {
                Log.i(tag, "About to logout $accountID")
                Log.i(tag, "Calling stack:")
                Thread.dumpStack()
                // There is also a token that is generated for P8:
                // In case of legacy server, we have to discard a row in **both** tables
                if (it.isLegacy) {
                    accountDB.legacyCredentialsDao().forgetPassword(accountID)
                }
                accountDB.tokenDao().deleteToken(accountID)
                it.authStatus = AppNames.AUTH_STATUS_NO_CREDS
                accountDB.accountDao().update(it)
                return@withContext null
            }
        } catch (e: Exception) {
            val msg = "Could not delete credentials for ${StateID.fromId(accountID)}"
            logException(tag, msg, e)
            return@withContext msg
        }
    }

    suspend fun notifyError(stateID: StateID, code: Int) = withContext(Dispatchers.IO) {
        try {
            accountDB.accountDao().getAccount(stateID.accountId)?.let {
                when (code) {
                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                        if (it.authStatus == AppNames.AUTH_STATUS_CONNECTED){
                            it.authStatus = AppNames.AUTH_STATUS_UNAUTHORIZED
                            accountDB.accountDao().update(it)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            val msg = "Could not update account for $stateID after error $code"
            logException(tag, msg, e)
        }
    }

    suspend fun isClientConnected(stateID: String): Boolean = withContext(Dispatchers.IO) {
        var isConnected = hasAtLeastMeteredNetwork(CellsApp.instance.applicationContext)
        val accountID = StateID.fromId(stateID).accountId
        accountDB.accountDao().getAccount(accountID)?.let {
            return@withContext isConnected && it.authStatus == AppNames.AUTH_STATUS_CONNECTED
        }
        return@withContext false
    }

    /**
     * Sets the lifecycle_state of a given session to "foreground".
     * WARNING: no check is done on the passed accountID.
     */
    suspend fun openSession(accountID: String) {
        return withContext(Dispatchers.IO) {

            // First put other opened sessions in the background
            val tmpSessions = accountDB.sessionDao().foregroundSessions()
            for (currSession in tmpSessions) {
                currSession.lifecycleState = AppNames.LIFECYCLE_STATE_BACKGROUND
                accountDB.sessionDao().update(currSession)
            }

            var openSession = accountDB.sessionDao().getSession(accountID)

            if (openSession == null) {
                // should never happen
                Log.e(tag, "No session found for $accountID")
//                openSession = fromAccountID(accountID)
//                accountDB.sessionDao().insert(openSession)
            } else {
                openSession.lifecycleState = AppNames.LIFECYCLE_STATE_FOREGROUND
                accountDB.sessionDao().update(openSession)
            }
        }
    }

    suspend fun refreshWorkspaceList(accountIDStr: String): String? = withContext(Dispatchers.IO) {
        val accountID = StateID.fromId(accountIDStr)
        try {
            val client: Client = sessionFactory.getUnlockedClient(accountIDStr)
            val accService = CellsApp.instance.accountService
            val nodeService = CellsApp.instance.nodeService
            val fileService = CellsApp.instance.fileService

            val wsDiff = WorkspaceDiff(accountID, client, accService, nodeService, fileService)
            wsDiff.compareWithRemote()

//            // We assume the list of workspaces is small enough to be first loaded in memory
//            val workspaces = mutableListOf<WorkspaceNode>()
//            client.workspaceList { node: Node? ->
//                if (node is WorkspaceNode) {
//                    workspaces.add(node)
//                }
//            }
//
//            // TODO also handle deletion
//            for (node in workspaces) {
//                var rw = RWorkspace.createChild(parentID, node)
//                var old = accountDB.workspaceDao().getWorkspace(rw.encodedState)
//                if (old == null) {
//                    accountDB.workspaceDao().insert(rw)
//                    CellsApp.instance.nodeService.
//                } else {
//                    accountDB.workspaceDao().update(rw)
//                }
//            }

        } catch (e: SDKException) {
            Log.e(tag, "could not get workspace list for $accountID")
            e.printStackTrace()
            return@withContext "cannot connect to distant server"
        }
        return@withContext null
    }

}
