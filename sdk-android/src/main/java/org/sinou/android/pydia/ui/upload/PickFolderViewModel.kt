package org.sinou.android.pydia.ui.upload

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.services.NodeService

/**
 * This holds a folder and all its children folders to choose a target destination
 * for uploads and moves.
 */
class PickFolderViewModel(
    nodeService: NodeService,
    val stateID: StateID,
    application: Application
) : AndroidViewModel(application) {

    val children = nodeService.listChildFolders(stateID)

    class TargetFolderViewModelFactory(
        private val nodeService: NodeService,
        private val stateID: StateID,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PickFolderViewModel::class.java)) {
                return PickFolderViewModel(nodeService, stateID, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
