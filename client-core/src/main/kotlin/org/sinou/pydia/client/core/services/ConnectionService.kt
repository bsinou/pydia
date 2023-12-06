package org.sinou.pydia.client.core.services

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.sinou.pydia.client.core.LoadingState
import org.sinou.pydia.client.core.LoginStatus
import org.sinou.pydia.client.core.NetworkStatus
import org.sinou.pydia.client.core.ServerConnection
import org.sinou.pydia.client.core.db.accounts.RSessionView
import org.sinou.pydia.client.core.db.accounts.RWorkspace
import org.sinou.pydia.client.core.services.models.ConnectionState
import org.sinou.pydia.client.core.services.models.SessionState
import org.sinou.pydia.client.core.util.currentTimestamp
import org.sinou.pydia.client.ui.theme.CellsColor
import org.sinou.pydia.sdk.api.SdkNames
import org.sinou.pydia.sdk.transport.StateID
import java.util.UUID

/**
 * Centralises session flow management for all other services and models.
 */
class ConnectionService(
    coroutineService: CoroutineService,
    networkService: NetworkService,
    preferencesService: PreferencesService,
    private val appCredentialService: AppCredentialService,
    private val accountService: AccountService,
    private val pollService: PollService,
) {

    private val id: String = UUID.randomUUID().toString()
    private val logTag = "ConnectionServ_${id.substring(30)}"

    private val serviceScope = coroutineService.cellsIoScope

    private val lock = Any()
    private var lastTimeChecked = -1L

    private val networkStatusFlow = networkService.networkStatusFlow
    private val loadingFlag = pollService.loadingFlag
    private val applyColors =
        preferencesService.cellsPreferencesFlow.map { prefs -> prefs.meteredNetwork.showWarning }

    // Cold session view flow
    val sessionView: Flow<RSessionView?> = accountService.activeSessionView

    private val sessionColor: Flow<String?> =
        sessionView.combine(networkStatusFlow) { currSession, networkStatus ->
            if (currSession == null) null else {
                when (serverConnectionState(SessionState.from(currSession, networkStatus))) {
                    ServerConnection.UNREACHABLE -> CellsColor.OfflineColor
                    ServerConnection.LIMITED -> CellsColor.MeteredColor
                    ServerConnection.OK -> currSession.customColor()
                }
            }
        }

    val customColor: Flow<String?> = sessionView.combine(applyColors) { session, dynamicCols ->
        session to dynamicCols
    }.combine(sessionColor) { (session, dynamicCols), sessionColor ->
        if (dynamicCols) {
            sessionColor
        } else {
            session?.let {
                if (it.isReachable && !it.isLoggedIn()) {
                    CellsColor.OfflineColor
                } else {
                    it.customColor()
                }
            }
        }
    }

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

    val sessionStateFlow: StateFlow<SessionState> = sessionView
        .combine(networkStatusFlow) { activeSession, networkStatus ->
            val nextState = if (activeSession == null) {
                SessionState(StateID.NONE, false, networkStatus, LoginStatus.Undefined)
            } else {
                if (networkStatus.isConnected() && !activeSession.isReachable) {
                    // We have internet access but current server is marked as un-reachable
                    // We try again to connect
                    serviceScope.launch {
                        // switch to unreachable until proven we can reach it (4 lines below)
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
                    // TODO we do not pause yet monitoring when we have no connection or server is unreachable ??
                    pauseMonitoring()
                } else {
                    relaunchMonitoring()
                }
                SessionState(
                    activeSession.getStateID(),
                    activeSession.isReachable,
                    networkStatus,
                    LoginStatus.fromId(activeSession.authStatus)
                )
            }
            Log.d(logTag, "... Emitting new Session state: $nextState")
            nextState
        }.stateIn(
            scope = serviceScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = SessionState.NONE
        )

    // And direct derivatives
    val currAccountID: Flow<StateID> = sessionStateFlow.map { it.accountID }

    private var wasReachable = true

    @OptIn(FlowPreview::class)
    val liveConnectionState: StateFlow<ConnectionState> =
        sessionStateFlow.combine(loadingFlag) { connection, loading ->
            appliedConnectionState(loading, connection)
        }.onEach {
            if (it.serverConnection.isConnected() && !wasReachable) {
                pollService.relaunchMonitoring(true)
            }
            wasReachable = it.serverConnection.isConnected()
        }.debounce(400L)
            .stateIn(
                scope = serviceScope,
                started = SharingStarted.WhileSubscribed(30000L),
                initialValue = appliedConnectionState(LoadingState.STARTING, SessionState.NONE)
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
        return newConn
    }

    // Shortcuts
    fun isConnected(): Boolean {
        return liveConnectionState.value.serverConnection != ServerConnection.UNREACHABLE
    }

    // Delegated to pollService
    fun relaunchMonitoring() {
        // Set to false without second thought as it was so before
        pollService.relaunchMonitoring(false)
    }

    fun pauseMonitoring() {
        pollService.pauseMonitoring()
    }

    fun setCurrentStateID(newStateID: StateID) {
        pollService.watch(newStateID)
    }

    fun forceRefresh() {
        pollService.forceRefresh()
    }

    fun pause(oldID: StateID) {
        pollService.pause(oldID)
    }

    init {
        Log.i(logTag, "### ConnectionService initialised")
    }
}
