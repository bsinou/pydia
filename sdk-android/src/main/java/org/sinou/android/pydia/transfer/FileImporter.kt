package org.sinou.android.pydia.transfer

import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp

class FileImporter(private val registry: ActivityResultRegistry) : DefaultLifecycleObserver {

    private val getContentKey = AppNames.KEY_PREFIX_ + "select.files"
    private val takePictureKey = AppNames.KEY_PREFIX_ + "take.picture"

    lateinit var getMultipleContents: ActivityResultLauncher<String>
    lateinit var takePicture: ActivityResultLauncher<Uri>

    override fun onCreate(owner: LifecycleOwner) {
        getMultipleContents = registry.register(
            getContentKey,
            owner,
            ActivityResultContracts.GetMultipleContents()
        )
        { uri ->
            Log.i("MyLifeCycleObserver", "Received file at $uri")
        }

        takePicture = registry.register(
            takePictureKey,
            owner,
            ActivityResultContracts.TakePicture()
        )
        { uri ->
            Log.i("MyLifeCycleObserver", "Received file at $uri")
        }

    }

    fun selectFiles() {
        getMultipleContents.launch("*/*")
    }

    fun getFromCamera() {
        takePicture.launch(Uri.fromFile(CellsApp.instance.filesDir))
    }
}
