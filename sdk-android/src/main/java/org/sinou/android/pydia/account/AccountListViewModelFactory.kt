package org.sinou.android.pydia.account

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.sinou.android.pydia.room.account.AccountDao
import org.sinou.android.pydia.room.account.AccountDatabase

/** Creates a new view model that has a reference to the AccountDB**/
class AccountListViewModelFactory(
    private val accountDB: AccountDatabase,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountListViewModel::class.java)) {
            return AccountListViewModel(accountDB, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
