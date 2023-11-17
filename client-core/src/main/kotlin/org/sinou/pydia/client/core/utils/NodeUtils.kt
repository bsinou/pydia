package org.sinou.pydia.client.core.utils

import android.util.Log
import android.webkit.MimeTypeMap
import androidx.compose.runtime.saveable.Saver
import org.sinou.pydia.client.core.db.accounts.RWorkspace
import org.sinou.pydia.client.core.db.nodes.RTreeNode
import org.sinou.pydia.sdk.api.SdkNames
import org.sinou.pydia.sdk.api.ui.WorkspaceNode
import org.sinou.pydia.sdk.transport.StateID

private const val LOG_TAG = "NodeUtils.kt"

// Utility to make the stateID savable
val stateIDSaver = Saver<StateID, String>(
    save = { stateID -> stateID.id },
    restore = { id -> StateID.fromId(id) },
)

fun areNodeContentEquals(local: RTreeNode, remote: RTreeNode): Boolean {
    // TODO rather use this when debugging is over. Also adapt areWsNodeContentEquals(), see below
//        return remote.eTag != null
//                && remote.eTag == local.etag
//                && local.remoteModificationTS == remote.lastModified()
//                // Also compare meta hash: timestamp is not updated when a meta changes
//                && remote.metaHashCode == local.metaHash
// // Does not work
// // && remote.flags == local.flags

    var isEqual = local.remoteModificationTS == remote.remoteModificationTS
    if (!isEqual) {
        //Log.d(NODE_UTILS, "Differ: modification times are not equals")
        return false
    }

    isEqual = remote.etag != null
    if (!isEqual) {
        Log.d(LOG_TAG, "Differ: no remote eTag")
        return false
    }
    isEqual = remote.etag == local.etag
    if (!isEqual) {
        Log.d(LOG_TAG, "Differ: eTag are different")
        return false
    }

    // Also compare meta hash: timestamp is not updated when a meta changes
    isEqual = remote.metaHash == local.metaHash
    if (!isEqual) {
        Log.d(LOG_TAG, "Differ: meta hash are not equals")
        Log.d(LOG_TAG, "local meta: ${local.properties}")
        Log.d(LOG_TAG, "remote meta: ${remote.properties}")
        return false
    }

    // Warning: this misses changes in the local state (e.G offline flag is set) that must be handled at another level
    // isEqual = remote.flags == local.flags

    return true
}

fun areWsNodeContentEquals(local: RWorkspace, remote: WorkspaceNode): Boolean {

    var isEqual = remote.slug == local.slug
    if (!isEqual) {
        Log.d(LOG_TAG, "Differ: slug have changed")
        return false
    }
    isEqual = remote.label == local.label
    if (!isEqual) {
        Log.d(LOG_TAG, "Differ: labels are different")
        return false
    }
    isEqual = remote.description == local.description
    if (!isEqual) {
        Log.d(LOG_TAG, "Differ: descriptions are different")
        return false
    }

    // TODO this is still broken, we do not have any info on remote modification date
//    isEqual = local.remoteModificationTS == remote.lastModified
//    if (!isEqual) {
//        Log.d(LOG_TAG, "Differ: Modification time are not equals")
//        return false
//    }

    isEqual = local.type == remote.type
    if (!isEqual) {
        Log.d(LOG_TAG, "Differ: Modification time are not equals")
        return false
    }

    // TODO also compare properties
    return true
}

fun isPreViewable(element: RTreeNode): Boolean {
    return if (element.mime.startsWith("image/")) {
        true
    } else if (element.mime.startsWith("\"image/")) {
        Log.w(LOG_TAG, "We had to tweak mime")
        Thread.dumpStack()
        // TODO remove this once the mime has been cleaned
        true
    } else if (element.mime == SdkNames.NODE_MIME_DEFAULT || element.mime == "\"${SdkNames.NODE_MIME_DEFAULT}\"") {
        val name = element.name.lowercase()
        name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".png")
                || name.endsWith(".gif")
    } else {
        false
    }
}

fun getMimeType(url: String, fallback: String = SdkNames.NODE_MIME_DEFAULT): String {
    val ext = MimeTypeMap.getFileExtensionFromUrl(url)
    if (ext != null) {
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        if (mime != null) return mime
    }
    return fallback
}
