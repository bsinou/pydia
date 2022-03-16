package org.sinou.android.pydia.ui.menus

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sinou.android.pydia.services.NodeService

/**
 * Holds a TreeNode for the various context menus.
 */
class TreeNodeMenuViewModel(
    val stateID: StateID,
    private val contextType: String,
    private val nodeService: NodeService,
    application: Application
) : AndroidViewModel(application) {

    private val tag = "NodeMenuViewModel"

    private var viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val node = nodeService.getLiveNode(stateID)

    var _targetUri: Uri? = null
    val targetUri: Uri?
        get() = _targetUri

    fun prepareImport(uri: Uri) {
        vmScope.launch {
            _targetUri = uri
        }
    }

    class NodeMenuViewModelFactory(
        private val stateID: StateID,
        private val contextType: String,
        private val nodeService: NodeService,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TreeNodeMenuViewModel::class.java)) {
                return TreeNodeMenuViewModel(stateID, contextType, nodeService, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}