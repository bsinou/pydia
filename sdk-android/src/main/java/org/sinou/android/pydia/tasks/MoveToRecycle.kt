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
            doMoveToRecycle(context, node.encodedState)
        }
        .setNegativeButton(R.string.button_cancel, null)
        .show()
    return true
}

fun moveNodesToRecycle(
    context: Context,
    states: List<StateID>
): Boolean {

    val msg = if (states.size == 1) {
        context.resources.getQuantityString(R.plurals.confirm_multi_move_to_recycle_desc, 1)
    } else {
        String.format(
            context.resources.getQuantityString(
                R.plurals.confirm_multi_move_to_recycle_desc,
                states.size
            ), states.size
        )
    }

    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.confirm_move_to_recycle_title)
        .setIcon(R.drawable.ic_baseline_delete_24)
        .setMessage(msg)
        .setPositiveButton(R.string.button_confirm) { dialog, _ ->

            for (stateId in states) {
                doMoveToRecycle(context, stateId.id)
            }

        }
        .setNegativeButton(R.string.button_cancel, null)
        .show()
    return true
}

private fun doMoveToRecycle(context: Context, encodedState: String) {
    CellsApp.instance.appScope.launch {
        CellsApp.instance.nodeService.delete(StateID.fromId(encodedState))
            ?.let {
                withContext(Dispatchers.Main) {
                    showLongMessage(context, it)
                }
            }
    }
}