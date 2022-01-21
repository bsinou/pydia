package org.sinou.android.pydia.ui.browse

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.*
import org.sinou.android.pydia.db.browse.RTreeNode
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.services.NodeService
import java.util.concurrent.TimeUnit

/**
 * Holds a folder and all its children
 */
class BrowseFolderViewModel(
    private val accountService: AccountService,
    private val nodeService: NodeService,
    val stateID: StateID,
    application: Application
) : AndroidViewModel(application) {

    private val tag = "TreeFolderViewModel"
    private var viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var _currentFolder: LiveData<RTreeNode>
    val currentFolder: LiveData<RTreeNode>
        get() = _currentFolder

    val children = nodeService.ls(stateID)

    private var _isActive = false

    private fun watchFolder() = vmScope.launch {
        while (_isActive) {
            if (accountService.isClientConnected(stateID.accountId)) {
                Log.i(tag, "Watching ${stateID}, found ${children.value?.size} children")
                nodeService.pull(stateID)?.let {
                    // Not-Null response is an error message, pause polling
                    Log.e(tag, "$it, pausing poll")
                    pause()
                }
            }
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
            if (modelClass.isAssignableFrom(BrowseFolderViewModel::class.java)) {
                return BrowseFolderViewModel(accountService, nodeService, stateID, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
