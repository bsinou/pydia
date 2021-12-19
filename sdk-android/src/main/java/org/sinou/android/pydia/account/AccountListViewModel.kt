package org.sinou.android.pydia.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import org.sinou.android.pydia.room.account.Account
import org.sinou.android.pydia.room.account.AccountDatabase

/**
 * Central ViewModel when dealing with a user's accounts.
 */
class AccountListViewModel(
    val database: AccountDatabase,
    application: Application
) : AndroidViewModel(application) {

    private val TAG = "AccountListViewModel"

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var activeAccount = MutableLiveData<Account?>()

    private val _accounts = database.accountDao().getAllAccounts()
    val accounts: LiveData<List<Account>>
        get() = _accounts

    init {
        initializeActiveAccount()
    }

    private fun initializeActiveAccount() {
        uiScope.launch {
            val act = doGetActiveAccount()
            activeAccount.value = act
        }
    }

    private suspend fun doGetActiveAccount(): Account? {
        return withContext(Dispatchers.IO) {
            database.accountDao().getActiveAccount()
        }
    }

}
