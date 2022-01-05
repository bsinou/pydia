package org.sinou.android.pydia.services

import android.util.Log
import com.pydio.cells.api.*
import com.pydio.cells.client.CellsClient
import com.pydio.cells.client.ClientFactory
import com.pydio.cells.transport.CellsTransport
import com.pydio.cells.transport.ServerURLImpl
import com.pydio.cells.transport.auth.CredentialService
import com.pydio.cells.transport.auth.Token
import com.pydio.cells.utils.MemoryStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.room.account.AccountDB
import org.sinou.android.pydia.room.account.RLegacyCredentials
import org.sinou.android.pydia.room.account.RLiveSession
import org.sinou.android.pydia.room.account.RToken

class SessionFactory(
    private val accountDB: AccountDB,
    serverStore: Store<Server>,
    transportStore: Store<Transport>,
    credentialService: CredentialService,
) : ClientFactory(credentialService, serverStore, transportStore) {

    private val TAG = "SessionFactory"


    companion object {

        @Volatile
        private var INSTANCE: SessionFactory? = null

        // Locally store a cache of known sessions
        @Volatile
        private lateinit var sessions: Store<RLiveSession>

        @Volatile
        private lateinit var servers: Store<Server>

        @Volatile
        private lateinit var transports: Store<Transport>

        @Volatile
        private lateinit var tokenStore: Store<Token>

        @Volatile
        private lateinit var pwdStore: Store<String>

        @Volatile
        private lateinit var credentialService: CredentialService

        fun getSessionFactory(accountDB: AccountDB): SessionFactory {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                servers = MemoryStore()
                transports = MemoryStore()
                sessions = MemoryStore()
                tokenStore = TokenStore(accountDB)
                pwdStore = PasswordStore(accountDB)
                credentialService = CredentialService(tokenStore, pwdStore)
                val instance = SessionFactory(
                    accountDB,
                    servers,
                    transports,
                    credentialService
                )
                INSTANCE = instance
                return instance
            }
        }

        fun fromRToken(rToken: RToken): Token {
            val currToken = Token()
            currToken.tokenType = rToken.tokenType
            currToken.value = rToken.value
            currToken.subject = rToken.subject
            currToken.expiresIn = rToken.expiresIn
            currToken.expirationTime = rToken.expirationTime
            currToken.idToken = rToken.idToken
            currToken.refreshToken = rToken.refreshToken
            currToken.scope = rToken.scope
            return currToken
        }
    }

    init {
        GlobalScope.launch(Dispatchers.IO) {
            val accounts = accountDB.accountDao().getAccounts()
            for (account in accounts) {
                try {
                    restoreSession(account.accountID)
                } catch (e: SDKException) {
                    Log.e(TAG, "Cannot restore session for " + account.accountID + ": " + e.message)
                }
            }
            Log.i(TAG, "... Session factory initialised")
        }
    }


    @Throws(SDKException::class)
    fun getUnlockedClient(accountID: String): Client {

        sessions.get(accountID) ?: run {
            restoreSession(accountID)
        }

        sessions.get(accountID).let {
            if (it.authStatus.equals(AppNames.AUTH_STATUS_CONNECTED)) {
                return getClient(transports.get(accountID)!!)
            } else {
                throw SDKException(
                    ErrorCodes.authentication_required,
                    "cannot unlock session for $accountID, auth status:" + it.authStatus
                )
            }
        }

//        // We should never reach this point
//        throw SDKException(
//            ErrorCodes.unknown_account,
//            "No account for $accountID, cannot restore session"
//        )
    }

    @Throws(SDKException::class)
    fun restoreSession(accountID: String) {

        val account = accountDB.accountDao().getAccount(accountID)
            ?: throw SDKException(
                ErrorCodes.unknown_account,
                "No account for $accountID, cannot restore session"
            )

        try {

            val serverURL = ServerURLImpl.fromAddress(account.url, account.skipVerify)
            // TODO probably useless
            servers.get(accountID) ?: registerServer(serverURL)
            transports.get(accountID) ?: restoreAccount(serverURL, account.username)

            val session = accountDB.liveSessionDao().getSession(accountID) ?: throw SDKException(
                ErrorCodes.internal_error,
                "Session for $accountID should exist at this point"
            )
            sessions.put(accountID, session)

        } catch (se: SDKException) {
            Log.e(TAG, "could not resurrect session: " + se.message)
            // Handle well known errors and transfer the error to the caller
            when (se.code) {
                ErrorCodes.authentication_required -> {
                    account.authStatus = AppNames.AUTH_STATUS_NO_CREDS
                    accountDB.accountDao().update(account)
                }
                ErrorCodes.token_expired -> {
                    account.authStatus = AppNames.AUTH_STATUS_EXPIRED
                    accountDB.accountDao().update(account)
                }
            }
            throw se
        }
    }

    override fun getCellsClient(transport: CellsTransport?): CellsClient? {
        return CellsClient(transport, S3Client(transport))
    }

    class PasswordStore(private val accountDB: AccountDB) : Store<String> {

        override fun put(id: String, password: String) {
            val cred = RLegacyCredentials(accountID = id, password = password)
            if (accountDB.legacyCredentialsDao().getCredential(id) == null) {
                accountDB.legacyCredentialsDao().insert(cred)
            } else {
                accountDB.legacyCredentialsDao().update(cred)
            }
        }

        override fun get(id: String): String? {
            return accountDB.legacyCredentialsDao().getCredential(id)?.password
        }

        override fun remove(id: String) {
            accountDB.legacyCredentialsDao().forgetPassword(id)
        }

        override fun clear() {
            TODO("Not yet implemented")
        }

        override fun getAll(): MutableMap<String, String> {
            val allCreds: MutableMap<String, String> = HashMap()
            for (cred in accountDB.legacyCredentialsDao().getAll()) {
                allCreds[cred.accountID] = cred.password
            }
            return allCreds
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
            if (accountDB.tokenDao().getToken(id) == null) {
                accountDB.tokenDao().insert(rToken)
            } else {
                accountDB.tokenDao().update(rToken)
            }
        }

        override fun get(id: String): Token? {
            val rToken = accountDB.tokenDao().getToken(id)
            var token: Token? = null
            if (rToken != null) {
                token = fromRToken(rToken)
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
            val allCreds: MutableMap<String, Token> = HashMap()
            for (token in accountDB.tokenDao().getAll()) {
                allCreds[token.accountID] = fromRToken(token)
            }
            return allCreds
        }
    }
}
