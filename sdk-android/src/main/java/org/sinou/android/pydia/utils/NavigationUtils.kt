package org.sinou.android.pydia.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import com.pydio.cells.api.SdkNames
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import org.sinou.android.pydia.BuildConfig
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.db.nodes.RTreeNode
import java.io.File


const val DEFAULT_FILE_PROVIDER_SUFFIX = ".fileprovider"
const val DEFAULT_FILE_PROVIDER_ID =
    BuildConfig.APPLICATION_ID + DEFAULT_FILE_PROVIDER_SUFFIX

fun showMessage(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun showLongMessage(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}


/**
 * Open current file with the viewer provided by Android OS.
 * Thanks to https://stackoverflow.com/questions/56598480/couldnt-find-meta-data-for-provider-with-authority
 */
fun externallyView(context: Context, file: File, node: RTreeNode): Intent {
    val uri = FileProvider.getUriForFile(context, DEFAULT_FILE_PROVIDER_ID, file)
    var mime = node.mime

    if (Str.notEmpty(mime)) {
        // FIXME This seems to be a bug in the upstream layers (the SDK or even the Cells API)
        //    mime type is surrounded by unnecessary double quotes that break the view intent
//        Log.d("NavUtils", "... mime before: $mime")
        if (mime.startsWith("\"")) {
            mime = mime.substring(1)
        }
        if (mime.endsWith("\"")) {
            mime = mime.substring(0, mime.length - 1)
        }
//        Log.d("NavUtils", "... mime after: $mime")
    }

//    if (SdkNames.NODE_MIME_DEFAULT == mime) {
//        Log.e("NavUtils", "... got a default mime type for: ${node.name}")
//        mime = getMimeType(node.name)
//    }
    // First approach fails when name has spaces or quotes...
    if (SdkNames.NODE_MIME_DEFAULT == mime) {
        Log.d("NavUtils", "... Last chance with the content resolver")
        val cr = CellsApp.instance.applicationContext.contentResolver
        mime = cr.getType(uri) ?: SdkNames.NODE_MIME_DEFAULT
    }

    Log.d("NavUtils", "....Creating an intent with mime: $mime")

    // TODO
    // FIXME to insure the user can choose the opening app, see: https://developer.android.com/guide/components/intents-filters#ForceChooser

    return Intent().setAction(Intent.ACTION_VIEW)
        .setDataAndType(uri, mime)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}


//fun downloadToDevice(context: Context, file: File, node: RTreeNode): Intent {
//
//    val uri = FileProvider.getUriForFile(context, DEFAULT_FILE_PROVIDER_ID, file)
//
//    var mime = node.mime
//    if (SdkNames.NODE_MIME_DEFAULT.equals(mime)) {
//        mime = getMimeType(node.name)
//    }
//
//    return Intent().setAction(Intent.ACTION_VIEW)
//        .setDataAndType(uri, mime)
//        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//}

//fun openWith(context: Context, file: File, node: RTreeNode): Intent {
//
//    val uri = FileProvider.getUriForFile(context, DEFAULT_FILE_PROVIDER_ID, file)
//
//    return Intent().setAction(Intent.ACTION_VIEW)
//        .setDataAndType(uri, "*/*")
//        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//}


fun resetToHomeStateIfNecessary(manager: FragmentManager, currentState: StateID) {
    // We manually set the current to be at root of the workspace to handle certain corner cases,
    // typically when app has been restored with an empty back stack deep in a workspace or
    // when we are in a special page
    val count = manager.backStackEntryCount
    if (count == 0 && currentState.path?.length ?: 0 > 0
        || currentState.path == "/${currentState.workspace}"
    ) {
        CellsApp.instance.setCurrentState(StateID.fromId(currentState.accountId))
    }
}

fun dumpBackStack(caller: String?, manager: FragmentManager) {
    val count = manager.backStackEntryCount
    val entry = if (count > 0) manager.getBackStackEntryAt(count - 1) else null
    Log.i(caller, "Back stack entry count: $count")
    Log.i(caller, "Previous entry: $entry")
}
