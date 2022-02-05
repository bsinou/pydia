package org.sinou.android.pydia.transfer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.services.FileService
import org.sinou.android.pydia.services.NodeService
import org.sinou.android.pydia.ui.browse.TreeNodeMenuViewModel
import org.sinou.android.pydia.utils.DEFAULT_FILE_PROVIDER_ID
import java.io.File
import java.io.IOException

class FileImporter(
    private val registry: ActivityResultRegistry,
    private val fileService: FileService,
    private val nodeService: NodeService,
    private val nodeMenuVM: TreeNodeMenuViewModel,
    private val caller: String,
    private val callingFragment: BottomSheetDialogFragment,
) : DefaultLifecycleObserver {

    private val tag = "FileImporter"
    private val getContentKey = AppNames.KEY_PREFIX_ + "select.files"
    private val takePictureKey = AppNames.KEY_PREFIX_ + "take.picture"

    private lateinit var getMultipleContents: ActivityResultLauncher<String>
    private lateinit var takePicture: ActivityResultLauncher<Uri>

    override fun onCreate(owner: LifecycleOwner) {
        getMultipleContents = registry.register(
            getContentKey,
            owner,
            ActivityResultContracts.GetMultipleContents()
        )
        { uris ->
            for (uri in uris) {
                nodeService.enqueueUpload(nodeMenuVM.stateID, uri)
            }
            callingFragment.dismiss()
        }

        takePicture = registry.register(takePictureKey, owner, TakePictureToInternalStorage())
        { pictureTaken ->
            if (!pictureTaken) {
                // Does not work...
                // Toast.makeText(callingFragment.requireContext(), "blah", Toast.LENGTH_LONG).show()
                // showLongMessage(callingFragment.requireActivity().baseContext, "Operation aborted by user")
                callingFragment.dismiss()
            } else {
                nodeMenuVM.targetUri?.let {
                    nodeService.enqueueUpload(nodeMenuVM.stateID, it)
                    callingFragment.dismiss()
                }
            }
        }
    }

    fun selectFiles() { // we do not assume any specific type
        getMultipleContents.launch("*/*")
    }

    suspend fun takePicture(stateID: StateID) = withContext(Dispatchers.IO) {
        doTakePicture(stateID)
    }

    private fun doTakePicture(stateID: StateID) {
        val photoFile: File? = try {
            fileService.createImageFile(stateID)
        } catch (ex: IOException) {
            Log.e(tag, "Cannot create photo file")
            ex.printStackTrace()
            // Error occurred while creating the File
            null
        }
        // Continue only if the File was successfully created
        photoFile?.also {
            val uri: Uri = FileProvider.getUriForFile(
                callingFragment.requireContext(),
                DEFAULT_FILE_PROVIDER_ID,
                it
            )
            nodeMenuVM.prepareImport(uri)
            takePicture.launch(uri)
        }
    }
}

private class TakePictureToInternalStorage : ActivityResultContracts.TakePicture() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return super.createIntent(context, input).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }
}
