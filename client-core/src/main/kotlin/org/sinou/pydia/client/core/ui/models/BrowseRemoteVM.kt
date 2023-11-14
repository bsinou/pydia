package org.sinou.pydia.client.core.ui.models

import android.util.Log
import androidx.lifecycle.ViewModel
import org.sinou.pydia.client.core.services.ConnectionService
import org.sinou.pydia.client.core.services.ErrorService
import org.sinou.pydia.sdk.transport.StateID
import org.koin.core.component.KoinComponent

class BrowseRemoteVM(
    private val connectionService: ConnectionService,
    private val errorService: ErrorService
) : ViewModel(), KoinComponent {

    private val logTag = "BrowseRemoteVM"

    val connectionState = connectionService.liveConnectionState
    val isLegacy = connectionService.isRemoteLegacy

    fun watch(newStateID: StateID, isForceRefresh: Boolean) {
        connectionService.setCurrentStateID(newStateID)
        if (isForceRefresh) {
            connectionService.forceRefresh()
        }
    }

    fun pause(oldID: StateID) {
        connectionService.pause(oldID)
    }

    init {
        Log.i(logTag, "... Main browse view model has been initialised")
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(logTag, "Cleared")
    }
}
