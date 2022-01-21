package org.sinou.android.pydia.ui.auth

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
import java.net.MalformedURLException

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

    fun authLaunched() {
        _server.value = null
    }

    private fun switchLoading(newState: Boolean) {
        _isLoading.value = newState
    }

    private suspend fun doPing(serverAddress: String): ServerURL? {
        return withContext(Dispatchers.IO) {
            Log.i(tag, "Perform real ping to $serverAddress")
            var newURL: ServerURL? = null
            try {
                newURL = ServerURLImpl.fromAddress(serverAddress)
                newURL.ping()
            } catch (e: MalformedURLException) {
                updateErrorMsg(e.message ?: "Invalid address, please update")
            } catch (e: Exception) {
                updateErrorMsg(e.message ?: "Invalid address, please update")
                e.printStackTrace()
            }
            newURL
        }
    }

    private suspend fun doRegister(su: ServerURL): Server? {

        return withContext(Dispatchers.IO) {
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
