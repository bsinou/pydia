package org.sinou.android.pydia.browse

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.*
import org.sinou.android.pydia.room.account.RLiveSession
import org.sinou.android.pydia.room.account.RSession
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.services.NodeService
import java.util.concurrent.TimeUnit

/**
 * This holds the session that is currently used in foreground for browsing the cache
 * and the remote server.
 */
class ForegroundSessionViewModel(
    val accountService: AccountService,
    val nodeService: NodeService,
    val accountID: String,
    application: Application
) : AndroidViewModel(application) {

    private val tag = "ForegroundSessionVM"

    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _liveSession = accountService.accountDB.liveSessionDao().getLiveSession(accountID)
    val liveSession: LiveData<RLiveSession?>
        get() = _liveSession

    private var _isActive = false
    val isActiveSession: Boolean
        get() = _isActive

    // TODO handle network status
    private fun watchSession() = viewModelScope.launch {
        while (isActiveSession) {
            Log.i(
                tag,
                "Watching ${accountID} - already having a session ${liveSession?.value}"
            )
            accountService.refreshWorkspaceList(accountID)
            delay(TimeUnit.SECONDS.toMillis(3))
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun setForeground(accountID: String) {
        _liveSession =  accountService.accountDB.liveSessionDao().getLiveSession(accountID)
    }

    fun resume() {
        _isActive = true
        watchSession()
    }

    fun pause() {
        _isActive = false
    }

    private fun initializeSession(accountID: String) =
        viewModelScope.launch {
            //  _currentSession.value = getSessionFromDB(accountID)
            // watchSession()
        }


    private suspend fun getSessionFromDB(accountID: String): RSession? {
        return withContext(Dispatchers.IO) {
            Log.i(tag, "Account ID: "+ accountID + ", account DB: "+ accountService.accountDB.toString())
            var session = accountService.accountDB.sessionDao().getSession(accountID)
            var liveSession = accountService.accountDB.liveSessionDao().getSession(accountID)
//            if (session?.authStatus != "online") {
//                session = null
//            }
            session
        }
    }

    init {
        initializeSession(accountID)
    }

    class ForegroundSessionViewModelFactory(
        private val accountService: AccountService,
        private val nodeService: NodeService,
        private val accountID: String,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ForegroundSessionViewModel::class.java)) {
                return ForegroundSessionViewModel(
                    accountService,
                    nodeService,
                    accountID,
                    application
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
