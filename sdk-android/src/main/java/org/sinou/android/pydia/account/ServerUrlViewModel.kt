package org.sinou.android.pydia.account

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.api.SDKException
import com.pydio.cells.api.Server
import com.pydio.cells.api.ServerURL
import com.pydio.cells.transport.ClientData
import com.pydio.cells.transport.ServerURLImpl
import com.pydio.cells.transport.auth.jwt.OAuthConfig
import kotlinx.coroutines.*
import org.sinou.android.pydia.services.AccountService
import java.net.MalformedURLException
import java.util.*

/**
 * Manages the registration of a new server.
 *
 * TODO Should also be the reference for the callback OAuth states.*/
class ServerUrlViewModel(private val accountService: AccountService) : ViewModel() {

    private val TAG = "ServerUrlViewModel"

    private var viewModelJob = Job()

    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // First step, we have nothing then an address
    private val _serverAddress = MutableLiveData<String>()
    val serverAddress: LiveData<String>
        get() = _serverAddress

    // Handle TLS if necessary
    private val _skipVerify = MutableLiveData<Boolean>()
    val skipVerify: LiveData<Boolean>
        get() = _skipVerify

    // A valid server URL with TLS managed
    private val _serverUrl = MutableLiveData<ServerURL>()
    val serverUrl: LiveData<ServerURL>
        get() = _serverUrl

    // Server is a Pydio instance and has already been registered
    private val _server = MutableLiveData<Server?>()
    val server: LiveData<Server?>
        get() = _server

    // Manage UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    private val _launchOAuthIntent = MutableLiveData<Intent?>()
    val launchOAuthIntent: LiveData<Intent?>
        get() = _launchOAuthIntent

    fun authLaunched() {
        _server.value = null
        _launchOAuthIntent.value = null
    }

    fun pingAddress(serverAddress: String) {
        _serverAddress.value = serverAddress
        vmScope.launch {

            switchLoading(true)

            // First Ping the server to insure Address is valid
            // and distant server has a valid TLS configuration
            val serverURL = doPing(serverAddress)
            serverURL?.let {
                _serverUrl.value = it

                // ServerURL is OK, tries to register server
                val server = doRegister(serverURL)
                server?.let {
                    _server.value = it
                }
            }
            switchLoading(false)
        }
    }

    fun registerServer(serverURL: ServerURL) {
        vmScope.launch {
            switchLoading(true)
            val server = doRegister(serverURL)
            server?.let {
                _server.value = it
            }
            switchLoading(false)
        }
    }

    fun launchOAuthProcess(currServer: Server) {
        vmScope.launch {
            switchLoading(true)
            _launchOAuthIntent.value = doLaunchOAuthProcess(currServer)
        }
    }

    private fun switchLoading(newState: Boolean) {
        _isLoading.value = newState
    }

    private suspend fun doPing(serverAddress: String): ServerURL? {
        return withContext(Dispatchers.IO) {
            Log.i(TAG, "Perform real ping to $serverAddress")
            var newURL: ServerURL? = null
            try {
                newURL = ServerURLImpl.fromAddress(serverAddress)
                newURL.ping()
            } catch (e: MalformedURLException) {
                updateErrorMsg(e.message ?: "Invalid address, please update")
            }
            newURL
        }
    }


    private suspend fun doLaunchOAuthProcess(currServer: Server): Intent? {

        return withContext(Dispatchers.IO) {
            val cfg: OAuthConfig = currServer.getOAuthConfig()
            val oAuthState = generateOAuthState(12)
            val uri: Uri = generateUriData(cfg, oAuthState)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            // Register the state for the enable the callback
            accountService.inProcessCallbacks.put(oAuthState, currServer.serverURL)
            intent
        }
    }

    private val SEED_CHARS = "abcdef1234567890"

    private fun generateOAuthState(length: Int): String {
        val sb = StringBuilder()
        val rand = Random()
        for (i in 0 until length) {
            sb.append(SEED_CHARS.get(rand.nextInt(SEED_CHARS.length)))
        }
        return sb.toString()
    }

    fun generateUriData(cfg: OAuthConfig, state: String): Uri {
        var uriBuilder = Uri.parse(cfg.authorizeEndpoint).buildUpon()
        uriBuilder = uriBuilder.appendQueryParameter("state", state)
            .appendQueryParameter("scope", cfg.scope)
            .appendQueryParameter("client_id", ClientData.getClientId())
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", cfg.redirectURI)
        if (cfg.audience != null && "" != cfg.audience) {
            uriBuilder.appendQueryParameter("audience_id", cfg.audience)
        }
        return uriBuilder.build()
    }


    private suspend fun doRegister(su: ServerURL): Server? {

        return withContext(Dispatchers.IO) {
            Log.i(TAG, "About to register the server ${su.id}")
            var newServer: Server? = null
            try {
                newServer = accountService.sessionFactory.registerServer(su)

            } catch (e: SDKException) {
                updateErrorMsg(
                    e.message
                        ?: "This does not seem to be a Pydio server address, please double check"
                )
            }
            newServer
        }
    }

    private suspend fun updateErrorMsg(msg: String) {
        return withContext(Dispatchers.Main) {
            _errorMessage.value = msg
        }
    }

    override fun onCleared() {
        Log.i(TAG, "$TAG destroyed!")
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        Log.i(TAG, "$TAG created!")
    }

    class ServerUrlViewModelFactory(
        private val accountService: AccountService
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ServerUrlViewModel::class.java)) {
                return ServerUrlViewModel(accountService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
