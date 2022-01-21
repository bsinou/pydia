package org.sinou.android.pydia.utils

import android.content.Context
import android.webkit.MimeTypeMap
import com.pydio.cells.api.SdkNames
import org.sinou.android.pydia.db.browse.RTreeNode

fun isFolder(treeNode: RTreeNode): Boolean {
    return SdkNames.NODE_MIME_FOLDER.equals(treeNode.mime) || isRecycle(treeNode)
}

fun isRecycle(treeNode: RTreeNode): Boolean {
    return SdkNames.NODE_MIME_RECYCLE.equals(treeNode.mime)
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

fun getMimeType(url: String, fallback: String = "*/*"): String {
    val ext = MimeTypeMap.getFileExtensionFromUrl(url)
    if (ext != null) {
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        if (mime != null) return mime
    }
    return fallback
}

