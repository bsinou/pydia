package org.sinou.android.pydia.ui.browse

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.*
import org.sinou.android.pydia.db.nodes.RTreeNode
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.services.NodeService
import org.sinou.android.pydia.utils.BackOffTicker
import java.util.concurrent.TimeUnit

/**
 * Holds a folder and all its children
 */
class BrowseFolderViewModel(
    val stateID: StateID,
    private val accountService: AccountService,
    private val nodeService: NodeService,
    application: Application
) : AndroidViewModel(application) {

    private var _currentFolder = nodeService.getLiveNode(stateID)
    val currentFolder: LiveData<RTreeNode>
        get() = _currentFolder

    val children = nodeService.ls(stateID)

    private val tag = BrowseFolderViewModel::class.simpleName
    private var viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val backOffTicker = BackOffTicker()
    private var _isActive = false
    private var currWatcher: Job? = null

    // Manage UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    init {
        setLoading(true)
    }

    private fun watchFolder() = vmScope.launch {
        while (_isActive) {
            doPull()
            val nd = backOffTicker.getNextDelay()
            delay(TimeUnit.SECONDS.toMillis(nd))
            Log.d(tag, "... Next delay: $nd")
        }
    }

    private suspend fun doPull() {
        val result = nodeService.pull(stateID)
        withContext(Dispatchers.Main) {
            if (result.second != null) {
                _errorMessage.value = result.second
                pause()
            } else if (result.first > 0) {
                backOffTicker.resetIndex()
            }
            setLoading(false)
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
        setLoading(true)
        pause()
        currWatcher?.cancel()
        resume()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    class BrowseFolderViewModelFactory(
        private val accountService: AccountService,
        private val nodeService: NodeService,
        private val stateID: StateID,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BrowseFolderViewModel::class.java)) {
                return BrowseFolderViewModel(stateID, accountService, nodeService, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
