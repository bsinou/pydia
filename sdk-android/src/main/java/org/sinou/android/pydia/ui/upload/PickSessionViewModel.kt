package org.sinou.android.pydia.ui.upload

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.sinou.android.pydia.services.AccountService

/**
 * This holds the list of connected clients to choose a target destination for uploads and moves.
 */
class PickSessionViewModel(
    accountService: AccountService,
    application: Application
) : AndroidViewModel(application) {

    val sessions = accountService.liveSessions

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
