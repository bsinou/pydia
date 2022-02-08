package org.sinou.android.pydia.services

import android.util.Log
import com.pydio.cells.api.*
import com.pydio.cells.client.CellsClient
import com.pydio.cells.client.ClientFactory
import com.pydio.cells.transport.CellsTransport
import com.pydio.cells.transport.ClientData
import com.pydio.cells.transport.ServerURLImpl
import com.pydio.cells.transport.auth.CredentialService
import com.pydio.cells.transport.auth.Token
import com.pydio.cells.utils.MemoryStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.db.accounts.RLiveSession
import org.sinou.android.pydia.db.accounts.RToken
import org.sinou.android.pydia.utils.hasAtLeastMeteredNetwork
import org.sinou.android.pydia.utils.hasUnMeteredNetwork

class SessionFactory(
    private val accountService: AccountService,
    serverStore: Store<Server>,
    transportStore: Store<Transport>,
    credentialService: CredentialService,
) : ClientFactory(credentialService, serverStore, transportStore) {

    private val tag = SessionFactory::class.java.simpleName

    private var sessionFactoryJob = Job()
    private val sessionFactoryScope = CoroutineScope(Dispatchers.IO + sessionFactoryJob)

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

        fun getSessionFactory(accountService: AccountService, authService: AuthService): SessionFactory {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            val accountDB = accountService.accountDB
            synchronized(this) {
                servers = MemoryStore()
                transports = MemoryStore()
                sessions = MemoryStore()
                val instance = SessionFactory(
                    accountService,
                    servers,
                    transports,
                    authService.credentialService
                )
                INSTANCE = instance
            }
            return INSTANCE!!
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

    private var ready = false
    fun isReady(): Boolean = ready

    init {
        sessionFactoryScope.launch(Dispatchers.IO) {
            val accounts = accountService.accountDB.accountDao().getAccounts()
            for (account in accounts) {
                try {
                    restoreSession(account.accountID)
                } catch (e: SDKException) {
                    Log.e(tag, "Cannot restore session for " + account.accountID + ": " + e.message)
                }
            }
            Log.i(tag, "... Session factory initialised")
            ready = true
        }
    }

    override fun getCellsClient(transport: CellsTransport?): CellsClient {
        return CellsClient(transport, S3Client(transport))
    }

    @Throws(SDKException::class)
    fun getUnlockedUnMeteredClient(accountID: String): Client {
        if (!hasUnMeteredNetwork(CellsApp.instance.applicationContext)) {
            throw SDKException(
                ErrorCodes.no_un_metered_connection,
                "No un-metered connection available"
            )
        }
        return internalGetClient(accountID)
    }

    @Throws(SDKException::class)
    fun getUnlockedClient(accountID: String): Client {
        if (!hasAtLeastMeteredNetwork(CellsApp.instance.applicationContext)) {
            throw SDKException(ErrorCodes.no_internet, "No internet connection is available")
        }

        return internalGetClient(accountID)
    }

    @Throws(SDKException::class)
    private fun internalGetClient(accountID: String): Client {

        // At this point we are sure we have a connection to the internet
        // until it breaks :) ... code defensively afterwards and correctly handle errors
        // First check if we are connected to the internet

        sessions.get(accountID) ?: run {
            try {
                restoreSession(accountID)
            } catch (e: SDKException) {
               throw e
            }
        }

        sessions.get(accountID).let {
            if (it.authStatus == AppNames.AUTH_STATUS_CONNECTED) {
                return getClient(transports.get(accountID)!!)
            } else {
                throw SDKException(
                    ErrorCodes.authentication_required,
                    "cannot unlock session for $accountID, auth status:" + it.authStatus
                )
            }
        }
    }

    @Throws(SDKException::class)
    fun restoreSession(accountID: String) {
        Log.i(tag, "### Restoring session with client data ${ClientData.getInstance().name}")

        val db = accountService.accountDB
        val account = db.accountDao().getAccount(accountID)
            ?: throw SDKException(
                ErrorCodes.unknown_account,
                "No account for $accountID, cannot restore session"
            )

        try {
            val skipVerify = account.tlsMode == 1
            val serverURL = ServerURLImpl.fromAddress(account.url, skipVerify)
            // TODO probably useless
            servers[accountID] ?: registerServer(serverURL)
            transports.get(accountID) ?: restoreAccount(serverURL, account.username)

            val session = db.liveSessionDao().getSession(accountID) ?: throw SDKException(
                ErrorCodes.internal_error,
                "Session for $accountID should exist at this point"
            )
            sessions.put(accountID, session)

        } catch (se: SDKException) {
            Log.e(tag, "could not resurrect session: " + se.message)
            // Handle well known errors and transfer the error to the caller
            when (se.code) {
                ErrorCodes.authentication_required -> {
                    account.authStatus = AppNames.AUTH_STATUS_NO_CREDS
                    db.accountDao().update(account)
                }
                ErrorCodes.token_expired -> {
                    account.authStatus = AppNames.AUTH_STATUS_EXPIRED
                    db.accountDao().update(account)
                }
            }
            throw se
        }
    }
}
