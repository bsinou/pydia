package org.sinou.pydia.client.ui.browse.models

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.sinou.pydia.client.core.db.accounts.RWorkspace
import org.sinou.pydia.client.core.db.nodes.RTreeNode
import org.sinou.pydia.client.ui.core.AbstractCellsVM
import org.sinou.pydia.client.ui.models.TreeNodeItem
import org.sinou.pydia.client.ui.models.toTreeNodeItems
import org.sinou.pydia.sdk.transport.StateID

/**
 * Main ViewModel when browsing a Cells or P8 server.
 * It holds the StateID of the current **parent** folder and a Flow list of its children.
 */
class FolderVM(private val stateID: StateID) : AbstractCellsVM() {

    private val logTag = "FolderVM"

    // Load the current parent for various labels and generic actions
    private val _rTreeNode = MutableStateFlow<RTreeNode?>(null)
    val treeNode: StateFlow<RTreeNode?> = _rTreeNode.asStateFlow()
    private val _rWorkspace = MutableStateFlow<RWorkspace?>(null)
    val workspace: StateFlow<RWorkspace?> = _rWorkspace.asStateFlow()

    // Observe parent folder's children
    @OptIn(ExperimentalCoroutinesApi::class)
    private val tnChildren: Flow<List<RTreeNode>> =
        defaultOrderPair.flatMapLatest { (order, direction) ->
            try {
                if (stateID.slug.isNullOrEmpty()) {
                    nodeService.listWorkspaces(stateID)
                } else {
                    nodeService.sortedListFlow(stateID, order, direction)
                }
            } catch (e: Exception) {
                // This should never happen but it has been seen in prod
                // Adding a failsafe to avoid crash
                Log.e(logTag, "Could not list children for $stateID: ${e.message}")
                flow { listOf<RTreeNode>() }
            }
        }
    val children: Flow<List<TreeNodeItem>> = tnChildren.map { nodes ->
        toTreeNodeItems(nodeService, nodes)
    }

    init {
        viewModelScope.launch {
            nodeService.getNode(stateID)?.let { node ->
                if (this.isActive) {
                    _rTreeNode.value = node
                    if (node.isWorkspaceRoot()) {
                        _rWorkspace.value = nodeService.getWorkspace(stateID)
                    }
                }
            }
        }
    }
}
