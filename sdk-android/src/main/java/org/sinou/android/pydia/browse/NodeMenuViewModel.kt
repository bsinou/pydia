package org.sinou.android.pydia.browse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.room.browse.RTreeNode
import org.sinou.android.pydia.services.NodeService

/**
 * Holds a FileNode for the various context menus.
 */
class NodeMenuViewModel(
    private val stateID: StateID,
    private val contextType: String,
    private val nodeService: NodeService,
    application: Application
) : AndroidViewModel(application) {

    private val tag = "NodeMenuViewModel"

    val node = nodeService.getLiveNode(stateID)

    class NodeMenuViewModelFactory(
        private val stateID: StateID,
        private val contextType: String,
        private val nodeService: NodeService,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NodeMenuViewModel::class.java)) {
                return NodeMenuViewModel(stateID, contextType, nodeService, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
