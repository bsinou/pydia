package org.sinou.android.pydia.ui.transfer

import android.app.Application
import androidx.lifecycle.*
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import kotlinx.coroutines.*
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.services.NodeService
import org.sinou.android.pydia.utils.BackOffTicker
import java.util.concurrent.TimeUnit

/**
 * Holds a folder and all its children folders to choose a target destination
 * for uploads and copy/moves.
 */
class PickFolderViewModel(
    val stateID: StateID,
    private val accountService: AccountService,
    private val nodeService: NodeService,
    application: Application
) : AndroidViewModel(application) {

    val children = nodeService.listChildFolders(stateID)

    // Technical local objects
    // private val tag = PickFolderViewModel::class.simpleName
    private val viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val backOffTicker = BackOffTicker()
    private var _isActive = false
    private var currWatcher: Job? = null

    // UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    init {
        _isLoading.value = true
    }

    private fun watchFolder() = vmScope.launch {
        while (_isActive) {
            doPull()
            val nd = backOffTicker.getNextDelay()
            delay(TimeUnit.SECONDS.toMillis(nd))
            //Log.d(tag, "... Next delay: $nd")
        }
    }

    private suspend fun doPull() {

        if (Str.empty(stateID.file)) {
            val err = accountService.refreshWorkspaceList(stateID.accountId)
            withContext(Dispatchers.Main) {
                if (Str.notEmpty(err)) {
                    _errorMessage.value = err
                    pause()
                }
                // TODO also detect when something has changed to reset backoff ticker
                _isLoading.value = false
            }
        } else {
            val result = nodeService.pull(stateID)
            withContext(Dispatchers.Main) {
                if (result.second != null) {
                    _errorMessage.value = result.second
                    pause()
                } else if (result.first > 0) {
                    backOffTicker.resetIndex()
                }
                _isLoading.value = false
            }
        }
    }

    fun resume() {
        if (!_isActive) {
            _isActive = true
            currWatcher = watchFolder()
        }
        backOffTicker.resetIndex()
    }

    fun pause() {
        _isActive = false
    }

    fun forceRefresh() {
        _isLoading.value = true
        pause()
        currWatcher?.cancel()
        resume()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class PickFolderViewModelFactory(
        private val stateID: StateID,
        private val accountService: AccountService,
        private val nodeService: NodeService,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PickFolderViewModel::class.java)) {
                return PickFolderViewModel(stateID, accountService, nodeService, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
