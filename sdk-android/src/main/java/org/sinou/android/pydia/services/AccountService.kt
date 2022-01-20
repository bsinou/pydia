package org.sinou.android.pydia.services

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.pydio.cells.api.*
import com.pydio.cells.api.ui.Node
import com.pydio.cells.api.ui.WorkspaceNode
import com.pydio.cells.transport.CellsTransport
import com.pydio.cells.transport.StateID
import com.pydio.cells.transport.auth.Token
import com.pydio.cells.transport.auth.credentials.JWTCredentials
import com.pydio.cells.transport.auth.jwt.IdToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.room.account.AccountDB
import org.sinou.android.pydia.room.account.RAccount
import org.sinou.android.pydia.room.account.RLiveSession
import org.sinou.android.pydia.room.account.RSession
import org.sinou.android.pydia.utils.AndroidCustomEncoder
import org.sinou.android.pydia.utils.hasAtLeastMeteredNetwork
import java.io.File

/**
 * AccountService is the single source of truth for accounts, sessions and auth in the app.
 * It takes care of both local caching of session info and authentication against the remote
 * servers. It holds a SessionFactory that gives access to the SDK client
 */
class AccountService(val accountDB: AccountDB, private val baseDir: File) {

    private val encoder: CustomEncoder = AndroidCustomEncoder()

    companion object {
        private const val TAG = "AccountService"

        const val LIFECYCLE_STATE_FOREGROUND = "foreground"
        const val LIFECYCLE_STATE_BACKGROUND = "background"
        const val LIFECYCLE_STATE_PAUSED = "paused"
    }

    // Local stores to cache live objects
//    private val servers = MemoryStore<Server>()
//    private val transports = MemoryStore<Transport>()

    // Expose the session factory for clients to retrieve clients
    val sessionFactory: SessionFactory = SessionFactory.getSessionFactory(accountDB)

    // Temporary keep track of all states that have been generated for OAuth processes
    val inProcessCallbacks = mutableMapOf<String, ServerURL>()


    val activeSession: LiveData<RLiveSession?> = accountDB
        .liveSessionDao().getLiveActiveSession(LIFECYCLE_STATE_FOREGROUND)

    val liveSessions: LiveData<List<RLiveSession>> = accountDB.liveSessionDao().getLiveSessions()

