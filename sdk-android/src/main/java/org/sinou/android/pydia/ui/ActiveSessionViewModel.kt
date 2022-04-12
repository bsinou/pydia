package org.sinou.android.pydia.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.db.accounts.RLiveSession
import org.sinou.android.pydia.db.accounts.RWorkspace
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.utils.BackOffTicker
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Hold the session that is currently in foreground for browsing the cache
 * and the remote server.
 */
class ActiveSessionViewModel(
    private val accountService: AccountService,
    id: String = UUID.randomUUID().toString()
) : ViewModel() {

    private val logTag = "${ActiveSessionViewModel::class.simpleName}[$id]"
    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Business objects
    private var _accountId: String? = null
    val accountId: String?
        get() = _accountId

    lateinit var liveSession: LiveData<RLiveSession?>
    lateinit var workspaces: LiveData<List<RWorkspace>>

    // Watcher states
    private var _isRunning = false
    val isRunning: Boolean
        get() = _isRunning
    private val backOffTicker = BackOffTicker()
    private var currWatcher: Job? = null

    // Manage UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    fun afterCreate(accountId: String?) {
        if (accountId != null) {
            _accountId = accountId
            Log.i(logTag, "Initialising active session for $accountId")
            liveSession = accountService.getLiveSession(accountId)
            workspaces = accountService.getLiveWorkspaces(accountId)
        } else {
            Log.e(logTag, "Initialising model with no account ID.")
        }
    }

    // TODO handle network status
    private fun watchSession() = viewModelScope.launch {
        while (_isRunning) {
            doPull()
            val nd = backOffTicker.getNextDelay()
            Log.d(logTag, "... Next delay: $nd - $accountId")
            delay(TimeUnit.SECONDS.toMillis(nd))
        }
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    private suspend fun doPull() {

        if (accountId == null || liveSession.value == null) {
            Log.w(logTag, "No live session for $accountId ")
            return
        }
        val result = accountService.refreshWorkspaceList(accountId!!)
        withContext(Dispatchers.Main) {
            if (result.second != null) { // Non-Null response is an error message
                if (backOffTicker.getCurrentIndex() > 0) {
                    // We do not display the error message if first
                    _errorMessage.value = result.second
                }
                Log.i(logTag, "Pausing poll")
                pause()
            } else if (result.first > 0) {
                backOffTicker.resetIndex()
            }
            setLoading(false)
            if (result.second == null) { // Also reset error message
                _errorMessage.value = null
            }
        }
    }

    fun resume() {
        Log.d(logTag, "resuming...")
        if (!_isRunning) {
            _isRunning = true
            currWatcher = watchSession()
        }
        backOffTicker.resetIndex()
    }

    fun forceRefresh() {
        setLoading(true)
        pause()
        currWatcher?.cancel()
        resume()
    }

    fun pause() {
        _isRunning = false
    }

    override fun onCleared() {
        super.onCleared()
        Log.e(logTag, "onCleared for $accountId")
        viewModelJob.cancel()
    }

    init {
        setLoading(true)
    }

}
