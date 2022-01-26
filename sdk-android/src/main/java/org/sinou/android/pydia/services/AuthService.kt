package org.sinou.android.pydia.services

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.pydio.cells.api.CustomEncoder
import com.pydio.cells.api.ServerURL
import com.pydio.cells.transport.CellsTransport
import com.pydio.cells.transport.ClientData
import com.pydio.cells.transport.StateID
import com.pydio.cells.transport.auth.CredentialService
import com.pydio.cells.transport.auth.Token
import com.pydio.cells.transport.auth.credentials.JWTCredentials
import com.pydio.cells.transport.auth.jwt.IdToken
import com.pydio.cells.transport.auth.jwt.OAuthConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.db.account.ROAuthState
import org.sinou.android.pydia.utils.AndroidCustomEncoder
import java.util.*

class AuthService(
    private val accountService: AccountService,
    val credentialService: CredentialService
) {

    private val tag = AuthService::class.java.simpleName
    private val encoder: CustomEncoder = AndroidCustomEncoder()

    companion object {

        const val NEXT_ACTION_BROWSE = "browse_account"
        const val NEXT_ACTION_ACCOUNTS = "account_list"
        const val NEXT_ACTION_TERMINATE = "terminate"

        @Volatile
        private var INSTANCE: AuthService? = null

        fun getAuthService(accountService: AccountService): AuthService {
            val tempInstance = AuthService.INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            val accountDB = accountService.accountDB
            synchronized(this) {
                val instance = AuthService(
                    accountService,
                    CredentialService(
                        TokenStore(accountService),
                        PasswordStore(accountService),
                    )
                )
                AuthService.INSTANCE = instance
            }
            return AuthService.INSTANCE!!
        }
    }

    /** Cells' Credentials flow management */

    suspend fun createOAuthIntent(url: ServerURL, next: String): Intent? =
        withContext(Dispatchers.IO) {
            val serverID = StateID(url.id).id
            val server = accountService.sessionFactory.getServer(serverID)
                ?: return@withContext null

            val oAuthState = generateOAuthState()
            val uri: Uri = generateUriData(server.oAuthConfig, oAuthState)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            // Register the state to enable the callback
            val rOAuthState = ROAuthState(
                state = oAuthState,
                serverURL = url,
                next = next,
                startTimestamp = Calendar.getInstance().timeInMillis / 1000L
            )
            accountService.accountDB.authStateDao().insert(rOAuthState)
            intent
        }

    suspend fun handleOAuthResponse(oauthState: String, code: String): Pair<String, String?>? =
        withContext(Dispatchers.IO) {
            var accountID: String? = null

            val rState = accountService.accountDB.authStateDao().get(oauthState)
            if (rState == null) {
                Log.i(tag, "Ignored callback with unknown state: ${oauthState}")
                return@withContext null
            }
            try {
                val transport = accountService.sessionFactory
                    .getAnonymousTransport(rState.serverURL.id) as CellsTransport
                val token = transport.getTokenFromCode(code, encoder)
                accountID = manageRetrievedToken(transport, token)
                // Leave auth state cache clean
                accountService.accountDB.authStateDao().delete(oauthState)
            } catch (e: Exception) {
                Log.e(tag, "Could not finalize credential auth flow")
                e.printStackTrace()
            }
            if (accountID == null) {
                return@withContext null
            } else {
                return@withContext Pair(accountID!!, rState.next)
            }
        }

    private fun manageRetrievedToken(transport: CellsTransport, token: Token): String {
        val idToken = IdToken.parse(encoder, token.idToken)
        val accountID = StateID(idToken.claims.name, transport.server.url())
        val jwtCredentials = JWTCredentials(accountID.username, token)

        accountService.registerAccount(transport.server.serverURL, jwtCredentials)

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

    private fun generateUriData(cfg: OAuthConfig, state: String): Uri {
        var uriBuilder = Uri.parse(cfg.authorizeEndpoint).buildUpon()
        uriBuilder = uriBuilder.appendQueryParameter("state", state)
            .appendQueryParameter("scope", cfg.scope)
            .appendQueryParameter("client_id", ClientData.getInstance().clientId)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", cfg.redirectURI)
        if (cfg.audience != null && "" != cfg.audience) {
            uriBuilder.appendQueryParameter("audience_id", cfg.audience)
        }
        return uriBuilder.build()
    }

    private val seedChars = "abcdef1234567890"

    private fun generateOAuthState(): String {
        val sb = StringBuilder()
        val rand = Random()
        for (i in 0..12) {
            sb.append(seedChars[rand.nextInt(seedChars.length)])
        }
        return sb.toString()
    }
}