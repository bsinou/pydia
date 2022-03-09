package org.sinou.android.pydia.ui.browse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.services.NodeService

/**
 * Holds a live list of the offline roots for the current session
 */
class OfflineRootsViewModel(
    private val nodeService: NodeService,
    val stateID: StateID,
    application: Application
) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val offlineRoots = CellsApp.instance.nodeService.listOfflineRoots(stateID)

    class OfflineRootsViewModelFactory(
        private val nodeService: NodeService,
        private val stateID: StateID,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OfflineRootsViewModel::class.java)) {
                return OfflineRootsViewModel(nodeService, stateID, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
