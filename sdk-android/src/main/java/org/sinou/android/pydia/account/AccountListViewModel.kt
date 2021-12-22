package org.sinou.android.pydia.account

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.sinou.android.pydia.room.account.RAccount
import org.sinou.android.pydia.room.account.AccountDB
import org.sinou.android.pydia.room.account.RLiveSession
import org.sinou.android.pydia.room.account.RSession

/**
 * Central ViewModel when dealing with a user's accounts.
 */
class AccountListViewModel(
    val database: AccountDB,
    application: Application
) : AndroidViewModel(application) {

    private val TAG = "AccountListViewModel"

    private var viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private var activeAccount = MutableLiveData<RAccount?>()

    private val _sessions = database.liveSessionDao().getLiveSessions()
    val sessions: LiveData<List<RLiveSession>>
        get() = _sessions


//    init {
//        initializeActiveAccount()
//    }
//
//    private fun initializeActiveAccount() {
//        uiScope.launch {
//            val act = doGetActiveAccount()
//            activeAccount.value = act
//        }
//    }
//
//    private suspend fun doGetActiveAccount(): Account? {
//        return withContext(Dispatchers.IO) {
//            database.accountDao().getActiveAccount()
//        }
//    }

    class AccountListViewModelFactory(
        private val accountDB: AccountDB,
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
}
