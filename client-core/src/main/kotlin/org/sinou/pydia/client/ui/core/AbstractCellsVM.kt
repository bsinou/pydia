package org.sinou.pydia.client.ui.core

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.sinou.pydia.client.core.ListType
import org.sinou.pydia.client.core.LoadingState
import org.sinou.pydia.client.core.ServerConnection
import org.sinou.pydia.client.core.db.nodes.RTreeNode
import org.sinou.pydia.client.core.services.ConnectionService
import org.sinou.pydia.client.core.services.models.ConnectionState
import org.sinou.pydia.client.core.services.ErrorService
import org.sinou.pydia.client.core.services.NodeService
import org.sinou.pydia.client.core.services.PreferencesService
import org.sinou.pydia.client.ui.models.ErrorMessage
import org.sinou.pydia.client.core.util.externallyView
import org.sinou.pydia.sdk.api.ErrorCodes
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.transport.StateID

/**
 * Provides generic flows to ease Cells App pages implementation.
 */
open class AbstractCellsVM : ViewModel(), KoinComponent {

    private val logTag = "AbstractCellsVM"

    // Avoid boiling plate to have the connection services here
    private val errorService: ErrorService by inject()
    private val connectionService: ConnectionService by inject()
    protected val prefs: PreferencesService by inject()
    protected val nodeService: NodeService by inject()
    // private val applicationContext: Context by inject()

    // Expose a flow of error messages for the end-user
    val errorMessage: Flow<ErrorMessage?> = errorService.userMessages

    fun errorReceived() {
        // Remove the message from the queue
        errorService.appendError()
    }

    // Loading data from server state
    private val _loadingState = MutableStateFlow(LoadingState.STARTING)
    val connectionState: StateFlow<ConnectionState> =
        _loadingState.combine(connectionService.sessionStateFlow) { currLoadState, connStatus ->
            val cs = connectionService.appliedConnectionState(currLoadState, connStatus)
            Log.e(logTag, "### Loading: $cs (State: $currLoadState, status: $connStatus)")
            cs
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ConnectionState(LoadingState.STARTING, ServerConnection.OK)
        )

    // Preferences
    private val listPrefs = prefs.cellsPreferencesFlow.map { cellsPreferences ->
        cellsPreferences.list
    }
    val layout = listPrefs.map { it.layout }
    protected val defaultOrder = prefs.cellsPreferencesFlow.map { cellsPreferences ->
        cellsPreferences.list.order
    }
    protected val defaultOrderPair = prefs.cellsPreferencesFlow.map { cellsPreferences ->
        prefs.getOrderByPair(
            cellsPreferences,
            ListType.DEFAULT
        )
    }

    fun setListLayout(listLayout: ListLayout) {
        viewModelScope.launch {
            prefs.setListLayout(listLayout)
        }
    }

    /* Generic access to the underlying objects */

    fun isServerReachable(): Boolean {
        return connectionService.isConnected()
    }

    suspend fun getNode(stateID: StateID): RTreeNode? {
        return nodeService.getNode(stateID) ?: run {
            try {
                // Also try to retrieve node from the remote server
                nodeService.tryToCacheNode(stateID)
            } catch (se: SDKException) {
                done(se)
                return null
            }
        }
    }

    suspend fun mustConfirmDL(stateID: StateID): Boolean {
        val serverConnection = connectionService.liveConnectionState.value.serverConnection
        Log.d(logTag, "... must confirm dl 4 $stateID, connection: $serverConnection")
        if (serverConnection == ServerConnection.OK) {
            return false
        } else if (serverConnection == ServerConnection.LIMITED) {
            nodeService.getNode(stateID)?.let {
                val limitedPrefs = prefs.fetchPreferences().meteredNetwork
                val fSize = it.size
                return when {
                    !limitedPrefs.applyLimits -> false
                    !limitedPrefs.askBeforeDL -> false
                    limitedPrefs.sizeThreshold <= 0 -> false
                    else -> {
                        fSize >= limitedPrefs.sizeThreshold * 1024 * 1024
                    }
                }
            }
        }
        return false
    }

    @Throws(SDKException::class)
    suspend fun viewFile(context: Context, stateID: StateID, skipUpToDateCheck: Boolean = false) {
        getNode(stateID)?.let { node ->
            viewFile(context, node, skipUpToDateCheck)
        }
    }

    fun showError(errorMsg: ErrorMessage) {
        errorService.appendError(errorMsg)
    }

    /* Entry points for children models to update current UI state */

    protected fun launchProcessing() {
        _loadingState.value = LoadingState.PROCESSING
        errorService.appendError(errorMsg = null)
    }

    /* Pass a non-null errorMsg parameter when the process has terminated with an error*/
    protected fun done(errorMsg: ErrorMessage? = null) {
        _loadingState.value = LoadingState.IDLE
        errorService.appendError(errorMsg)
    }

    protected fun done(e: Exception) {
        _loadingState.value = LoadingState.IDLE
        errorService.appendError(e)
    }

    protected fun error(msg: String) {
        _loadingState.value = LoadingState.IDLE
        errorService.appendError(msg)
    }

    /* Local helpers */

    @Throws(SDKException::class)
    private suspend fun viewFile(
        context: Context,
        node: RTreeNode,
        skipUpToDateCheck: Boolean = false
    ) {
        val reachable = isServerReachable()
        val currSkip = skipUpToDateCheck || !reachable
        Log.e(
            logTag, "Launch view file, skip check: $currSkip," +
                    " loading: ${connectionService.liveConnectionState.value.loading}" +
                    " server reachable: $reachable}"
        )
        val (lf, isUpToDate) = nodeService.getLocalFile(node, currSkip)

        if (lf == null) {
            throw SDKException(ErrorCodes.no_local_file)
        } else if (!isUpToDate) {
            throw SDKException(ErrorCodes.outdated_local_file)
        } else {
            // TODO investigate. We use the activity context to launch the view activity, otherwise we have this message:
            //   Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
            // externallyView(applicationContext, lf, node)
            externallyView(context, lf, node)
        }
    }
}
