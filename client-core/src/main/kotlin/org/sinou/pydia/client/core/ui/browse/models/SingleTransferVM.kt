package org.sinou.pydia.client.core.ui.browse.models

import android.util.Log
import androidx.lifecycle.ViewModel
import org.sinou.pydia.client.core.db.nodes.RTransfer
import org.sinou.pydia.client.core.services.TransferService
import org.sinou.pydia.sdk.transport.StateID
import kotlinx.coroutines.flow.Flow

/** Provides access to a RTransfer record given an account and a transfer ID*/
class SingleTransferVM(
    private val accountID: StateID,
    private val transferService: TransferService,
) : ViewModel() {

    private val logTag = "SingleTransferVM"

    fun getTransfer(transferID: Long): Flow<RTransfer?> =
        transferService.liveTransfer(accountID, transferID)

    init {
        Log.d(logTag, "after init for $accountID")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(logTag, "after clear for $accountID")
    }
}
