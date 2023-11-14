package org.sinou.pydia.client.core.services

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.pydia.client.core.LoadingState
import org.sinou.pydia.client.core.LoginStatus
import org.sinou.pydia.client.core.NetworkStatus
import org.sinou.pydia.client.core.ServerConnection
import org.sinou.pydia.client.core.db.accounts.RSessionView
import org.sinou.pydia.client.core.db.accounts.RWorkspace
import org.sinou.pydia.client.core.ui.theme.CellsColor
import org.sinou.pydia.client.core.utils.BackOffTicker
import org.sinou.pydia.client.core.utils.CellsCancellation
import org.sinou.pydia.client.core.utils.currentTimestamp
import org.sinou.pydia.client.core.utils.timestampToString
import org.sinou.pydia.sdk.api.ErrorCodes
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.SdkNames
import org.sinou.pydia.sdk.transport.StateID
import org.sinou.pydia.sdk.utils.Str
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Hold the session that is currently in foreground for browsing the cache and the remote server.
 */
class ConnectionService(
    private val coroutineService: CoroutineService,
    networkService: NetworkService,
    private val accountService: AccountService,
//     private val nodeService: NodeService,
    private val appCredentialService: AppCredentialService,
) {

    private val id: String = UUID.randomUUID().toString()
    private val logTag = "ConnectionServ_${id.substring(30)}"

    private val serviceScope = coroutineService.cellsIoScope

    // TODO rather manage an array of currently "active" sessions: typically when running the sync in a background session
    private var credentialsJob: Job? = null

    private var pollJob: Job? = null

    // Cold flows
    val sessionView: Flow<RSessionView?> = accountService.activeSessionView
    private val networkStatus: Flow<NetworkStatus> = networkService.networkStatusFlowCold

    val currentConnection: Flow<ServerConnection> =
        sessionView.combine(networkStatus) { session, network ->
            session?.let {
                serverConnectionState(SessionState.from(it, network))
            } ?: ServerConnection.UNREACHABLE
        }

    val currAccountID: Flow<StateID?> =
        sessionView.map { it?.accountID?.let { accId -> StateID.fromId(accId) } }

    // Debug only
    private var lastKnownConnection: ServerConnection? = null
    private fun serverConnectionState(connection: SessionState): ServerConnection {
        val newConn = if (
            !connection.networkStatus.isConnected()
            || !connection.isServerReachable
            || !connection.loginStatus.isConnected()
        ) {
            ServerConnection.UNREACHABLE
        } else if (
            connection.networkStatus == NetworkStatus.ROAMING
            || connection.networkStatus == NetworkStatus.METERED
        ) {
            // TODO also include preference checks for Offline and Roaming
            ServerConnection.LIMITED
        } else {
            ServerConnection.OK
        }
        // TODO remove dirty debug only
        if (newConn != lastKnownConnection) {
            Log.e(logTag, "New connection state: $newConn from connection: $connection")
            lastKnownConnection = newConn
        }
        return newConn
    }

    val customColor: Flow<String?> = sessionView.map { currSession ->
        currSession?.let {
            if (it.isReachable && !it.isLoggedIn()) {
                CellsColor.OfflineColor
            } else {
                it.customColor()
            }
        }
    }
    // val customColor: Flow<String?> = sessionView.combine(networkStatus) { currSession, networkStatus ->
    //         if (currSession == null) null else {
    //             when (serverConnectionState(SessionState.from(currSession, networkStatus))) {
    //                 ServerConnection.UNREACHABLE -> CellsColor.OfflineColor
    //                 ServerConnection.LIMITED -> CellsColor.MeteredColor
    //                 ServerConnection.OK -> currSession.customColor()
    //             }
    //         }
    //     }

    // Expose a flag to the various screens to know if current remote is Cells or P8
    private var _isRemoteLegacy = false
    val isRemoteLegacy: Boolean = _isRemoteLegacy

    @OptIn(ExperimentalCoroutinesApi::class)
    val wss: Flow<List<RWorkspace>> = sessionView.flatMapLatest { currSessionView ->
        accountService.getWsByTypeFlow(
            SdkNames.WS_TYPE_DEFAULT,
            currSessionView?.accountID ?: StateID.NONE.id
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val cells: Flow<List<RWorkspace>> = sessionView.flatMapLatest { currSessionView ->
        accountService.getWsByTypeFlow(
            SdkNames.WS_TYPE_CELL,
            currSessionView?.accountID ?: StateID.NONE.id
        )
    }

    // Hot flows

    private val networkStatusFlow = networkService.networkStatusFlow
    private val loadingFlag = MutableStateFlow(LoadingState.IDLE)

    private var lastTimeChecked = -1L
    private val lock = Any()

    val sessionStateFlow: Flow<SessionState> = sessionView
        .combine(networkStatusFlow) { activeSession, networkStatus ->

            if (activeSession == null) {
                SessionState(networkStatus, false, LoginStatus.Undefined)
            } else {
                _isRemoteLegacy = activeSession.isLegacy

                if (networkStatus.isConnected() && !activeSession.isReachable) {
                    // We have internet access but current server is marked as un-reachable
                    // We try again to connect
                    serviceScope.launch {
                        delay(1000L) // Wait 1 sec before doing the request
                        val doIt: Boolean
                        synchronized(lock) {
                            if (currentTimestamp() - lastTimeChecked > 3) {
                                lastTimeChecked = currentTimestamp()
                                doIt = true
                            } else {
                                doIt = false
                            }
                        }
                        if (doIt) {
                            // This also update cached reachable status of the server
                            appCredentialService.insureServerIsReachable(activeSession.getStateID())
                        }
                    }
                }

                if (activeSession.authStatus != LoginStatus.Connected.id) {
                    pauseMonitoring()
                } else {
                    relaunchMonitoring()
                }
                SessionState(
                    networkStatus,
                    activeSession.isReachable,
                    LoginStatus.fromId(activeSession.authStatus)
                )
            }
        }
        .flowOn(coroutineService.ioDispatcher)
        .conflate()

    val liveConnectionState: StateFlow<ConnectionState> =
        loadingFlag.combine(sessionStateFlow) { loading, connection ->
            appliedConnectionState(loading, connection)
        }.stateIn(
            scope = serviceScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ConnectionState(LoadingState.STARTING, ServerConnection.OK)
        )

    fun appliedConnectionState(
        loading: LoadingState,
        connection: SessionState
    ): ConnectionState {
        val tmpLoading =
            if (!connection.networkStatus.isConnected() || !connection.isServerReachable) {
                LoadingState.IDLE
            } else {
                loading
            }

        return ConnectionState(tmpLoading, serverConnectionState(connection))
    }

    fun isConnected(): Boolean {
        return liveConnectionState.value.serverConnection != ServerConnection.UNREACHABLE
    }

    fun relaunchMonitoring() {
        // Only relaunch if no job is referenced
        pollJob ?: run {
            relaunchCredJob()
            relaunchPollJob()
        }
    }

    fun pauseMonitoring() {
        pauseCredJob()
        pausePollJob()
        // loadingFlag.value = LoadingState.IDLE
    }

    private fun relaunchPollJob() {
        serviceScope.launch {
            pollJob?.cancelAndJoin()
            pollJob = serviceScope.launch {
                while (this.isActive) {
                    watchFolder(this)
                    delay(2000L)
                }
                // loadingFlag.value = LoadingState.IDLE
            }
            Log.i(logTag, "### Relaunched PollJob, new ID: $pollJob")
        }
    }

    private fun pausePollJob() {
        serviceScope.launch {
            pollJob?.cancelAndJoin()
            Log.i(logTag, "### PollJob paused, ID was: $pollJob")
            pollJob = null
        }
    }

    private fun relaunchCredJob() {
        serviceScope.launch {
            credentialsJob?.cancelAndJoin()
            credentialsJob = serviceScope.launch {
                while (this.isActive) {
                    monitorCredentials()
                    // Log.e(logTag, "### About to sleep: ${this.isActive}")
                    delay(20000)
                    // Log.e(logTag, "### After sleep, is active: ${this.isActive}")
                }
            }
            Log.i(logTag, "### Relaunched Credentials Job, new ID: $credentialsJob")
        }
    }

    private fun pauseCredJob() {
        serviceScope.launch {
            credentialsJob?.cancelAndJoin()
            Log.i(logTag, "### Credentials Job paused, ID was: $credentialsJob")
            credentialsJob = null
        }
    }

    // TODO this must be improved
    private suspend fun monitorCredentials() = withContext(coroutineService.ioDispatcher) {
        val currSession = sessionView.first() ?: return@withContext
        val currID = currSession.getStateID()
        if (currSession.isLegacy) {
            // this is for Cells only
            return@withContext
        }
        val token = appCredentialService.get(currID.id) ?: run {
            Log.w(logTag, "Session $currID has no credentials, aborting")
            pauseMonitoring()
            return@withContext
        }
        val validDur = token.expirationTime - currentTimestamp()
        Log.d(logTag, "Monitoring Credentials for $currID, expires in $validDur secs.")
        if (validDur > 120) {
            return@withContext
        }

        Log.w(logTag, "Monitoring Credentials for $currID, found a token that needs refresh")
        val expTimeStr = timestampToString(token.expirationTime, "dd/MM HH:mm")
        Log.d(logTag, "   Expiration time is $expTimeStr")
        val timeout = currentTimestamp() + 30
        val oldTs = token.expirationTime
        var newTs = oldTs

        appCredentialService.requestRefreshToken(currID)
        try {
            while (oldTs == newTs && currentTimestamp() < timeout) {
                delay(1000)
                appCredentialService.getToken(currID)?.let {
                    newTs = it.expirationTime
                }
            }
            return@withContext
        } catch (se: SDKException) {
            Log.e(logTag, "Unexpected error while monitoring refresh token process for $currID")
        }
    }

    /* MANAGE POLLING */

    private var currStateID = StateID.NONE
    private var _isActive = false
    private var delayJob: Job? = null
    private val backOffTicker = BackOffTicker()

    private fun setActive(active: Boolean) {
        if (!active) {
            Log.e(logTag, "#### Setting _isActive to false !!")
        }
        _isActive = active
    }

    fun setCurrentStateID(newStateID: StateID) {
        if (newStateID != currStateID) {
            currStateID = newStateID
            setActive(true)
            loadingFlag.value = LoadingState.STARTING
            backOffTicker.resetIndex()
            delayJob?.cancel(CellsCancellation())
        }
    }

    fun forceRefresh() {
        setActive(true)
        // TODO rather set processing here and idle again when we see we are offline.
        if (isConnected()) {
            loadingFlag.value = LoadingState.PROCESSING
        }
        backOffTicker.resetIndex()
        delayJob?.cancel(CellsCancellation())
    }

    fun pause(oldID: StateID) {
        if (oldID == currStateID) {
            Log.i(logTag, "... Pause remote watching for [${currStateID}]")
            setActive(false)
            loadingFlag.value = LoadingState.IDLE
        } else {
            Log.d(logTag, "Received pause for [$oldID] but currID is [${currStateID}]")
        }
    }

    private suspend fun watchFolder(crScope: CoroutineScope) {
        try {
            doPull(currStateID, _isActive)
        } catch (e: Exception) {
            Log.e(logTag, "Unexpected error: $e")
            e.printStackTrace()
        }
        val nd = backOffTicker.getNextDelay()

        // Handle cancellable delay
        if (!crScope.isActive) {
            return
        }
        val tmpDelayJob = serviceScope.launch {
            try {
                delay(TimeUnit.SECONDS.toMillis(nd))
                var msg = "\tafter ${nd}s sleep,"
                msg += if (_isActive) {
                    " still"
                } else {
                    " STOPPED"
                }
                msg += " watching at $currStateID - $this"
                Log.d(logTag, msg)
            } catch (e: CancellationException) {
                // Expected when we browse as the thread is waiting for next tick.
                Log.d(logTag, "Delay has been cancelled: ${e.message}")
            } catch (e: Exception) {
                Log.e(logTag, "## Unexpected error for $this:\n\t$e")
                // e.printStackTrace()
            }
        }
        delayJob = tmpDelayJob
        tmpDelayJob.join()
    }

    @Throws(SDKException::class)
    private suspend fun doPull(stateID: StateID, isActive: Boolean) {

        var result: Pair<Int, String?> = Pair(0, "")

        if (liveConnectionState.value.serverConnection == ServerConnection.UNREACHABLE) {
            serviceScope.launch {
                // We trigger a ping to the server to check if it is back on-line
                if (appCredentialService.insureServerIsReachable(stateID)) {
                    // In such case we reset the ticker
                    backOffTicker.resetIndex()
                } else {
                    // loadingFlag.value = LoadingState.IDLE
                }
            }
            // we do not try to pull but don't stop the main job
            return
        } else if (StateID.NONE == stateID) {
            // no state ID, we do not try to pull but don't stop the main job
            return
        } else if (!isActive) {
            // The job has been paused, but we still keep on waiting for a state change with backoff timer
            // loadingFlag.value = LoadingState.IDLE
            return
        } else {

            // Manual retry when we come back on an account after a while, to give some time for the token to be refreshed
            var retry = false
            var retryNb = 0
            while (retryNb == 0 || (retry && retryNb < 4)) {
                retryNb++
                try {
                    result = if (Str.empty(stateID.file)) {
                        accountService.refreshWorkspaceList(stateID.account())
                    } else {
// Fixme
                        //                         nodeService.pull(stateID)
                        TODO("Reimplement this ")
                    }
                } catch (se: SDKException) {
                    if (ErrorCodes.token_expired == se.code) {
                        retry = true
                        delay(1500)
                    } else {
                        throw se
                    }
                }
            }
        }

        if (Str.notEmpty(result.second)) {
            Log.e(logTag, "Cannot refresh, msg: ${result.second!!}")
            // errorService.appendError(result.second!!)
            pause(stateID)
        }
        if (result.first > 0) { // At least one change => reset backoff ticker
            backOffTicker.resetIndex()
        }
        loadingFlag.value = LoadingState.IDLE
    }

    init {
        Log.i(logTag, "### ConnectionService initialised")
    }
}

data class ConnectionState(val loading: LoadingState, val serverConnection: ServerConnection)

data class SessionState(
    val networkStatus: NetworkStatus,
    val isServerReachable: Boolean,
    val loginStatus: LoginStatus
) {
    companion object {
        fun from(view: RSessionView, status: NetworkStatus): SessionState {
            return SessionState(
                networkStatus = status,
                isServerReachable = view.isReachable,
                loginStatus = LoginStatus.fromId(view.authStatus)
            )
        }
    }
}

fun SessionState.isOK(): Boolean {
    // TODO rather also rely on server connection to also take prefs limit in account
    return isServerReachable && networkStatus == NetworkStatus.OK && loginStatus.isConnected()
}
