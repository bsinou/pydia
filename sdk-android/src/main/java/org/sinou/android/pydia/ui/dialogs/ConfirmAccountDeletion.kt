package org.sinou.android.pydia.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.launch
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R

class ConfirmAccountDeletion : DialogFragment() {

    private val args: ConfirmAccountDeletionArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val account = StateID.fromId(args.accountId)
            val builder = AlertDialog.Builder(it)
            builder
                .setTitle(resources.getString(R.string.confirm_account_deletion_title, account))
                .setMessage(R.string.confirm_account_deletion_desc)
                .setPositiveButton(R.string.button_ok) { dialog, _ ->
                    lifecycleScope.launch {
                        CellsApp.instance.accountService.forgetAccount(account.accountId)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.button_cancel) { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}