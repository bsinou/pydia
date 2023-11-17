package org.sinou.pydia.client.core.ui.login.models

import android.content.Intent
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
import org.sinou.pydia.client.core.db.accounts.RSessionView
import org.sinou.pydia.client.core.services.AccountService
import org.sinou.pydia.client.core.services.AuthService
import org.sinou.pydia.client.core.services.SessionFactory
import org.sinou.pydia.client.core.ui.login.LoginDestinations
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.Server
import org.sinou.pydia.sdk.api.ServerURL
import org.sinou.pydia.sdk.transport.ServerURLImpl
import org.sinou.pydia.sdk.transport.StateID
import java.io.IOException
import java.net.MalformedURLException
import javax.net.ssl.SSLException

class LoginVM(
    private val authService: AuthService,
    private val sessionFactory: SessionFactory,
    private val accountService: AccountService,
) : ViewModel() {

    private val logTag = "LoginVM"

    // UI
    // Add some delay for the end user to be aware of what is happening under the hood.
    // TODO remove or reduce
    private val smoothActionDelay = 750L

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String?> = _message.asStateFlow()

    private var _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Business methods
    fun flush() {
        viewModelScope.launch {
            resetMessages()
        }
    }

    /**
     * Returns the next route to navigate to or null if we want to stay on the page
     */
    suspend fun pingAddress(url: String, skipVerify: Boolean): String? {
        return processAddress(url, skipVerify).also {
            if (!it.isNullOrEmpty())
                _message.value = ""
        }
    }

    suspend fun confirmSkipVerifyAndPing(url: String): String? {
        return processAddress(url, true)
    }


    suspend fun getSessionView(accountID: StateID): RSessionView? = withContext(Dispatchers.IO) {
        accountService.getSession(accountID)
    }

    suspend fun handleOAuthResponse(state: String, code: String): Pair<StateID, String?>? {
        Log.i(logTag, "Handling OAuth response")

        switchLoading(true)
        updateMessage("Retrieving authentication token...")
        val res = withContext(Dispatchers.IO) {
            delay(smoothActionDelay)
            authService.handleOAuthResponse(accountService, sessionFactory, state, code)
        } ?: run {
            updateErrorMsg("could not retrieve token from code")
            return null
        }

        updateMessage("Updating account info...")

        val res2 = withContext(Dispatchers.IO) {
            val tmpResult = accountService.refreshWorkspaceList(res.first)
            delay(smoothActionDelay)
            tmpResult
        }

        return res2.second?.let {
            updateErrorMsg(it)
            null
        } ?: run {
            res
        }
    }

    // Internal helpers

    /** Returns the route for the next destination if we have to move to next page */
    private suspend fun processAddress(url: String, skipVerify: Boolean): String? {

        if (url.isEmpty()) {
            updateErrorMsg("Server address is empty, could not proceed")
            return null
        }
        switchLoading(true)

        // 1) Ping the server and check if:
        //   - address is valid
        //   - distant server has a valid TLS configuration
        val pingResult = doPing(url, skipVerify)
        val serverURL = pingResult.first ?: run {
//            if (skipVerify) { // ping fails also with skip verify.
//                // TODO handle the case
//            }
            // We assume we always have the skip verify route here
            return pingResult.second
        }

        Log.e(logTag, "after ping, server URL: $serverURL")

        // ServerURL is OK aka 200 at given URL with correct cert
        updateMessage("Address and cert are valid. Registering server...")

        //  2) Register the server locally
        val server = doRegister(serverURL)
            ?: // Error messages and states are handled above
            return null

        val serverID = StateID(server.url())
        // 3) Specific login process depending on the remote server type (Cells or P8).
        switchLoading(false)
        // FIXME this is not satisfying: error won't be processed correctly
        return LoginDestinations.LaunchAuthProcessing.createRoute(
            serverID,
            skipVerify,
            AuthService.LOGIN_CONTEXT_CREATE
        )
    }

    /**
     * Returns a ServerURL if the ping is successful or a route to navigate to the skip verify step if we got a SSL exception
     * All exception are handled locally and we should never be further thrown
     */
   private suspend fun doPing(
        serverAddress: String,
        skipVerify: Boolean
    ): Pair<ServerURL?, String?> {
        return withContext(Dispatchers.IO) {
            Log.i(logTag, "... About to ping $serverAddress")
            val tmpURL: ServerURL?
            var newURL: ServerURL? = null
            try {
                tmpURL = ServerURLImpl.fromAddress(serverAddress, skipVerify)
                tmpURL.ping()
                newURL = tmpURL
            } catch (e: MalformedURLException) {
                Log.e(logTag, "Invalid address: [$serverAddress]. Cause:  ${e.message} ")
                updateErrorMsg(e.message ?: "Invalid address, please update")
            } catch (e: SSLException) {
                updateErrorMsg("Invalid certificate for $serverAddress")
                Log.e(logTag, "Invalid certificate for $serverAddress: ${e.message}")
                // We might do this better with exceptions once with have a valid strategy for error handling with coroutines
                return@withContext null to LoginDestinations.SkipVerify.createRoute(
                    StateID(
                        serverAddress
                    )
                )
            } catch (e: IOException) {
                updateErrorMsg("Unexpected IOException: ${e.message}")
                e.printStackTrace()
            } catch (e: Exception) {
                updateErrorMsg("Unexpected error for $serverAddress: ${e.message}")
                e.printStackTrace()
            }
            Pair(newURL, null)
        }
    }

    private suspend fun doRegister(su: ServerURL): Server? {
        return try {
            val newServer = withContext(Dispatchers.IO) {
                sessionFactory.registerServer(su)
            }
            newServer
        } catch (e: SDKException) {
            val msg = "${su.url.host} does not seem to be a Pydio server"
            updateErrorMsg("$msg. Please, double-check.")
            Log.e(logTag, "$msg - Err.${e.code}: ${e.message}")
            // TODO double check
            // navigateBack()
            // TODO setCurrentStep(LoginStep.URL)
            null
        }
    }

    suspend fun newOAuthIntent(serverURL: ServerURL, loginContext: String): Intent? =
        withContext(Dispatchers.Main) {
            updateMessage("Launching OAuth credential flow")
            val uri = try {
                authService.generateOAuthFlowUri(
                    sessionFactory,
                    serverURL,
                    loginContext,
                )

            } catch (e: SDKException) {
                val msg =
                    "Cannot get uri for ${serverURL.url.host}, cause: ${e.code} - ${e.message}"
                Log.e(logTag, msg)
                updateErrorMsg(msg)
                return@withContext null
            }
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY

            Log.e(logTag, "Intent created: ${intent.data}")
            return@withContext intent
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
        super.onCleared()
    }

    init {
        Log.i(logTag, "Created")
    }
}
