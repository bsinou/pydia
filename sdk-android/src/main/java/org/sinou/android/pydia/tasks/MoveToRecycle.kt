package org.sinou.android.pydia.tasks

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.db.browse.RTreeNode
import org.sinou.android.pydia.utils.showLongMessage

fun moveToRecycle(
    context: Context,
    node: RTreeNode
): Boolean {

    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.confirm_move_to_recycle_title)
        .setIcon(R.drawable.ic_baseline_delete_24)
        .setMessage(context.resources.getString(R.string.confirm_move_to_recycle_desc, node.name))
        .setPositiveButton(R.string.button_confirm) { dialog, _ ->
            doMoveToRecycle(context, node)
        }
        .setNegativeButton(R.string.button_cancel, null)
        .show()
    return true
}

private fun doMoveToRecycle(context: Context, node: RTreeNode) {
    CellsApp.instance.appScope.launch {
        CellsApp.instance.nodeService.delete(StateID.fromId(node.encodedState))
            ?.let {
                withContext(Dispatchers.Main) {
                    showLongMessage(context, it)
                }
            }
    }
}