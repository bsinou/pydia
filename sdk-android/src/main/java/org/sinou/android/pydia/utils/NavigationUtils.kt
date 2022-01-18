package org.sinou.android.pydia.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.pydio.cells.api.SdkNames
import org.sinou.android.pydia.BuildConfig
import org.sinou.android.pydia.room.browse.RTreeNode
import org.sinou.android.pydia.services.NodeService
import java.io.File

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