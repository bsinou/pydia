package org.sinou.android.pydia.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*
import org.sinou.android.pydia.db.accounts.RLiveSession
import org.sinou.android.pydia.db.accounts.RWorkspace
import org.sinou.android.pydia.services.AccountService
import java.util.concurrent.TimeUnit

/**
 * Holds the session that is currently in foreground for browsing the cache
 * and the remote server.
 * This expects that the accountService has already been initialized.
 */

class ActiveSessionViewModel(
    private val accountService: AccountService,
    val accountId: String?,
    application: Application
) : AndroidViewModel(application) {

    private val tag = "ActiveSessionViewModel"

    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    lateinit var liveSession: LiveData<RLiveSession?>
    lateinit var workspaces: LiveData<List<RWorkspace>>

    private var _isRunning = false
    val isRunning: Boolean
        get() = _isRunning

    init {
        if (accountId != null ){
            Log.i(tag, "Initialising active session for $accountId")
            liveSession = accountService.getLiveSession(accountId)
            workspaces = accountService.getLiveWorkspaces(accountId)
        } else {
            Log.e(tag, "Initialising model with no account ID.")
        }
    }

    // TODO handle network status
    private fun watchSession() = viewModelScope.launch {
        while (isRunning) {

            if (liveSession.value == null){
                Log.w(tag, "No live session for $accountId ")
             } else {
                Log.i(tag, "Watching ${liveSession.value!!.accountID} ")
            }

            liveSession.value?.let { liveSession ->
                accountService.refreshWorkspaceList(liveSession.accountID)?.let {
                    // Not-Null response is an error message, pause polling
                    Log.e(tag, "$it, pausing poll")
                    pause()
                }
            }
            delay(TimeUnit.SECONDS.toMillis(10))
        }
    }

    fun resume() {
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
        Log.e(tag, "onCleared for $accountId")
        viewModelJob.cancel()
    }

    class ActiveSessionViewModelFactory(
        private val accountService: AccountService,
        private val accountId: String?,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ActiveSessionViewModel::class.java)) {
                return ActiveSessionViewModel(accountService, accountId, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
