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
 * Holds a live list of the cached bookmarks for the current session
 */
class BookmarksViewModel(
    private val nodeService: NodeService,
    val stateID: StateID,
    application: Application
) : AndroidViewModel(application) {

    val bookmarks = CellsApp.instance.nodeService.listBookmarks(stateID)

    private var viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    class BookmarksViewModelFactory(
        private val nodeService: NodeService,
        private val stateID: StateID,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BookmarksViewModel::class.java)) {
                return BookmarksViewModel(nodeService, stateID, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    fun triggerRefresh() {
        // For the time being, we only launch the bookmark list refresh explicitly,
        // typically on resume from the corresponding fragment
        // TODO implement dynamic update
        vmScope.launch {
            nodeService.refreshBookmarks(stateID)
        }
    }

}
