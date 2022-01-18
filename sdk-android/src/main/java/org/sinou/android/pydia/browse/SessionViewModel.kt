package org.sinou.android.pydia.browse

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.*
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.room.account.RSession
import java.util.concurrent.TimeUnit

/**
 * Holds the session that is currently in foreground for browsing the cache
 * and the remote server.
 */
class SessionViewModel(
    val accountID: StateID,
    application: Application
) : AndroidViewModel(application) {

    private val tag = "SessionViewModel"

    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val accountService = CellsApp.instance.accountService
    val nodeService = CellsApp.instance.nodeService

    // Exposed liveData
    val liveSession = accountService.accountDB.liveSessionDao().getLiveSession(accountID.id)
    val bookmarks = nodeService.listBookmarks(accountID)

//    lateinit var accountService: AccountService
//    lateinit var nodeService: NodeService

    //    lateinit var liveSession : LiveData<RLiveSession?>
//    lateinit var bookmarks: LiveData<List<RTreeNode>>
//
//    init {
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                accountService = CellsApp.instance.accountService
//                nodeService = CellsApp.instance.nodeService
//            }
//            liveSession = accountService.accountDB.liveSessionDao().getLiveSession(accountID.id)
//            nodeService.listBookmarks(accountID)
//            Log.i(tag, "... Initialisation done ")
//        }
//    }

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
            accountService.refreshWorkspaceList(accountID.id)
            delay(TimeUnit.SECONDS.toMillis(3))
        }
    }


    fun resume() {
        _isActive = true
        watchSession()
    }

    fun pause() {
        _isActive = false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        initializeSession(accountID.id)
    }

    private fun initializeSession(accountID: String) =
        viewModelScope.launch {
            //  _currentSession.value = getSessionFromDB(accountID)
            // watchSession()
        }

    private suspend fun getSessionFromDB(accountID: String): RSession? {
        return withContext(Dispatchers.IO) {
            Log.i(
                tag,
                "Account ID: " + accountID + ", account DB: " + accountService.accountDB.toString()
            )
            var session = accountService.accountDB.sessionDao().getSession(accountID)
            var liveSession = accountService.accountDB.liveSessionDao().getSession(accountID)
//            if (session?.authStatus != "online") {
//                session = null
//            }
            session
        }
    }

    class SessionViewModelFactory(
        private val accountID: StateID,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
                return SessionViewModel(accountID, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
