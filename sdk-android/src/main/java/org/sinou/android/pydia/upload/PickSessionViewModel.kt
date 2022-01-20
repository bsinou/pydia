package org.sinou.android.pydia.upload

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.sinou.android.pydia.services.AccountService

/**
 * This holds the list of connected clients to choose a target destination for uploads and moves.
 */
class PickSessionViewModel(
    private val accountService: AccountService,
    application: Application
) : AndroidViewModel(application) {

    private val tag = "PickSessionVM"

    private var viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val sessions = accountService.liveSessions

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class TargetAccountViewModelFactory(
        private val accountService: AccountService,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PickSessionViewModel::class.java)) {
                return PickSessionViewModel(accountService, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
