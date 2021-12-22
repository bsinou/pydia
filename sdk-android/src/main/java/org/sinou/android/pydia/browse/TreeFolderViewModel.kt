package org.sinou.android.pydia.browse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sinou.android.pydia.room.browse.TreeNode
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.services.NodeService

/**
 * This holds a folder and all its children
 */
class TreeFolderViewModel(
    val accountService: AccountService,
//    val nodeService: NodeService,
//    stateID: StateID,
    application: Application
) : AndroidViewModel(application) {

    private val TAG = "TreeFolderViewModel"

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    lateinit private var _currentFolder: LiveData<TreeNode>
    val currentFolder: LiveData<TreeNode>
        get() = _currentFolder

    lateinit var children: LiveData<List<TreeNode>>
//private
    //    val children: LiveData<List<TreeNode>>
//        get() = _children


    // TODO handle network status

    /*
    private val _accounts = database.accountDao().getAllAccounts()
    val accounts: LiveData<List<Account>>
        get() = _accounts
*/

    fun watch(stateID: StateID) {
        initializeActiveSession(stateID)
    }

    private fun initializeActiveSession(stateID: StateID) {
        uiScope.launch {

            // children = nodeService.ls(stateID)
        }
    }

    class TreeFolderViewModelFactory(
        private val accountService: AccountService,
        private val nodeService: NodeService,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TreeFolderViewModel::class.java)) {
                return TreeFolderViewModel(accountService, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
