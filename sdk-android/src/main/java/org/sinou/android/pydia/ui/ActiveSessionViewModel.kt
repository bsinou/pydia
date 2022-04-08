package org.sinou.android.pydia.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sinou.android.pydia.db.accounts.RLiveSession
import org.sinou.android.pydia.db.accounts.RWorkspace
import org.sinou.android.pydia.services.AccountService
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

    private var _accountId: String? = null
    val accountId: String?
        get() = _accountId

    lateinit var liveSession: LiveData<RLiveSession?>
    lateinit var workspaces: LiveData<List<RWorkspace>>

    private var _isRunning = false
    val isRunning: Boolean
        get() = _isRunning

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
        while (isRunning) {

            if (liveSession.value == null) {
                Log.w(logTag, "No live session for $accountId ")
            } else {
                Log.i(logTag, "Watching ${liveSession.value!!.accountID} ")
            }

            liveSession.value?.let { liveSession ->
                accountService.refreshWorkspaceList(liveSession.accountID)?.let {
                    // Not-Null response is an error message, pause polling
                    Log.e(logTag, "$it, pausing poll")
                    pause()
                }
            }
            delay(TimeUnit.SECONDS.toMillis(10))
        }
    }

    fun resume() {
        Log.d(logTag, "resuming...")
        // TODO check: active session is generally set after resume is called for the first time.
        //  How do we handle the null case
        _isRunning = true
        watchSession()
    }

    fun pause() {
        _isRunning = false
    }

    override fun onCleared() {
        super.onCleared()
        Log.e(logTag, "onCleared for $accountId")
        viewModelJob.cancel()
    }
}
