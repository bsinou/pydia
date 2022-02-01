package org.sinou.android.pydia.transfer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.services.NodeService

class FileImporter(
    private val registry: ActivityResultRegistry,
    private val nodeService: NodeService,
    private val parentID: StateID?,
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
                parentID?.let {
                    Log.i(caller, "Received file at $uri")
                    nodeService.enqueueUpload(it, uri)
                    callingFragment.dismiss()
                } ?: run {
                    Log.w(caller, "Received file at $uri with **no** parent stateID")
                }
            }
        }

        takePicture = registry.register(
            takePictureKey,
            owner,
            TakeMyPicture()
        ) { pictureTaken ->
            Log.i(tag, "Received **Picture** file:  $pictureTaken")

//            parentID?.let {
//                Log.i(caller, "Received file at $uri")
//                nodeService.enqueueUpload(it, uri!!)
//                callingFragment.dismiss()
//            } ?: run {
//                Log.w(caller, "Received file at $uri with **no** parent stateID")
//            }
        }

    }

    fun selectFiles() {
        getMultipleContents.launch("*/*")
    }

    fun getFromCamera(uri: Uri) {
        // Uri.fromFile(CellsApp.instance.externalMediaDirs[0])
//        takePicture.launch(null)
        takePicture.launch(uri)
    }
}


private class TakeMyPicture : ActivityResultContracts.TakePicture() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return super.createIntent(context, input).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }
}