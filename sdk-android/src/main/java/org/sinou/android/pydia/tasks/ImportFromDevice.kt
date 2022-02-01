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

fun importFromDevice(
    context: Context,
    parent: RTreeNode
): Boolean {

    MaterialAlertDialogBuilder(context)
        .setTitle("Create folder")
        .setView(R.layout.dialog_edit_text)
        .setPositiveButton("Create") { dialog, _ ->
            val input = (dialog as AlertDialog).findViewById<TextView>(android.R.id.text1)
            doImportFromDevice(context, parent, input!!.text)
        }
        .setNegativeButton("Cancel", null)
        .show()
    return true
}

private fun doImportFromDevice(context: Context, parent: RTreeNode, name: CharSequence) {
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