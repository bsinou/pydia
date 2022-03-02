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

fun logoutAccount(
    context: Context,
    accountId: String
): Boolean {
    val account = StateID.fromId(accountId)
    MaterialAlertDialogBuilder(context)
        .setTitle(context.resources.getString(R.string.confirm_account_logout_title, account))
        .setMessage(R.string.confirm_account_logout_desc)
        .setPositiveButton(R.string.button_ok) { dialog, _ ->
            doLogout(context, accountId)
        }
        .setNegativeButton(R.string.button_cancel, null)
        .show()
    return true
}

private fun doLogout(context: Context, accountId: String) {
    CellsApp.instance.appScope.launch {
        CellsApp.instance.accountService.logoutAccount(accountId)
            ?.let {
                withContext(Dispatchers.Main) {
                    showMessage(context, it)
                }
            }
            ?: run {
                withContext(Dispatchers.Main) {
                    showMessage(
                        context,
                        "${StateID.fromId(accountId)} has been disconnected"
                    )
                }
            }
    }
}
