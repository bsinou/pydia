package org.sinou.android.pydia.ui.transfer

import androidx.lifecycle.ViewModel
import org.sinou.android.pydia.services.TransferService

/**
 * Hold a list of current transfers
 */
class TransferViewModel(val transferService: TransferService) : ViewModel() {

    val transfers = transferService.activeTransfers

}
