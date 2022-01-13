package org.sinou.android.pydia.upload

import android.app.Application
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.*
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sinou.android.pydia.services.NodeService
import java.io.IOException
import java.io.InputStream

/**
 * Holds the current location while choosing a target for file uploads or moves.
 */
class ChooseTargetViewModel(
    private val nodeService: NodeService,
    private val currApp: Application,
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

                val cr = currApp.contentResolver
                val mimeMap = MimeTypeMap.getSingleton()

                for (uri in uris){
                    var filename = uri.lastPathSegment!!
                    var inputStream: InputStream? = null
                    try {
                        inputStream = cr.openInputStream(uri)
                        val mime = cr.getType(uri)
                        mimeMap.getExtensionFromMimeType(mime)?.let{
                            // TODO make a better check
                            //   - retrieve file extension
                            //   - only append if the extension seems to be unvalid
                            if (!filename.endsWith(it, true)){
                                filename += ".$it"
                            }
                        }
                        val error = nodeService.uploadAt(stateID, filename, inputStream!!)
                    } catch (ioe: IOException) {
                        ioe.printStackTrace()
                    }
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
