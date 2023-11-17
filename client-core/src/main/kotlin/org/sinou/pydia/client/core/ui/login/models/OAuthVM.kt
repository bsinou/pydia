package org.sinou.pydia.client.core.ui.login.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.pydia.client.core.services.AccountService
import org.sinou.pydia.client.core.services.AuthService
import org.sinou.pydia.client.core.services.CoroutineService
import org.sinou.pydia.client.core.services.PreferencesService
import org.sinou.pydia.client.core.services.SessionFactory
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.transport.StateID

enum class OAuthProcessState {
    NEW, PROCESSING, DONE, SKIP
}

class OAuthVM(
    private val prefs: PreferencesService,
    coroutineService: CoroutineService,
    private val authService: AuthService,
    private val sessionFactory: SessionFactory,
    private val accountService: AccountService,
) : ViewModel() {

    private val logTag = "OAuthVM"
    private val ioDispatcher = coroutineService.ioDispatcher
    private val uiDispatcher = coroutineService.uiDispatcher

    private val smoothActionDelay = 2000L

    private val _processState = MutableStateFlow(OAuthProcessState.NEW)
    val processState: StateFlow<OAuthProcessState> = _processState.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _accountID = MutableStateFlow<StateID?>(null)
    private val _loginContext = MutableStateFlow<String?>(null)
    val accountID: StateFlow<StateID?> = _accountID.asStateFlow()
    val loginContext: StateFlow<String?> = _loginContext.asStateFlow()


    private val _message = MutableStateFlow("")
    val message: StateFlow<String?> = _message.asStateFlow()

    private var _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    suspend fun isAuthStateValid(state: String): Pair<Boolean, StateID> {
        return authService.isAuthStateValid(state)
    }

    fun skip(){
        _processState.value = OAuthProcessState.SKIP
    }

    /**
     * Returns the target session's account StateID and a login context when the process is successful,
     * or null otherwise
     */
    suspend fun launchCodeManagement(state: String, code: String) {
        Log.i(logTag, "Handling OAuth response")

        viewModelScope.launch {
            _processState.value = OAuthProcessState.PROCESSING
            switchLoading(true)
            updateMessage("Retrieving authentication token...")

            delay(smoothActionDelay)
            val (accID, lc) =
                authService.handleOAuthResponse(accountService, sessionFactory, state, code)
                    ?: run {// Nothing to do, we simply ignore the call
                        _processState.value = OAuthProcessState.SKIP
                        return@launch
                    }
            try {
                updateMessage("Updating account info...")
                delay(smoothActionDelay)
                accountService.refreshWorkspaceList(accID)
                _accountID.value = accID
                _loginContext.value = lc
                _processState.value = OAuthProcessState.DONE
            } catch (se: SDKException) {
                updateErrorMsg("Could not refresh workspaces for $accID: ${se.message}")
            }
        }
    }

    // UI Methods
    private fun switchLoading(newState: Boolean) {
        if (newState) { // also remove old error message when we start a new processing
            _errorMessage.value = ""
        }
        _isProcessing.value = newState
    }

    private suspend fun updateMessage(msg: String) {
        withContext(Dispatchers.Main) {
            _message.value = msg
            // or yes ?? TODO switchLoading(true)
        }
    }

    private suspend fun updateErrorMsg(msg: String) {
        withContext(Dispatchers.Main) {
            _errorMessage.value = msg
            _message.value = ""
            switchLoading(false)
        }
    }

    suspend fun resetMessages() {
        withContext(Dispatchers.Main) {
            _errorMessage.value = ""
            _message.value = ""
            switchLoading(false)
        }
    }

    override fun onCleared() {
        Log.i(logTag, "Cleared")
    }

    init {
        Log.i(logTag, "Created")
    }
}
