package org.sinou.android.pydia.data

class AccountViewModelTest

/*
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlin.coroutines.CoroutineContext

class AccountViewModel (application: Application) : AndroidViewModel(application){

    private val accountRepository : AccountRepository = TODO()
    val accounts : LiveData<List<Account>>

    private var job = Job()

    private val coroutineContext: CoroutineContext get() = job + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    init {
            val accountDao = AccountDatabase.getDatabase(application).accountDao()
            accountRepository = AccountRepository(accountDao)
            accounts = accountRepository.accounts    }
        fun insert(todo: Account) = scope.launch(Dispatchers.IO) {
            accountRepository.insert(todo)
        }
        override fun onCleared() {
            super.onCleared()
            job.cancel()
        }
    }
*/
