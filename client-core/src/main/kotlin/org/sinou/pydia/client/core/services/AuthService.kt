package org.sinou.pydia.client.core.services

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.withContext
import org.sinou.pydia.client.core.db.auth.AuthDB
import org.sinou.pydia.client.core.db.auth.ROAuthState
import org.sinou.pydia.client.core.util.AndroidCustomEncoder
import org.sinou.pydia.client.core.util.currentTimestamp
import org.sinou.pydia.sdk.api.CustomEncoder
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.ServerURL
import org.sinou.pydia.sdk.transport.CellsTransport
import org.sinou.pydia.sdk.transport.ClientData
import org.sinou.pydia.sdk.transport.StateID
import org.sinou.pydia.sdk.transport.auth.Token
import org.sinou.pydia.sdk.transport.auth.credentials.JWTCredentials
import org.sinou.pydia.sdk.transport.auth.jwt.IdToken
import org.sinou.pydia.sdk.transport.auth.jwt.OAuthConfig
import java.util.*

class AuthService(
    coroutineService: CoroutineService,
    authDB: AuthDB
) {
    private val logTag = "AuthService"
    private val encoder: CustomEncoder = AndroidCustomEncoder()

    private val ioDispatcher = coroutineService.ioDispatcher

    private val tokenDao = authDB.tokenDao()
    private val authStateDao = authDB.authStateDao()

    companion object {
        const val LOGIN_CONTEXT_CREATE = "new_account"
        const val LOGIN_CONTEXT_BROWSE = "browse"
        const val LOGIN_CONTEXT_SHARE = "share"
        const val LOGIN_CONTEXT_ACCOUNTS = "account_list"
    }

    fun forgetCredentials(accountID: StateID) {
        tokenDao.deleteToken(accountID.id)
    }

    suspend fun clearOAuthStates() =
        withContext(ioDispatcher) {
            authStateDao.deleteAll()
        }

    /** Cells' Credentials flow management */
    suspend fun generateOAuthFlowUri(
        sessionFactory: SessionFactory,
        url: ServerURL,
        loginContext: String
    ): Uri? = withContext(ioDispatcher) {
        try {

            val server = sessionFactory.getServer(url.getStateID())
            // TODO Do we want to try to re-register the server when it is unknown from the SessionFactory
                ?: let {
                    Log.e(logTag, "could not get server with url ${url.getStateID()}")
                    return@withContext null
                }

            val oAuthState = generateOAuthState()
            val uri: Uri = generateUriData(server.oAuthConfig!!, oAuthState)
            // Register the state to enable the callback
            val rOAuthState = ROAuthState(
                state = oAuthState,
                serverURL = url,
                loginContext = loginContext,
                startTimestamp = currentTimestamp()
            )
            Log.d(logTag, "About to store OAuth state: $rOAuthState")
            authStateDao.insert(rOAuthState)
            return@withContext uri
        } catch (e: SDKException) {
            Log.e(
                logTag, "could not create intent for ${url.url.host}," +
                        " cause: ${e.code} - ${e.message}"
            )
            e.printStackTrace()
            return@withContext null
        } catch (e: Exception) {
            Log.e(logTag, "Unexpected exception: ${e.message}")
            e.printStackTrace()
            return@withContext null
        }
    }

    suspend fun isAuthStateValid(authState: String): Boolean = withContext(ioDispatcher) {
        return@withContext authStateDao.get(authState) != null
    }

    /**
     * Returns the target session's account StateID and a login context when the process is successful,
     * or null otherwise
     */
    suspend fun handleOAuthResponse(
        accountService: AccountService,
        sessionFactory: SessionFactory,
        oauthState: String,
        code: String
    ): Pair<StateID, String>? = withContext(ioDispatcher) {
        var accountID: StateID? = null

        val rState = authStateDao.get(oauthState)
        if (rState == null) {
            Log.w(logTag, "Ignored callback with unknown state: $oauthState")
            return@withContext null
        }
        try {
            Log.i(logTag, "... Handling state ${rState.state} for ${rState.serverURL.url}")

            val transport = sessionFactory
                .getAnonymousTransport(rState.serverURL.getStateID()) as CellsTransport
            val token = transport.getTokenFromCode(code, encoder)
            accountID = manageRetrievedToken(accountService, transport, token)
            Log.e(logTag, "... Token managed. Login context: ${rState.loginContext}")

            // Leave OAuth state cacheDB clean
            authStateDao.delete(oauthState)
            // When creating a new account, we want to put its session on the foreground
            if (rState.loginContext == LOGIN_CONTEXT_CREATE) {
                accountService.openSession(accountID)
            }
        } catch (e: Exception) {
            Log.e(logTag, "Could not finalize credential auth flow")
            e.printStackTrace()
        }
        if (accountID == null) {
            return@withContext null
        } else {
            return@withContext Pair(accountID, rState.loginContext)
        }
    }

    @Throws(SDKException::class)
    private suspend fun manageRetrievedToken(
        accountService: AccountService,
        transport: CellsTransport,
        token: Token
    ): StateID {

        // FIXME ugly casts...
        val idToken = IdToken.parse(encoder, token.idToken!!)
        val accountID = StateID(idToken.claims!!.name, transport.server.url())
        val jwtCredentials = JWTCredentials(accountID.username!!, token)

        accountService.signUp(transport.server.serverURL, jwtCredentials)

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
        return accountID
    }

    private fun generateUriData(cfg: OAuthConfig, state: String): Uri {
        var uriBuilder = Uri.parse(cfg.authorizeEndpoint).buildUpon()
        uriBuilder = uriBuilder.appendQueryParameter("state", state)
            .appendQueryParameter("scope", cfg.scope)
            .appendQueryParameter("client_id", ClientData.getInstance().clientID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", cfg.redirectURI)
        if (cfg.audience != null && "" != cfg.audience) {
            uriBuilder.appendQueryParameter("audience_id", cfg.audience)
        }
        return uriBuilder.build()
    }

    private fun generateOAuthState(): String {
        val sb = StringBuilder()
        val rand = Random()
        for (i in 0..12) {
            sb.append(seedChars[rand.nextInt(seedChars.length)])
        }
        return sb.toString()
    }

    private val seedChars = "abcdef1234567890"
}
