package org.sinou.android.pydia.ui.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.sinou.android.pydia.services.AccountService

/**
 * Central ViewModel when dealing with a user's accounts.
 */
class AccountListViewModel(
    accountService: AccountService,
    application: Application
) : AndroidViewModel(application) {

    // TODO rather implement a wrapping method in the account service
    val sessions = accountService.accountDB.liveSessionDao().getLiveSessions()

    class AccountListViewModelFactory(
        private val accountService: AccountService,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccountListViewModel::class.java)) {
                return AccountListViewModel(accountService, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
