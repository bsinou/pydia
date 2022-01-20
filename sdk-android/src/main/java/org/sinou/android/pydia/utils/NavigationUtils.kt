package org.sinou.android.pydia.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import com.pydio.cells.api.SdkNames
import org.sinou.android.pydia.BuildConfig
import org.sinou.android.pydia.room.browse.RTreeNode
import org.sinou.android.pydia.services.NodeService
import java.io.File

private const val FILE_PROVIDER_SUFFIX = ".fileprovider"
private const val FILE_PROVIDER_ID = BuildConfig.APPLICATION_ID + FILE_PROVIDER_SUFFIX


fun downloadToDevice(context: Context, file: File, node: RTreeNode): Intent {

    val uri = FileProvider.getUriForFile(context, FILE_PROVIDER_ID, file)

    var mime = node.mime
    if (SdkNames.NODE_MIME_DEFAULT.equals(mime)) {
        mime = NodeService.getMimeType(node.name)
    }

    return Intent().setAction(Intent.ACTION_VIEW)
        .setDataAndType(uri, mime)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

fun openWith(context: Context, file: File, node: RTreeNode): Intent {

    val uri = FileProvider.getUriForFile(context, FILE_PROVIDER_ID, file)

    return Intent().setAction(Intent.ACTION_VIEW)
        .setDataAndType(uri, "*/*")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

}


/**
 * Open current file with the viewer provided by Android OS.
 *
 * Thanks to https://stackoverflow.com/questions/56598480/couldnt-find-meta-data-for-provider-with-authority
 */
fun externallyView(context: Context, file: File, node: RTreeNode): Intent {

    val uri = FileProvider.getUriForFile(
        context,
        BuildConfig.APPLICATION_ID + ".fileprovider", file
    )

    var mime = node.mime
    if (SdkNames.NODE_MIME_DEFAULT.equals(mime)) {
        mime = NodeService.getMimeType(node.name)
    }

    return Intent().setAction(Intent.ACTION_VIEW)
        .setDataAndType(uri, mime)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

fun getAppMime(context: Context, name: String): String {
    val filename = name.lowercase()

    return if (filename.contains(".doc") || filename.contains(".docx")) {
        "application/msword"
    } else if (filename.contains(".pdf")) {
        "application/pdf"
    } else if (filename.contains(".ppt") || filename.contains(".pptx")) {
        "application/vnd.ms-powerpoint"
    } else if (filename.contains(".xls") || filename.contains(".xlsx")) {
        "application/vnd.ms-excel"
    } else if (filename.contains(".rtf")) {
        "application/rtf"
    } else if (filename.contains(".wav") || filename.contains(".mp3")) {
        "audio/x-wav"
    } else if (filename.contains(".ogg") || filename.contains(".flac")) {
        "audio/*"
    } else if (filename.contains(".gif")) {
        "image/gif"
    } else if (filename.contains(".jpg") || filename.contains(".jpeg") || filename.contains(".png")) {
        "image/jpeg"
    } else if (filename.contains(".txt")) {
        "text/plain"
    } else if (filename.contains(".3gp") || filename.contains(".mpg") || filename
            .contains(".mpeg") || filename.contains(".mpe") || filename
            .contains(".mp4") || filename.contains(".avi")
    ) {
        "video/*"
    } else {
        "*/*"
    }
}

fun dumpBackStack(caller: String, manager: FragmentManager) {
    val count = manager.backStackEntryCount
    val entry = if (count > 0) manager.getBackStackEntryAt(count - 1) else null

    Log.i(caller, "Back stack entry count: $count")
    Log.i(caller, "Previous entry: $entry")
}