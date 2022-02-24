package org.sinou.android.pydia.ui.upload

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.*
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.MainActivity
import org.sinou.android.pydia.services.NodeService

/**
 * Holds the current location while choosing a target for file uploads or moves.
 */
class ChooseTargetViewModel(
    private val nodeService: NodeService,
    currApp: Application,
) : AndroidViewModel(currApp) {

    // Internal Helpers
    private val tag = ChooseTargetViewModel::class.simpleName
    private val viewModelJob = Job()
    private val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Context
    private var _actionContext: String? = null
    val actionContext: String
        get() = _actionContext ?: AppNames.ACTION_UPLOAD

    private var uris = mutableListOf<Uri>()

    // Runtime
    private var _currentLocation = MutableLiveData<StateID?>()
    val currentLocation: LiveData<StateID?>
        get() = _currentLocation

    // Flags to trigger termination of the ChooseTarget activity:
    // Either we have an intent or we set the postDone flag
    private val _postIntent = MutableLiveData<Intent?>()
    val postIntent: LiveData<Intent?>
        get() = _postIntent

    private var _postDone = MutableLiveData<Boolean>()
    val postDone: LiveData<Boolean>
        get() = _postDone

    fun setCurrentState(stateID: StateID?) {
        _currentLocation.value = stateID
    }

    fun isTargetValid(): Boolean {
        val valid = currentLocation.value?.path?.let { it.length > 1 } ?: false
        return valid
    }

    fun launchPost(context: Context) {
        currentLocation.value?.let { stateID ->
            vmScope.launch {
                when (_actionContext) {
                    AppNames.ACTION_COPY -> {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.action = AppNames.ACTION_CHOOSE_TARGET
                        intent.putExtra(AppNames.EXTRA_STATE, stateID.id)
                        withContext(Dispatchers.Main) {
                            _postIntent.value = intent
                        }
                    }
                    AppNames.ACTION_MOVE -> {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.action = AppNames.ACTION_CHOOSE_TARGET
                        intent.putExtra(AppNames.EXTRA_STATE, stateID.id)
                        withContext(Dispatchers.Main) {
                            _postIntent.value = intent
                        }
                    }
                    AppNames.ACTION_UPLOAD -> {
                        for (uri in uris) {
                            // TODO implement error management
                            val error = nodeService.enqueueUpload(stateID, uri)
                        }
                        withContext(Dispatchers.Main) {
                            _postDone.value = true
                        }
                    }
                    else -> Log.e(tag, "Unexpected action context: $_actionContext")
                }
            }
        }
    }

    fun setActionContext(actionContext: String) {
        this._actionContext = actionContext
    }

    fun initUploadAction(targets: List<Uri>) {
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
