package org.sinou.android.pydia.tasks

import android.content.Context
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.db.browse.RTreeNode
import org.sinou.android.pydia.utils.showMessage

fun createFolder(
    context: Context,
    parent: RTreeNode
): Boolean {

    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.dialog_create_folder_title)
        .setView(R.layout.dialog_edit_text)
        .setPositiveButton(R.string.dialog_create_folder_positive_btn) { dialog, _ ->
            val input = (dialog as AlertDialog).findViewById<TextView>(android.R.id.text1)
            doCreateFolder(context, parent, input!!.text)
        }
        .setNegativeButton(R.string.button_cancel, null)
        .show()
    return true
}

private fun doCreateFolder(context: Context, parent: RTreeNode, name: CharSequence) {
    CellsApp.instance.appScope.launch {
        CellsApp.instance.nodeService.createFolder(
            StateID.fromId(parent.encodedState),
            name.toString()
        )
            ?.let {
                withContext(Dispatchers.Main) {
                    showMessage(context, it)
                }
            }
            ?: run {
                withContext(Dispatchers.Main) {
                    showMessage(
                        context,
                        "Folder created at ${StateID.fromId(parent.encodedState).file}."
                    )
                }
            }
    }
}