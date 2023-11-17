package org.sinou.pydia.client.core.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.sinou.pydia.client.core.db.nodes.RTreeNode
import org.sinou.pydia.openapi.model.TreeNode
import org.sinou.pydia.openapi.model.TreeNodeType
import org.sinou.pydia.sdk.api.SdkNames
import org.sinou.pydia.sdk.transport.StateID
import org.sinou.pydia.sdk.utils.Log
import java.util.Properties

private const val LOG_TAG = "TreeNodeUtils.kt"

/* Performs the mapping between Cells API and the local model*/
fun fromTreeNode(stateID: StateID, treeNode: TreeNode): RTreeNode {

    // Construct path & stateID
    val path = treeNode.path
        ?: throw IllegalArgumentException("Cannot create a node with an empty path")
    val childStateID = StateID.safeFromId(stateID.accountId).withPath("/$path")
    // Deduct a Name
    val name = childStateID.fileName ?: run {
        childStateID.slug!!
    }
    val isFolder = treeNode.isFolder()

    try {
        // Mime
        val mimeType = if (isFolder) {
            if (SdkNames.RECYCLE_BIN_NAME == name) {
                SdkNames.NODE_MIME_RECYCLE
            } else if ("/" == childStateID.file) {
                SdkNames.NODE_MIME_WS_ROOT
            } else {
                SdkNames.NODE_MIME_FOLDER
            }
        } else {
            val encodedMime = treeNode.getProperty(SdkNames.NODE_PROPERTY_MIME)
            encodedMime?.let { extractJSONString(it) }
                ?: getMimeType(name, SdkNames.NODE_MIME_DEFAULT)
        }

        val mTime: Long = treeNode.mtime?.toLong() ?: -1
        // Size
        var sizeStr: String? = treeNode.propertySize
        if (sizeStr.isNullOrEmpty()) {
            sizeStr = "0"
        }
        val size = sizeStr.toLong()

        // TODO
        // Permissions
        treeNode.getProperty(SdkNames.NODE_PROPERTY_FILE_PERMS)

        // Share info
        var shared = false
        var shareUUID: String? = null
        val wsSharesStr = treeNode.getProperty(SdkNames.META_KEY_WS_SHARES)?.let { json ->
            val gson = Gson()
            val sharesType = object : TypeToken<Array<Map<String, Any>>>() {}.type
            val shares: Array<Map<String, Any>> = gson.fromJson(json, sharesType)

            shares.find { share ->
                val scope = share["Scope"]
                scope is Double && scope == 3.0
            }?.also { matchingShare ->
                shared = true
                shareUUID = matchingShare["UUID"] as? String
            }
        }

        // Image specific info.

        // Image specific info.
        val isImage = treeNode.getProperty("is_image") == "true"
//TODO
//            if (isImage) {
//                fileNode.setProperty(SdkNames.NODE_PROPERTY_IMAGE_WIDTH, meta.get("image_width"))
//                fileNode.setProperty(SdkNames.NODE_PROPERTY_IMAGE_HEIGHT, meta.get("image_height"))
//            }
//            // Also supports generated thumbs for other files (pdf, docs...) with recent Cells server
//            // Also supports generated thumbs for other files (pdf, docs...) with recent Cells server
//            if (meta.containsKey(SdkNames.META_KEY_IMG_THUMBS)) {
//                fileNode.setProperty(SdkNames.NODE_PROPERTY_HAS_THUMB, true.toString())
//            }
//            if (isImage) {
//                fileNode.setProperty(SdkNames.NODE_PROPERTY_IS_PRE_VIEWABLE, true.toString())
//            }

        // Retrieve the MetaData and store it as properties for later use
        val metaProps = Properties()
        treeNode.metaStore?.let {
            metaProps.putAll(it)
        }

        // FIXME
        // Also stores a hash of the meta to ease detection of future change
        val map = mutableMapOf<String, String>()
        treeNode.metaStore?.let {
            map.putAll(it)
        }
        val sorted = map.toSortedMap(compareBy<String> { it })
        val builder = StringBuilder()
        sorted.forEach {
            // We build a local hash after sorting the map because e.G workspace shares
            // are returned in a random **changing** order that modifies the hash almost at each call...
            builder.append(it.value)
        }
        val metaHash = builder.toString().hashCode()

        val node = RTreeNode(
            encodedState = childStateID.id,
            workspace = childStateID.slug!!,
            parentPath = childStateID.parentFile ?: "",
            name = name,
            uuid = treeNode.uuid!!,
            etag = treeNode.etag ?: "",
            mime = mimeType,
            size = size,
            remoteModificationTS = mTime,
            // TODO
            properties = Properties(),
            meta = metaProps,
            metaHash = metaHash,
        )

        // Share and offline cache values are rather handled in the NodeService directly
        node.setBookmarked(treeNode.isBookmark())
        node.setHasThumb(treeNode.hasThumb())
        // TODO
        // node.setPreViewable(treeNode.isPreViewable())

        node.sortName = when (node.mime) {
            SdkNames.NODE_MIME_WS_ROOT -> "1_${node.name}"
            SdkNames.NODE_MIME_FOLDER -> "3_${node.name}"
            SdkNames.NODE_MIME_RECYCLE -> "8_${node.name}"
            else -> "5_${node.name}"
        }
        return node

    } catch (e: Exception) {
        Log.e(LOG_TAG, "could not create RTreeNode for ${childStateID}: ${e.message}")
        throw e
    }
}

fun TreeNode.getProperty(key: String): String? {
    return this.metaStore?.let { it[key] }
}

fun TreeNode.isBookmark(): Boolean {
    return "true" == getProperty(SdkNames.NODE_PROPERTY_BOOKMARK)
}

fun TreeNode.isShared(): Boolean {
    return "true" == getProperty(SdkNames.NODE_PROPERTY_SHARED)
}

fun TreeNode.hasThumb(): Boolean {
    return "true" == getProperty(SdkNames.NODE_PROPERTY_HAS_THUMB)
}

fun TreeNode.isFolder(): Boolean {
    return this.type != TreeNodeType.LEAF
}

fun TreeNode.modificationTS(): Long {
    return mtime?.toLong() ?: -1
}

// TODO smelly codes
fun extractJSONString(jsonStr: String): String {
    return if (jsonStr.length > 2 && jsonStr.startsWith("\"") && jsonStr.endsWith("\"")) {
        jsonStr.substring(1, jsonStr.length - 1)
    } else jsonStr
}
