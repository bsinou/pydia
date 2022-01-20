package org.sinou.android.pydia.utils

import com.pydio.cells.api.SdkNames
import org.sinou.android.pydia.room.browse.RTreeNode

fun isFolder(treeNode: RTreeNode): Boolean {
    return SdkNames.NODE_MIME_FOLDER.equals(treeNode.mime) || isRecycle(treeNode)
}

fun isRecycle(treeNode: RTreeNode): Boolean {
    return SdkNames.NODE_MIME_RECYCLE.equals(treeNode.mime)
}
