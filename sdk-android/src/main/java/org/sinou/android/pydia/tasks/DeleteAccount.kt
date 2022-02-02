package org.sinou.android.pydia.ui.common

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.utils.showMessage

fun deleteAccount(
    context: Context,
    accountId: String
): Boolean {
    val account = StateID.fromId(accountId)
    MaterialAlertDialogBuilder(context)
        .setTitle(context.resources.getString(R.string.confirm_account_deletion_title, account))
        .setMessage(R.string.confirm_account_deletion_desc)
        .setPositiveButton(R.string.button_ok) { dialog, _ ->
            doDelete(context, accountId)
        }
        .setNegativeButton("Cancel", null)
        .show()
    return true
}

private fun doDelete(context: Context, accountId: String) {
    CellsApp.instance.appScope.launch {
        CellsApp.instance.accountService.forgetAccount(accountId)
            ?.let {
                withContext(Dispatchers.Main) {
                    showMessage(context, it)
                }
            }
            ?: run {
                withContext(Dispatchers.Main) {
                    showMessage(
                        context,
                        "${StateID.fromId(accountId)} has been removed"
                    )
                }
            }
    }
}
