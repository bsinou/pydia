package org.sinou.android.pydia.ui.upload

import android.app.Application
import androidx.lifecycle.*
import org.sinou.android.pydia.services.TransferService

/**
 * Hold a list of current transfers
 */
class TransferViewModel(
    val transferService: TransferService,
    application: Application
) : AndroidViewModel(application) {

    private val tag = TransferViewModel::class.java.simpleName

    val transfers = transferService.activeTransfers

//    // Manage UI
//    private val _isLoading = MutableLiveData<Boolean>()
//    val isLoading: LiveData<Boolean>
//        get() = _isLoading

    class TransferViewModelFactory(
        private val transferService: TransferService,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TransferViewModel::class.java)) {
                return TransferViewModel(transferService, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
