package org.sinou.pydia.client.core.services

import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.sinou.pydia.client.core.LoginStatus
import org.sinou.pydia.client.core.db.accounts.AccountDao
import org.sinou.pydia.client.core.db.accounts.SessionDao
import org.sinou.pydia.client.core.db.accounts.SessionViewDao
import org.sinou.pydia.client.core.util.currentTimestamp
import org.sinou.pydia.client.core.util.timestampToString
import org.sinou.pydia.sdk.api.ErrorCodes
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.SDKException.RemoteIOException
import org.sinou.pydia.sdk.api.Store
import org.sinou.pydia.sdk.api.Transport
import org.sinou.pydia.sdk.transport.CellsTransport
import org.sinou.pydia.sdk.transport.ServerURLImpl
import org.sinou.pydia.sdk.transport.StateID
import org.sinou.pydia.sdk.transport.auth.CredentialService
import org.sinou.pydia.sdk.transport.auth.Token
import java.util.UUID

/**
 * This app specific credential service provides a refresh method that insures that only
 * one refresh token process is launched at a time.
 */
class AppCredentialService(
    private val tokenStore: Store<Token>,
    passwordStore: Store<String>,
    private val transportStore: Store<Transport>,
    coroutineService: CoroutineService,
    private val networkService: NetworkService,
    private val accountDao: AccountDao,
    private val sessionDao: SessionDao,
    private val sessionViewDao: SessionViewDao,
) : CredentialService(tokenStore, passwordStore), KoinComponent {

    //    private val logTag = "AppCredentialService"
    private val id: String = UUID.randomUUID().toString()
    private val logTag = "AppCredService_${id.substring(30)}"

    // Insure we only process one refresh request at a time
    private val requestRefreshChannel = Channel<StateID>()

    // Semaphore for the refresh process.
    private val lock = Any()

    private val serviceScope = coroutineService.cellsIoScope
    private val ioDispatcher = coroutineService.ioDispatcher
    private val cpuDispatcher = coroutineService.cpuDispatcher

    private val maxFail = 300
    private var failNb: Int = 0

    init {
        serviceScope.launch {
            withContext(cpuDispatcher) {
                while (failNb < maxFail) {
                    try {
                        val currID = requestRefreshChannel.receive()
                        // Log.e(logTag, "#### Received refresh request")
//                         private val id: String = UUID.randomUUID().toString()   Log.e(logTag, "Received refresh token request for $currID")
                        safelyRefreshToken(currID)
                        delay(2000)
                        failNb = 0
                    } catch (e: Exception) {
                        Log.e(
                            logTag,
                            "Credential request channel consumer has failed with ${e.message}"
                        )
                        e.printStackTrace()
                        delay(2000)
                        failNb++
                    }
                }

                Log.e(
                    logTag,
                    "### Too many errors while trying to read channel"
                )
                Thread.dumpStack()
                // We failed more than 1000 times in a row
                throw SDKException(
                    ErrorCodes.internal_error,
                    "Too many errors while trying to read channel"
                )
            }
        }
    }

    suspend fun getToken(stateID: StateID): Token? = withContext(ioDispatcher) {
        tokenStore[stateID.id]
    }

    /**
     * Asynchronously triggers  a refresh token request if:
     * - remote is Cells
     * - we have all that we need
     * - no refresh is already running
     */
    override fun requestRefreshToken(stateID: StateID) {
        // Log.e(logTag, "Sending refresh token request for $stateID")
        // Thread.dumpStack()
        serviceScope.launch {
            requestRefreshChannel.send(stateID)
        }
    }

    private suspend fun safelyRefreshToken(stateID: StateID) = withContext(ioDispatcher) {
        val isConnected = networkService.isConnected()
        // Sanity checks
        if (!isConnected) {
            Log.e(
                logTag,
                "Cannot refresh token $stateID with no access to the remote server"
            )
            return@withContext
        }
        if (!insureServerIsReachable(stateID)) {
            Log.e(logTag, "Cannot refresh token $stateID, server is not reachable")
            return@withContext
        }

        synchronized(lock) {
            val token: Token = tokenStore[stateID.id] ?: run {
                Log.e(logTag, "Cannot refresh, no token for $stateID")
                return@withContext
            }

            if (token.refreshingSinceTs > 0) {
                if (token.refreshingSinceTs + 300 < currentTimestamp()) {
                    val tsSuffix = timestampToString(token.refreshingSinceTs, "dd/MM HH:mm")
                    Log.e(logTag, "Token for $stateID is refreshing since $tsSuffix")
                    Log.e(logTag, "  Removing lock and retrying")
                    synchronized(lock) {
                        token.refreshingSinceTs = 0
                        put(stateID.id, token)
                    }
                } else {
                    Log.e(logTag, "Token for $stateID is already refreshing ignoring")
                    return@withContext
                }
            }

            if (token.expirationTime > currentTimestamp() + token.expiresIn / 2) {
                // It has been refreshed recently, ignoring
                // Log.d(logTag, "Token for $stateID has just been refreshed, ignoring")
                return@withContext
            }

            // Insure we have a transport already defined in the store
            val transport = transportStore[stateID.accountId]
            if (transport == null) {
                Log.e(logTag, "Cannot refresh, no transport defined for $stateID")
                return@withContext
            } else if (transport !is CellsTransport) {
                Log.e(logTag, "Cannot refresh, transport for $stateID is not for Cells")
                return@withContext
            }

            // Now we start the real refresh token process
            token.refreshingSinceTs = currentTimestamp()
            put(stateID.id, token)

            launch {
                doRefresh(stateID, token, transport)
            }
        }
    }

    // First ping the server: we can use the refresh token only once. If we fail we sleep 2 sec.
    suspend fun insureServerIsReachable(stateID: StateID): Boolean = withContext(ioDispatcher) {
        val sessionView = sessionViewDao.getSession(stateID.accountId) ?: run {
            Log.e(logTag, "Cannot refresh, unknown session: $stateID")
            return@withContext false
        }
        val session = sessionDao.getSession(stateID.accountId) ?: run {
            Log.e(logTag, "Session view with no session for $stateID: something went wrong")
            Thread.dumpStack()
            return@withContext false
        }

        val serverURL = ServerURLImpl.fromAddress(sessionView.url, sessionView.skipVerify())
        try {
            serverURL.ping()
            if (!session.isReachable) { // Update reachable flag ASAP
                session.isReachable = true
                sessionDao.update(session)
            }
            return@withContext true
        } catch (e: Exception) {
            Log.e(logTag, "Aborting refresh process: ${e.message} - $this")
            // Update reachable flag in the Session DB
            session.isReachable = false
            sessionDao.update(session)
//            Log.d(logTag, "Before delay - $this")
            delay(2000)
//            Log.d(logTag, "After delay - $this")
//            Thread.dumpStack()
            return@withContext false
        }
    }

    private fun doRefresh(
        stateID: StateID,
        token: Token,
        transport: CellsTransport
    ) {
        try {
            Log.i(logTag, "... Launching effective refresh token process for $stateID")

            val newToken = token.refreshToken?.let {
                Log.d(logTag, "... About to wait for refreshed token")
                transport.getRefreshedOAuthToken(it)
            } ?: kotlin.run {
                Log.e(logTag, "No refresh token available for $stateID, cannot refresh")
                throw SDKException(
                    ErrorCodes.refresh_token_expired,
                    "No refresh token available for $stateID, cannot refresh"
                )
            }

            // Token has been refreshed Store and return
            synchronized(lock) {
                put(stateID.id, newToken)
            }
            accountDao.getAccount(stateID.accountId)?.let {
                it.authStatus = LoginStatus.Connected.id
                accountDao.update(it)
            }
            Log.d(logTag, "... Refresh token process done for $stateID")
        } catch (re: RemoteIOException) {
            Log.e(logTag, "Could not refresh for $stateID. Still keeping old credentials")
            Log.e(logTag, "  Cause: remoteIOException: ${re.message}")
        } catch (se: Exception) {

            if (se is SDKException && (se.code == ErrorCodes.refresh_token_expired || se.code == ErrorCodes.refresh_token_not_valid)) {
                // Could not refresh, finally deleting credentials to avoid being stuck
                logout(stateID, "#${se.code}: ${se.message}")
                throw se
            }
            Log.e(logTag, "Could not refresh for $stateID, unexpected exception: ${se.message}")
            se.printStackTrace()
            Log.e(logTag, "Keep old credentials and abort.")
            return
        }
    }

    private fun logout(stateID: StateID, cause: String) {
        Log.e(logTag, "### Refresh token expired for $stateID")
        Log.e(logTag, "  Cause: $cause")
        Log.e(logTag, "  !! Removing stored credentials !! ")
        synchronized(lock) {
            remove(stateID.id)
        }
        accountDao.getAccount(stateID.accountId)?.let {
            it.authStatus = LoginStatus.NoCreds.id
            accountDao.update(it)
        }
    }
}
