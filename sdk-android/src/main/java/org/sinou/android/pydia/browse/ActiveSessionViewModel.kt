package org.sinou.android.pydia.browse

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.*
import org.sinou.android.pydia.CellsApp
import java.util.concurrent.TimeUnit

/**
 * Holds the session that is currently in foreground for browsing the cache
 * and the remote server.
 * This expects that the accountService has already been initialized.
 */
class ActiveSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val tag = "ActiveSessionViewModel"

    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val accountService = CellsApp.instance.accountService
    val activeSession = accountService.activeSession

    private var _isRunning = false
    val isRunning: Boolean
        get() = _isRunning

    // TODO handle network status
    private fun watchSession() = viewModelScope.launch {
        while (isRunning) {
            Log.i(tag, "Watching ${activeSession.value} ")
            activeSession.value?.let {
                accountService.refreshWorkspaceList(it.accountID)?.let {
                    // Not-Null response is an error message, pause polling
                    Log.e(tag, "$it, pausing poll")
                    pause()
                }
            }
            delay(TimeUnit.SECONDS.toMillis(3))
        }
    }

    fun resume() {
        if (activeSession.value == null) {
            return
        }
        _isRunning = true
        watchSession()
    }

    fun pause() {
        _isRunning = false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


//    class ActiveSessionViewModelFactory(
//        private val application: Application
//    ) : ViewModelProvider.Factory {
//        @Suppress("unchecked_cast")
//        override fun <T : ViewModel> create(modelClass: Class<T>): T {
//            if (modelClass.isAssignableFrom(ActiveSessionViewModel::class.java)) {
//                return ActiveSessionViewModel(application) as T
//            }
//            throw IllegalArgumentException("Unknown ViewModel class")
//        }
//    }
}
