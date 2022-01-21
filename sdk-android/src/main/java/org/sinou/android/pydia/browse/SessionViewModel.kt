package org.sinou.android.pydia.browse

class SessionViewModel

/*
package org.sinou.android.pydia.browse

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.*
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.room.account.RLiveSession
import java.util.concurrent.TimeUnit

*/
/**
 * Holds the session that is currently in foreground for browsing the cache
 * and the remote server.
 *//*

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val tag = "SessionViewModel"

    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val accountService = CellsApp.instance.accountService
    val nodeService = CellsApp.instance.nodeService

    private var _accountID = MutableLiveData<StateID?>()
    val activeAccountID: LiveData<StateID?>
        get() = _accountID


    val bookmarks = nodeService.listBookmarks(StateID.fromId("http://example.com"))

    private val accountsLiveData: Map<String, LiveData<RLiveSession?>> = lazyMap { accountID ->
        val liveData = accountService.accountDB.liveSessionDao().getLiveSession(accountID)
        return@lazyMap liveData
    }

    fun liveSession(accountIDStr: String): LiveData<RLiveSession?> =
        accountsLiveData.getValue(accountIDStr)

    private var _isActive = false
    val isActiveSession: Boolean
        get() = _isActive

    // TODO handle network status
    private fun watchSession() = viewModelScope.launch {
        while (isActiveSession) {
            Log.i(tag, "Watching ${_accountID.value} ")
//            accountID?.let {
//                accountService.refreshWorkspaceList(it.id)
//            }
            delay(TimeUnit.SECONDS.toMillis(3))
        }
    }

    fun setActiveAccount(accountID: StateID?) {
        // TODO free old LiveData?
        _accountID.value = accountID
    }

//    fun openSession(accountID: StateID) {
//
//        Log.e(tag, "before launch: ${accountID}")
//        Thread.dumpStack()
//
//        viewModelScope.launch {
//            pause()
//            var tmp = withContext(Dispatchers.IO) {
//                val session2 = accountService.accountDB.liveSessionDao().getSession(accountID.id)
//                if (session2 != null){
//                    Log.i(tag, "Found a session: $session2")
//                }
//                accountService.accountDB.liveSessionDao().getLiveSession(accountID.id)
//            }
//
//
//            _liveSession = tmp
//            _bookmarks = withContext(Dispatchers.IO) {
//                nodeService.listBookmarks(accountID)
//            }
//            _liveSession.value?.let {
//                resume()
//            }
//        }
//
//        Log.e(tag, "here")
//    }

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


    class SessionViewModelFactory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
                return SessionViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    fun <K, V> lazyMap(initializer: (K) -> V): Map<K, V> {
        val map = mutableMapOf<K, V>()
        return map.withDefault { key ->
            val newValue = initializer(key)
            map[key] = newValue
            return@withDefault newValue
        }
    }
}
*/
