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
    val stateIDs: List<StateID>,
    val contextType: String,
    private val nodeService: NodeService,
) : ViewModel() {

    private val logTag = TreeNodeMenuViewModel::class.simpleName

    private var viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val node = nodeService.getLiveNode(stateIDs[0])
    val nodes = nodeService.getLiveNodes(stateIDs)

    private var _targetUri: Uri? = null
    val targetUri: Uri?
        get() = _targetUri

    fun prepareImport(uri: Uri) {
        vmScope.launch {
            _targetUri = uri
        }
    }
}
