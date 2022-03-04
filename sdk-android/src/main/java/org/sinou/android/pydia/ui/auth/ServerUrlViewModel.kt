package org.sinou.android.pydia.ui.auth

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.api.SDKException
import com.pydio.cells.api.Server
import com.pydio.cells.api.ServerURL
import com.pydio.cells.transport.ServerURLImpl
import kotlinx.coroutines.*
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.services.AuthService
import java.io.IOException
import java.net.MalformedURLException
import javax.net.ssl.SSLException

/**
 * Manages the declaration of a new server, by:
 * - checking existence of the target URL
 * - validating TLS status
 * - retrieving server type (Cells or P8)
 */
class ServerUrlViewModel(private val accountService: AccountService) : ViewModel() {

    private val tag = "ServerUrlViewModel"

    private var viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // First step, we have nothing then an address
    private val _serverAddress = MutableLiveData<String>()
    val serverAddress: LiveData<String>
        get() = _serverAddress

    // Handle TLS if necessary
    private val _skipVerify = MutableLiveData<Boolean>()
    private val _unvalidTLS = MutableLiveData<Boolean>()
    val unvalidTLS: LiveData<Boolean>
        get() = _unvalidTLS

    // A valid server URL with TLS managed
    private val _serverUrl = MutableLiveData<ServerURL>()
    val serverUrl: LiveData<ServerURL>
        get() = _serverUrl

    // Server is a Pydio instance and has already been registered
    private val _server = MutableLiveData<Server?>()
    val server: LiveData<Server?>
        get() = _server

    // Temporary intent to launch external OAuth Process
    private val _nextIntent = MutableLiveData<Intent?>()
    val nextIntent: LiveData<Intent?>
        get() = _nextIntent

    // Manage UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

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
        }
    }

    fun authLaunched() {
        // rather reset this when the launch has been canceled and the user modifies the URL
        // _server.value = null
        switchLoading(false)
    }

    fun intentStarted() {
        _nextIntent.value = null
    }

    private suspend fun doPing(serverAddress: String): ServerURL? {
        return withContext(Dispatchers.IO) {
            Log.i(tag, "Perform real ping to $serverAddress")
            var newURL: ServerURL? = null
            try {
                val tmp = _skipVerify.value?.let { it } ?: false
                newURL = ServerURLImpl.fromAddress(serverAddress, tmp)
                newURL.ping()
            } catch (e: MalformedURLException) {
                updateErrorMsg(e.message ?: "Invalid address, please update")
            } catch (e: SSLException) {
                updateErrorMsg("Invalid certificate for $serverAddress")
                withContext(Dispatchers.Main) {
                    _skipVerify.value = false
                    _unvalidTLS.value = true
                }
                return@withContext null
            } catch (e: IOException) {
                updateErrorMsg(e.message ?: "IOException:")
                e.printStackTrace()
            } catch (e: Exception) {
                updateErrorMsg(e.message ?: "Invalid address, please update")
                e.printStackTrace()
            }
            newURL
        }
    }

    fun confirmTlsValidationSkip(doSkip: Boolean) {
        if (doSkip) {
            _skipVerify.value = true
            serverAddress.value?.let { pingAddress(it) }
        } else {
            // cancel server and give the user the possibility to enter another address
        }
    }

    private suspend fun doRegister(su: ServerURL): Server? = withContext(Dispatchers.IO) {
        Log.i(tag, "About to register the server ${su.id}")
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

    fun launchOAuthProcess(serverURL: ServerURL) {
        vmScope.launch {
            _nextIntent.value = accountService.authService.createOAuthIntent(
                accountService,
                serverURL,
                AuthService.NEXT_ACTION_BROWSE
            )
            switchLoading(false)
        }
    }

    private fun switchLoading(newState: Boolean) {
        _isLoading.value = newState
    }

    private suspend fun updateErrorMsg(msg: String) {
        return withContext(Dispatchers.Main) {
            _errorMessage.value = msg
        }
    }

    override fun onCleared() {
        Log.i(tag, "$tag destroyed!")
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        Log.i(tag, "Created")
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
