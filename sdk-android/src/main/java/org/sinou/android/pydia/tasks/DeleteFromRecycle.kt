package org.sinou.android.pydia.tasks

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.db.nodes.RTreeNode
import org.sinou.android.pydia.services.NodeService
import org.sinou.android.pydia.utils.showLongMessage

fun deleteFromRecycle(
    context: Context,
    node: RTreeNode,
    nodeService: NodeService,
): Boolean {

    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.confirm_permanent_deletion_title)
        .setIcon(R.drawable.ic_baseline_delete_24)
        .setMessage(
            context.resources.getString(
                R.string.confirm_permanent_deletion_desc,
                node.name
            )
        )
        .setPositiveButton(R.string.button_confirm) { _, _ ->
            doDeleteFromRecycle(context, node, nodeService)
        }
        .setNegativeButton(R.string.button_cancel, null)
        .show()
    return true
}

private fun doDeleteFromRecycle(context: Context, node: RTreeNode, nodeService: NodeService) {
    CellsApp.instance.appScope.launch {
        nodeService.delete(StateID.fromId(node.encodedState))
            ?.let {
                withContext(Dispatchers.Main) {
                    showLongMessage(context, it)
                }
            }
    }
}