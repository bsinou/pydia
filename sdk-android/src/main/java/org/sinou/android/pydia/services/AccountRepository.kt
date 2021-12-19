package org.sinou.android.pydia.services

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.pydio.cells.api.*
import com.pydio.cells.transport.CellsTransport
import com.pydio.cells.transport.StateID
import com.pydio.cells.transport.auth.Token
import com.pydio.cells.transport.auth.credentials.JWTCredentials
import com.pydio.cells.transport.auth.jwt.IdToken
import com.pydio.cells.utils.MemoryStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.room.account.Account
import org.sinou.android.pydia.room.account.AccountDatabase
import org.sinou.android.pydia.utils.AndroidCustomEncoder

class AccountRepository(private val accountDB: AccountDatabase, private val workingDir: String?) {

    private val encoder: CustomEncoder = AndroidCustomEncoder()

    private val TAG = "AccountRepository"

    /* Stores and Factory */
    private val servers = MemoryStore<Server>()
    private val transports = MemoryStore<Transport>()
    lateinit var sessionFactory: SessionFactory

    // Temporary keep track of all states that have been generated for OAuth processes
    val inProcessCallbacks = mutableMapOf<String, ServerURL>()

    init {
        sessionFactory = SessionFactory(accountDB, servers, transports)
    }

    fun registerAccount(serverURL: ServerURL, credentials: Credentials): String? {
        try {

            val state = StateID(credentials.username, serverURL.toString());
            val existingAccount =
                accountDB.accountDao().getAccount(credentials.username, serverURL.toString())

            sessionFactory.registerAccountCredentials(serverURL, credentials)
            val server: Server = sessionFactory.getServer(serverURL.id)
            val account = toAccount(credentials.username, server)

            if (existingAccount == null) { // creation
                accountDB.accountDao().insert(account)
            } else { // update
                accountDB.accountDao().update(account)
            }

            return state.id

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun toAccount(username: String, server: Server): Account {
        return Account(
            username = username,
            url = server.url(),
            serverLabel = server.label,
            skipVerify = server.serverURL.skipVerify(),
            isLegacy = server.isLegacy,
            welcomeMessage = server.getWelcomeMessage(),
            // TODO
            // isActive =
        )
    }

    suspend fun handleOAuthResponse(oauthState: String, code: String) {
        withContext(Dispatchers.IO) {
            val serverURL = inProcessCallbacks.get(oauthState)
            if (serverURL != null) {
                try {
                    val transport =
                        sessionFactory.getAnonymousTransport(serverURL.getId()) as CellsTransport
                    val token = transport.getTokenFromCode(code, encoder)
                    manageRetrievedToken(transport, oauthState, token)
                } catch (e: Exception) {
                    Log.e(TAG, "Could not finalize credential auth flow")
                    e.printStackTrace()
                }
            } else {                // Ignore callback
                Log.i(TAG, "Ignored call back with unknown state: ${oauthState}")
            }

        }
    }

    private fun manageRetrievedToken(transport: CellsTransport, oauthState: String, token: Token) {
        val idToken = IdToken.parse(encoder, token.idToken)
        val accountID = StateID(idToken.claims.name, transport.server.url())
        val jwt = JWTCredentials(accountID.username, token)

        // TODO: here also launch a refresh workspace task and wait
        registerAccount(transport.server.serverURL, jwt)

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
    }


    val accounts: LiveData<List<Account>> = accountDB.accountDao().getAllAccounts()

    @WorkerThread
    suspend fun insert(account: Account) {
        accountDB.accountDao().insert(account)
    }

    @WorkerThread
    suspend fun update(account: Account) {
        accountDB.accountDao().update(account)
    }
}
