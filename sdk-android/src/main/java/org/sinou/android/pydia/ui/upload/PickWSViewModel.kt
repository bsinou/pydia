package org.sinou.android.pydia.ui.upload

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.sinou.android.pydia.services.AccountService

/**
 * This holds the list of workspaces to choose a target destination for uploads and moves.
 */
class PickWSViewModel(
    private val accountService: AccountService,
    private val stateID: StateID,
    application: Application
) : AndroidViewModel(application) {

    private val tag = "PickWSViewModel"

    private var viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val liveSession = accountService.accountDB.liveSessionDao().getLiveSession(stateID.accountId)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class TargetWorkspaceViewModelFactory(
        private val accountService: AccountService,
        private val stateID: StateID,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PickWSViewModel::class.java)) {
                return PickWSViewModel(accountService, stateID, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
