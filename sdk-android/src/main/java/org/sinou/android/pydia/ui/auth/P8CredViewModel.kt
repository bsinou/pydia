package org.sinou.android.pydia.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.api.SDKException
import com.pydio.cells.api.ServerURL
import com.pydio.cells.legacy.P8Credentials
import kotlinx.coroutines.*
import org.sinou.android.pydia.services.AccountService

/**
 * Manages retrieval of P8 credentials, optionally with a captcha.
 */
class P8CredViewModel(
    private val accountService: AccountService,
    private val serverURL: ServerURL
) : ViewModel() {

    private val TAG = "P8CredViewModel"

    private var viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Set upon successful authentication against the remote server
    private val _accountID = MutableLiveData<String?>()
    val accountID: LiveData<String?>
        get() = _accountID

    // Manage UI
    private val _isProcessing = MutableLiveData<Boolean>()
    val isProcessing: LiveData<Boolean>
        get() = _isProcessing

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    private val _launchCredValidation = MutableLiveData<Boolean>()
    val launchCredValidation: LiveData<Boolean>
        get() = _launchCredValidation

    fun launchValidation() {
        _launchCredValidation.value = true
    }

    fun validationLaunched() {
        _accountID.value = null
        _launchCredValidation.value = false
    }

    fun logToP8(login: String, password: String, captcha: String?) {
        // TODO validate passed parameters
        switchLoading(true)
        vmScope.launch {
            _errorMessage.value = doP8Auth(login, password, captcha)
            switchLoading(false)
        }
    }

    fun cancel() {
        val wasOn = isProcessing.value ?: false
        if (wasOn){
            _isProcessing.value = false
        }
        // TODO also cancel running jobs
    }

    private fun switchLoading(newState: Boolean) {
        _isProcessing.value = newState
    }

    private suspend fun doP8Auth(login: String, password: String, captcha: String?): String? {
        val creds = P8Credentials(login, password, captcha)
        var errorMsg: String? = null

        val accountIDStr = withContext(Dispatchers.IO) {
            Log.i(TAG, "Launch P8 Auth for ${creds.username}@${serverURL.url}")
            var id: String? = null
            try {
                id = accountService.registerAccount(serverURL, creds)

                accountService.refreshWorkspaceList(id)

            } catch (e: SDKException) {
                // TODO handle captcha here
                errorMsg = e.message ?: "Invalid credentials, please try again"
            }
            id
        }

        if (accountIDStr != null) {
            _accountID.value = accountIDStr
        }
        return errorMsg
    }

    override fun onCleared() {
        Log.i(TAG, "destroyed")
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        Log.i(TAG, "created")
    }

    class P8CredViewModelFactory(
        private val accountService: AccountService,
        private val serverURL: ServerURL,
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(P8CredViewModel::class.java)) {
                return P8CredViewModel(accountService, serverURL) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