    fun registerAccount(serverURL: ServerURL, credentials: Credentials): String? {
        try {
            val state = StateID(credentials.username, serverURL.id)
            val existingAccount = accountDB.accountDao().getAccount(state.accountId)

            sessionFactory.registerAccountCredentials(serverURL, credentials)
            val server: Server = sessionFactory.getServer(serverURL.id)
            val account = toAccount(credentials.username, server)

            // At this point we assume we have been connected or an error has already been thrown
            account.authStatus = AppNames.AUTH_STATUS_CONNECTED

            if (existingAccount == null) { // creation
                accountDB.accountDao().insert(account)
            } else { // update
                accountDB.accountDao().update(account)
            }
            registerLocalSession(StateID(account.username, account.url).id)

            return state.id

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun forgetAccount(accountID: String) = withContext(Dispatchers.IO) {
        try {
            // First retrieve the account to forget to know if it is legacy or not
            accountDB.accountDao().getAccount(accountID)?.let {
                if (it.isLegacy) {
                    accountDB.legacyCredentialsDao().forgetPassword(accountID)
                } else {
                    accountDB.tokenDao().forgetToken(accountID)
                }
                accountDB.sessionDao().forgetSession(accountID)
                accountDB.accountDao().forgetAccount(accountID)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun logoutAccount(accountID: String) = withContext(Dispatchers.IO) {
        try {
            // First retrieve the account to forget to know if it is legacy or not
            accountDB.accountDao().getAccount(accountID)?.let {
                if (it.isLegacy) {
                    accountDB.legacyCredentialsDao().forgetPassword(accountID)
                } else {
                    accountDB.tokenDao().forgetToken(accountID)
                }
                it.authStatus = AppNames.AUTH_STATUS_NO_CREDS
                accountDB.accountDao().update(it)

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun isClientConnected(accountID: String): Boolean = withContext(Dispatchers.IO) {
        var isConnected = hasAtLeastMeteredNetwork(CellsApp.instance.applicationContext)
        accountDB.accountDao().getAccount(accountID)?.let {
            return@withContext isConnected && it.authStatus == AppNames.AUTH_STATUS_CONNECTED
        }
        return@withContext false
    }


    /** Stores a new row in the Session DB */
    fun registerLocalSession(accountID: String) {
        var newSession = accountDB.sessionDao().getSession(accountID)
        // if the session already exists, do nothing
        if (newSession == null) {
            newSession = fromAccountID(accountID)
            accountDB.sessionDao().insert(newSession)
        }
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
                currSession.lifecycleState = LIFECYCLE_STATE_BACKGROUND
                accountDB.sessionDao().update(currSession)
            }

            var openSession = accountDB.sessionDao().getSession(accountID)

            if (openSession == null) {
                openSession = fromAccountID(accountID)
                accountDB.sessionDao().insert(openSession)
            } else {
                openSession.lifecycleState = LIFECYCLE_STATE_FOREGROUND
                accountDB.sessionDao().update(openSession)
            }
        }
    }

    suspend fun refreshWorkspaceList(accountID: String): String? = withContext(Dispatchers.IO) {
        try {
            val workspaces = mutableListOf<WorkspaceNode>()
            val client: Client = sessionFactory.getUnlockedClient(accountID)
            client.workspaceList { node: Node? ->
                if (node is WorkspaceNode) {
                    workspaces.add(node)
                }
            }

            // Update WS cache in session row
            val session = accountDB.sessionDao().getSession(accountID)
            if (session != null) {
                session.workspaces = workspaces.sorted()
                accountDB.sessionDao().update(session)
            }
        } catch (e: SDKException) {
            Log.e(TAG, "could not perform ls for " + accountID)
            e.printStackTrace()
            return@withContext "Cannot connect to distant server"
        }
        return@withContext null
    }

    /** Cells' Credentials flow management */

    suspend fun handleOAuthResponse(oauthState: String, code: String): String? =
        withContext(Dispatchers.IO) {
            var accountID: String? = null
            val serverURL = inProcessCallbacks.get(oauthState)
            if (serverURL == null) {
                Log.i(TAG, "Ignored call back with unknown state: ${oauthState}")
                return@withContext accountID
            }
            try {
                val transport =
                    sessionFactory.getAnonymousTransport(serverURL.id) as CellsTransport
                val token = transport.getTokenFromCode(code, encoder)
                accountID = manageRetrievedToken(transport, token)
            } catch (e: Exception) {
                Log.e(TAG, "Could not finalize credential auth flow")
                e.printStackTrace()
            }
            return@withContext accountID
        }

    private fun manageRetrievedToken(transport: CellsTransport, token: Token): String {
        val idToken = IdToken.parse(encoder, token.idToken)
        val accountID = StateID(idToken.claims.name, transport.server.url())
        val jwtCredentials = JWTCredentials(accountID.username, token)

        registerAccount(transport.server.serverURL, jwtCredentials)

        // TODO: also launch:
        //   - workspace refresh
        //   - offline check and update (in case of configuration change)

        // This will directly try to use the newly registered session to get a Client
//        val client: Client = sf.getUnlockedClient(accountID.id)
//        val workspaces: MutableMap<String, WorkspaceNode> = HashMap()
//        client.workspaceList { ws: Node ->
//            workspaces[(ws as WorkspaceNode).slug] = ws
//        }
//        val account: AccountRecord = sf.getSession(accountID.id).getAccount()
//        account.setWorkspaces(workspaces)
//        App.getAccountService().updateAccount(account)
//        App.getSessionFactory().loadKnownAccounts()


        // TODO ?
        // Set the session as current in the app
        // Adapt poll and tasks
        // check if it was a background thread
        // redirectToCallerWithNewState(State.fromAccountId(accountID.id), oauthState)
        return accountID.id
    }


    // TODO there is more idiomatic way to do, see "behind the scene" codelab
    fun toAccount(username: String, server: Server): RAccount {
        return RAccount(
            accountID = StateID(username, server.url()).accountId,
            username = username,
            url = server.url(),
            serverLabel = server.label,
            tlsMode = if (server.serverURL.skipVerify()) 1 else 0,
            isLegacy = server.isLegacy,
            welcomeMessage = server.welcomeMessage,
            authStatus = AppNames.AUTH_STATUS_NEW,
        )
    }

    private fun fromAccountID(accountID: String): RSession {
        val newSession = RSession(
            accountID = accountID,
            baseDir = "/tmp",
            lifecycleState = "foreground",
            workspaces = listOf(),
            offlineRoots = listOf(),
            bookmarkCache = listOf(),
            shareCache = listOf(),
        )
        return newSession
    }

//    public void createLocalFolders(String accountID) throws SDKException {
//
//        String baseFolderPath = baseLocalFolderPath(accountID);
//        File file = new File(baseFolderPath);
//        if (!file.exists() && !file.mkdirs()) {
//            throw new SDKException("could not create session base directory");
//        }
//
//        String cacheFolderPath = cacheLocalFolderPath(accountID);
//        file = new File(cacheFolderPath);
//        if (!file.exists() && !file.mkdirs()) {
//            throw new SDKException("could not create cache directory");
//        }
//
//        String tempFolderPath = tempLocalFolderPath(accountID);
//        file = new File(tempFolderPath);
//        if (!file.exists() && !file.mkdirs()) {
//            throw new SDKException("could not create temp directory");
//        }
//    }

    @WorkerThread
    suspend fun insert(account: RAccount) {
        accountDB.accountDao().insert(account)
    }

    @WorkerThread
    suspend fun update(account: RAccount) {
        accountDB.accountDao().update(account)
    }
}
