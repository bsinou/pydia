package org.sinou.android.pydia.ui.upload

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.*
import org.sinou.android.pydia.services.NodeService

/**
 * This holds a folder and all its children folders to choose a target destination
 * for uploads and moves.
 */
class PickFolderViewModel(
    val stateID: StateID,
    private val nodeService: NodeService,
    application: Application
) : AndroidViewModel(application) {

    private val tag = PickFolderViewModel::class.simpleName
    private val viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val children = nodeService.listChildFolders(stateID)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    fun forceRefresh() {
        _isLoading.value = true
        vmScope.launch {
            nodeService.pull(stateID)?.let {
                // Not-Null response is an error message, pause polling
                Log.e(tag, "Could not refresh folder at $stateID: $it")
            }
            withContext(Dispatchers.Main) {
                _isLoading.value = false
            }
        }
    }

    class PickFolderViewModelFactory(
        private val stateID: StateID,
        private val nodeService: NodeService,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PickFolderViewModel::class.java)) {
                return PickFolderViewModel(stateID, nodeService, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
