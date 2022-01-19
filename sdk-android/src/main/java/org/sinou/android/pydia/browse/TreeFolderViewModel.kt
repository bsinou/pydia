package org.sinou.android.pydia.browse

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.*
import org.sinou.android.pydia.room.browse.RTreeNode
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.services.NodeService
import java.util.concurrent.TimeUnit

/**
 * Holds a folder and all its children
 */
class TreeFolderViewModel(
    private val accountService: AccountService,
    private val nodeService: NodeService,
    val stateID: StateID,
    application: Application
) : AndroidViewModel(application) {

    private val tag = "TreeFolderViewModel"

    private var viewModelJob = Job()

    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    lateinit private var _currentFolder: LiveData<RTreeNode>
    val currentFolder: LiveData<RTreeNode>
        get() = _currentFolder

    val children = nodeService.ls(stateID)

    private var _isActive = false

    // TODO handle network status
    private fun watchFolder() = vmScope.launch {
        while (_isActive) {
            Log.i(
                tag,
                "Watching ${stateID}, found ${children.value?.size} children"
            )
            nodeService.pull(stateID)
            delay(TimeUnit.SECONDS.toMillis(10))
        }
    }


    fun resume() {
        _isActive = true
        watchFolder()
    }

    fun pause() {
        _isActive = false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


    class TreeFolderViewModelFactory(
        private val accountService: AccountService,
        private val nodeService: NodeService,
        private val stateID: StateID,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TreeFolderViewModel::class.java)) {
                return TreeFolderViewModel(accountService, nodeService, stateID, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
