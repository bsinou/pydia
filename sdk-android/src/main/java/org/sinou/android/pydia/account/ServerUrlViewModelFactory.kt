package org.sinou.android.pydia.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.sinou.android.pydia.services.AccountRepository

class ServerUrlViewModelFactory(
    private val accountRepository: AccountRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServerUrlViewModel::class.java)) {
            return ServerUrlViewModel(accountRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
