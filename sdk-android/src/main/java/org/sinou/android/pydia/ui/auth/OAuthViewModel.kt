package org.sinou.android.pydia.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*
import org.sinou.android.pydia.services.AccountService

/**
 * Manages retrieval of Cells credentials via the OAuth2 credentials flow.
 */
class OAuthViewModel(private val accountService: AccountService) : ViewModel() {

    private val tag = "OAuthViewModel"

    private val viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Set upon successful authentication against the remote server
    private val _accountID = MutableLiveData<Pair<String, String?>?>()
    val accountID: LiveData<Pair<String, String?>?>
        get() = _accountID

    // Manage UI
    private val _isProcessing = MutableLiveData<Boolean>().apply {
        this.value = true
    }
    val isProcessing: LiveData<Boolean>
        get() = _isProcessing

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?>
        get() = _message

    private fun switchLoading(newState: Boolean) {
        _isProcessing.value = newState
    }

    fun handleResponse(state: String, code: String) {
        vmScope.launch {
            _message.value = "Retrieving authentication token..."
            val newAccount =  withContext(Dispatchers.IO) {
                accountService.authService.handleOAuthResponse(state, code)
            } ?: run {
                _message.value = "could not retrieve token from code"
                return@launch
            }

            _message.value = "Configuring account..."
            withContext(Dispatchers.IO) {
                accountService.refreshWorkspaceList(newAccount.first)
            }

            _accountID.value = newAccount
            switchLoading(false)
        }
    }

    override fun onCleared() {
        Log.i(tag, "onCleared")
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        Log.i(tag, "created")
    }

    class OAuthFlowViewModelFactory(
        private val accountService: AccountService,
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OAuthViewModel::class.java)) {
                return OAuthViewModel(accountService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
