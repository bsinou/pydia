package org.sinou.android.pydia.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import com.pydio.cells.api.SdkNames
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.BuildConfig
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.db.browse.RTreeNode
import java.io.File

private const val DEFAULT_FILE_PROVIDER_SUFFIX = ".fileprovider"
private const val DEFAULT_FILE_PROVIDER_ID =
    BuildConfig.APPLICATION_ID + DEFAULT_FILE_PROVIDER_SUFFIX

fun downloadToDevice(context: Context, file: File, node: RTreeNode): Intent {

    val uri = FileProvider.getUriForFile(context, DEFAULT_FILE_PROVIDER_ID, file)

    var mime = node.mime
    if (SdkNames.NODE_MIME_DEFAULT.equals(mime)) {
        mime = getMimeType(node.name)
    }

    return Intent().setAction(Intent.ACTION_VIEW)
        .setDataAndType(uri, mime)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

fun openWith(context: Context, file: File, node: RTreeNode): Intent {

    val uri = FileProvider.getUriForFile(context, DEFAULT_FILE_PROVIDER_ID, file)

    return Intent().setAction(Intent.ACTION_VIEW)
        .setDataAndType(uri, "*/*")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

}

/**
 * Open current file with the viewer provided by Android OS.
 * Thanks to https://stackoverflow.com/questions/56598480/couldnt-find-meta-data-for-provider-with-authority
 */
fun externallyView(context: Context, file: File, node: RTreeNode): Intent {
    val uri = FileProvider.getUriForFile(context,DEFAULT_FILE_PROVIDER_ID, file)
    var mime = node.mime
    if (SdkNames.NODE_MIME_DEFAULT.equals(mime)) {
        mime = getMimeType(node.name)
    }
    return Intent().setAction(Intent.ACTION_VIEW)
        .setDataAndType(uri, mime)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

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

fun dumpBackStack(caller: String, manager: FragmentManager) {
    val count = manager.backStackEntryCount
    val entry = if (count > 0) manager.getBackStackEntryAt(count - 1) else null
    Log.i(caller, "Back stack entry count: $count")
    Log.i(caller, "Previous entry: $entry")
}
