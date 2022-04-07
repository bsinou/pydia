package org.sinou.android.pydia.ui.common

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.utils.showMessage

fun logoutAccount(
    context: Context,
    accountId: String,
    accountService: AccountService,
): Boolean {
    val account = StateID.fromId(accountId)
    MaterialAlertDialogBuilder(context)
        .setTitle(context.resources.getString(R.string.confirm_account_logout_title, account))
        .setMessage(R.string.confirm_account_logout_desc)
        .setPositiveButton(R.string.button_ok) { _, _ ->
            doLogout(context, accountId, accountService)
        }
        .setNegativeButton(R.string.button_cancel, null)
        .show()
    return true
}

private fun doLogout(
    context: Context, accountId: String,
    accountService: AccountService,
) {
    CellsApp.instance.appScope.launch {
        accountService.logoutAccount(accountId)
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
