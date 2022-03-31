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

/** Holds a folder and all its children */
class BrowseFolderViewModel(
    val stateId: StateID,
    private val accountService: AccountService,
    private val nodeService: NodeService,
    application: Application
) : AndroidViewModel(application) {

    private var _currentFolder = nodeService.getLiveNode(stateId)
    val currentFolder: LiveData<RTreeNode>
        get() = _currentFolder

    private var _children = nodeService.ls(stateId)
    val children: LiveData<List<RTreeNode>>
        get() = _children

//    private var _selected : Selection<String>? = null
//    val selected : Selection<String>?
//         get() = _selected

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
            Log.d(tag, "... Next delay: $nd - $stateId")
            delay(TimeUnit.SECONDS.toMillis(nd))
        }
    }

    private suspend fun doPull() {
        val result = nodeService.pull(stateId)
        withContext(Dispatchers.Main) {
            if (result.second != null) {
                if (backOffTicker.getCurrentIndex() > 0) {
                    // Not optimal, we should rather check the current session status
                    // before launching the poll
                    // We do not display the error message if first
                    _errorMessage.value = result.second
                }
                pause()
            } else if (result.first > 0) {
                backOffTicker.resetIndex()
            }
            setLoading(false)
        }
    }

    fun resume() {
        Log.i(tag, "resumed")
        resetLiveChildren()
        if (!_isActive) {
            _isActive = true
            currWatcher = watchFolder()
        }
        backOffTicker.resetIndex()
    }

    fun pause() {
        Log.i(tag, "paused")
        _isActive = false
    }

    fun forceRefresh() {
        setLoading(true)
        pause()
        currWatcher?.cancel()
        resume()
    }

    /** Force recreation of the liveData object, typically after sort order modification */
    private fun resetLiveChildren() {
        _children = nodeService.ls(stateId)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

//    fun clearSelection(){
//        _selected = null
//    }
//
//    fun setSelection(selected: Selection<String>){
//        _selected = selected
//    }

    // To observe liveData from with the view model (even not recommended)
    // See: https://stackoverflow.com/questions/47515997/observing-livedata-from-viewmodel
//    fun start(id : Long) : LiveData<User>? {
//        val liveData = MediatorLiveData<User>()
//        liveData.addSource(dataSource.getById(id), Observer {
//            if (it != null) {
//                // put your logic here
//            }
//        })
//    }

//// Activity/Fragment
//    viewModel.start(id)?.observe(this, Observer {
//        // blank observe here
//    })

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
