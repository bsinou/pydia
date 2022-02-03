package org.sinou.android.pydia.ui.upload

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sinou.android.pydia.services.NodeService

/**
 * Holds the current location while choosing a target for file uploads or moves.
 */
class ChooseTargetViewModel(
    private val nodeService: NodeService,
    currApp: Application,
) : AndroidViewModel(currApp) {

    private val viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var uris = mutableListOf<Uri>()

    private var _currentLocation = MutableLiveData<StateID?>()
    val currentLocation: LiveData<StateID?>
        get() = _currentLocation

    fun validTarget(): Boolean {
        return currentLocation.value?.path?.let { it.length > 1 } ?: false
    }

    fun setCurrentState(stateID: StateID?) {
        _currentLocation.value = stateID
    }

    fun launchUpload() {
        currentLocation.value?.let { stateID ->
            vmScope.launch {
                for (uri in uris) {
                    val error = nodeService.enqueueUpload(stateID, uri)
                }
            }
        }
    }

    fun initTarget(targets: List<Uri>) {
        uris.clear()
        uris.addAll(targets)
    }

    class ChooseTargetViewModelFactory(
        private val nodeService: NodeService,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChooseTargetViewModel::class.java)) {
                return ChooseTargetViewModel(nodeService, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
