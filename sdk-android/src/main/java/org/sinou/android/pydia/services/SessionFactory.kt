package org.sinou.android.pydia.services

import android.util.Log
import com.pydio.cells.api.*
import com.pydio.cells.client.CellsClient
import com.pydio.cells.client.ClientFactory
import com.pydio.cells.transport.CellsTransport
import com.pydio.cells.transport.ServerURLImpl
import com.pydio.cells.transport.StateID
import com.pydio.cells.transport.auth.CredentialService
import com.pydio.cells.transport.auth.Token
import com.pydio.cells.utils.MemoryStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.sinou.android.pydia.room.account.AccountDB
import org.sinou.android.pydia.room.account.RLegacyCredentials
import org.sinou.android.pydia.room.account.RSession
import org.sinou.android.pydia.room.account.RToken

class SessionFactory(
    private val accountDB: AccountDB,
    private val serverStore: Store<Server?>,
    private val transportStore: Store<Transport?>
) : ClientFactory(credService(accountDB), serverStore, transportStore) {

    private val TAG = "SessionFactory"

    companion object {
        fun credService(accountDB: AccountDB): CredentialService {
            return CredentialService(TokenStore(accountDB), PasswordStore(accountDB))
        }
    }

    private val tokenStore = TokenStore(accountDB)
    private val passwordStore = PasswordStore(accountDB)

    // Locally store a cache of known sessions
    private val sessions: Store<RSession> = MemoryStore<RSession>()


    init {
        GlobalScope.launch(Dispatchers.IO) {
            val accounts = accountDB.accountDao().getAccounts()
            for (account in accounts) {
                resurrectSession(StateID(account.username, account.url).accountId)
            }
            Log.i(TAG, "... Session factory initialised")
        }
    }


    @Throws(SDKException::class)
    fun getUnlockedClient(accountID: String): Client? {
//        val session: Session = sessions.get(accountId) ?: throw SDKException(
//            ErrorCodes.internal_error,
//            "no session defined for account " + accountId
//        )

        var transport = transportStore.get(accountID)
        if (transport == null) {
            resurrectSession(accountID)
            transport = transportStore.get(accountID) ?: throw SDKException(
                ErrorCodes.internal_error,
                "could not resurrect session for " + accountID
            )
        }

        return getClient(transport)
    }


    fun resurrectSession(accountID: String) {

        val stateID = StateID.fromId(accountID)
        val account = accountDB.accountDao().getAccount(stateID.username, stateID.serverUrl)
        if (account == null) {
            Log.e(TAG, "No account for $accountID, cannot resurrect session")
            return
        }

        val serverURL = ServerURLImpl.fromAddress(stateID.serverUrl, account.skipVerify)

        var server = serverStore.get(accountID)
        if (server == null) {
            server = registerServer(serverURL)
        }

        var transport = transportStore.get(accountID)
        if (transport == null) {

            try {
                restoreAccount(serverURL, stateID.username)
            } catch (e: Exception) {
                Log.e(TAG, "could not restore account: " + e.message)
            }
/*
            var credentials: Credentials?
            if (account.isLegacy) {
                val pwd = passwordStore.get(accountID)
                if (Str.empty(pwd)) {
                    Log.e(TAG, "No password found for $accountID, cannot resurrect session")
                    return
                }
                credentials = P8Credentials(stateID.username, pwd)
            } else {
                val token = tokenStore.get(accountID)
                if (token == null) {
                    Log.e(TAG, "No token found for $accountID, cannot resurrect session")
                    return
                }
                credentials = JWTCredentials(stateID.username, token)
            }

            registerAccountCredentials(serverURL, credentials)
*/
        }
    }

    override fun getCellsClient(transport: CellsTransport?): CellsClient? {
        return CellsClient(transport, S3Client(transport))
    }

    class PasswordStore(private val accountDB: AccountDB) : Store<String> {

        override fun put(id: String, password: String) {
            val cred = RLegacyCredentials(accountID = id, password = password)
            accountDB.legacyCredentialsDao().insert(cred)
        }

        override fun get(id: String): String? {
            return accountDB.legacyCredentialsDao().getCredential(id)?.password
        }

        override fun remove(id: String) {
            // TODO
        }

        override fun clear() {
            TODO("Not yet implemented")
        }

        override fun getAll(): MutableMap<String, String> {
            throw RuntimeException("foridden action: cannot list all password")
        }

    }

    class TokenStore(private val accountDB: AccountDB) : Store<Token> {

        override fun put(id: String, token: Token) {

            val rToken = RToken(
                accountID = id,
                idToken = token.idToken,
                subject = token.subject,
                value = token.value,
                expiresIn = token.expiresIn,
                expirationTime = token.expirationTime,
                scope = token.scope,
                refreshToken = token.refreshToken,
                tokenType = token.tokenType
            )
            accountDB.tokenDao().insert(rToken)
        }

        override fun get(id: String): Token? {
            val rToken = accountDB.tokenDao().getToken(id)
            var token: Token? = null
            if (rToken != null) {
                token = Token()
                token.value = rToken.value
            }
            return token
        }

        override fun remove(id: String) {
            accountDB.tokenDao().forgetToken(id)
        }

        override fun clear() {
            TODO("Not yet implemented")
        }

        override fun getAll(): MutableMap<String, Token> {
            throw RuntimeException("forbidden action: cannot list all tokens")
        }
    }
}
