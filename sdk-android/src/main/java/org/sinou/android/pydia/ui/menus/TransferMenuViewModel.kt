package org.sinou.android.pydia.ui.menus

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sinou.android.pydia.services.NodeService
import org.sinou.android.pydia.services.TransferService

/**
 * Holds a Transfer record for the dedicated context menu.
 */
class TransferMenuViewModel(
    transferUID: Long,
    val transferService: TransferService
) : ViewModel() {

//    private val tag = TransferMenuViewModel::class.simpleName
//    private var viewModelJob = Job()
//    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val rTransfer = transferService.getLiveRecord(transferUID)

//    class TransferMenuViewModelFactory(
//        private val transferUID: Long,
//        private val transferService: TransferService,
//        private val application: Application
//    ) : ViewModelProvider.Factory {
//        @Suppress("unchecked_cast")
//        override fun <T : ViewModel> create(modelClass: Class<T>): T {
//            if (modelClass.isAssignableFrom(TransferMenuViewModel::class.java)) {
//                return TransferMenuViewModel(transferUID, transferService, application) as T
//            }
//            throw IllegalArgumentException("Unknown ViewModel class")
//        }
//    }
}