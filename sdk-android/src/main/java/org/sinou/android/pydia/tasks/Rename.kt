package org.sinou.android.pydia.tasks

import android.content.Context
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.db.browse.RTreeNode
import org.sinou.android.pydia.utils.showLongMessage

fun rename(
    context: Context,
    node: RTreeNode
): Boolean {

    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.rename_dialog_title)
        .setIcon(R.drawable.ic_baseline_drive_file_rename_outline_24)
        .setView(R.layout.dialog_edit_text)
        .setMessage(context.resources.getString(R.string.rename_dialog_message, node.name))
        .setPositiveButton(R.string.rename_dialog_confirm_button) { dialog, _ ->
            val input = (dialog as AlertDialog).findViewById<TextView>(android.R.id.text1)
            if (input!!.text.isNullOrEmpty()){
                showLongMessage(context, "Please enter a valid not-empty name")
            } else {
                doRename(context, node, input!!.text)
            }
        }
        .setNegativeButton(R.string.button_cancel, null)
        .show()
    return true
}

private fun doRename(context: Context, node: RTreeNode, name: CharSequence) {
    CellsApp.instance.appScope.launch {
        CellsApp.instance.nodeService.rename(StateID.fromId(node.encodedState), name.toString())
            ?.let {
                withContext(Dispatchers.Main) {
                    showLongMessage(context, it)
                }
            }
    }
}