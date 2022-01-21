package org.sinou.android.pydia.ui.auth

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.api.ServerURL
import com.pydio.cells.transport.ClientData
import com.pydio.cells.transport.StateID
import com.pydio.cells.transport.auth.jwt.OAuthConfig
import kotlinx.coroutines.*
import org.sinou.android.pydia.services.AccountService
import java.util.*

/**
 * Manages retrieval of Cells credentials via the OAuth2 credentials flow.
 */
class OAuthViewModel(private val accountService: AccountService) : ViewModel() {

    private val tag = "OAuthViewModel"

    private val viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _launchOAuthIntent = MutableLiveData<Intent?>()
    val launchOAuthIntent: LiveData<Intent?>
        get() = _launchOAuthIntent

    fun authLaunched() {
        _launchOAuthIntent.value = null
    }

    // Set upon successful authentication against the remote server
    private val _accountID = MutableLiveData<String?>()
    val accountID: LiveData<String?>
        get() = _accountID

    // Manage UI
    private val _isProcessing = MutableLiveData<Boolean>().apply {
        this.value = false
    }
    val isProcessing: LiveData<Boolean>
        get() = _isProcessing

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    private fun switchLoading(newState: Boolean) {
        _isProcessing.value = newState
    }

    fun launchOAuthProcess(serverURL: ServerURL) {
        vmScope.launch {
            switchLoading(true)
            _launchOAuthIntent.value = doLaunchOAuth(serverURL)
        }
    }

    fun handleResponse(state: String, code: String) {
        vmScope.launch {
            switchLoading(true)
            _accountID.value = withContext(Dispatchers.IO) {
                accountService.handleOAuthResponse(state, code)
            }
            switchLoading(false)
        }
    }

    private suspend fun doLaunchOAuth(url: ServerURL): Intent? = withContext(Dispatchers.IO) {
            val serverID = StateID(url.id).id
            val server = accountService.sessionFactory.getServer(serverID)
                ?: return@withContext null

            val oAuthState = generateOAuthState()
            val uri: Uri = generateUriData(server.oAuthConfig, oAuthState)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            // Register the state for the enable the callback
            accountService.inProcessCallbacks[oAuthState] = url
            intent
        }


    private fun generateUriData(cfg: OAuthConfig, state: String): Uri {
        var uriBuilder = Uri.parse(cfg.authorizeEndpoint).buildUpon()
        uriBuilder = uriBuilder.appendQueryParameter("state", state)
            .appendQueryParameter("scope", cfg.scope)
            .appendQueryParameter("client_id", ClientData.getInstance().clientId)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", cfg.redirectURI)
        if (cfg.audience != null && "" != cfg.audience) {
            uriBuilder.appendQueryParameter("audience_id", cfg.audience)
        }
        return uriBuilder.build()
    }

    private val seedChars = "abcdef1234567890"

    private fun generateOAuthState(): String {
        val sb = StringBuilder()
        val rand = Random()
        for (i in 0..12) {
            sb.append(seedChars[rand.nextInt(seedChars.length)])
        }
        return sb.toString()
    }

    override fun onCleared() {
        Log.i(tag, "destroyed")
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        Log.i(tag, "created")
        switchLoading(false)
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
