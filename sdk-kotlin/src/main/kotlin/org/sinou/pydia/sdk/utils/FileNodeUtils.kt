package org.sinou.pydia.sdk.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.sinou.pydia.openapi.model.TreeNode
import org.sinou.pydia.openapi.model.TreeNodeType
import org.sinou.pydia.sdk.api.SdkNames
import org.sinou.pydia.sdk.api.ui.FileNode
import org.sinou.pydia.sdk.transport.StateID.Companion.utf8Encode
import java.util.Properties
import java.util.TreeMap

/**
 * Simply converts a Cells API TreeNode to our local FileNode object.
 * This is the central point for all tweaks to go on supporting  Pydio8 (and the legacy code).
 */
object FileNodeUtils {
    private const val logTag = "FileNodeUtils"
    fun toFileNode(treeNode: TreeNode): FileNode? {
        val fileNode = FileNode()

        // Pre-process Path info
        val uuid = treeNode.uuid ?: return null
        val treeNodePath = treeNode.path
        if (treeNodePath == null) {
            Log.w(logTag, "Cannot create FileNode with no path")
            return null
        }
        val slug = slugFrom(treeNodePath)
        val path = pathFrom(treeNodePath)
        val name = nameFrom(treeNodePath)
        var meta: Map<String, String>? = treeNode.metaStore
        if (meta == null) {
            meta = HashMap()
        }
        // Retrieve the MetaData and store it as properties for later use
        val metaProps = Properties()
        metaProps.putAll(meta)
        fileNode.meta = metaProps
        // Also stores a hash of the meta to ease detection of future change
        // (remote meta modification not always triggers a modification of the node's timestamp)
        val sorted = TreeMap(meta)
        val builder = StringBuilder()
        for (value in sorted.values) { // we can't use recent Java to support android 21 platform
            // FIXME: workspace shares are returned in a random **changing** order
            //  that modifies the hash almost at each call...
            builder.append(value)
        }
        fileNode.setProperty(
            SdkNames.NODE_PROPERTY_META_HASH,
            builder.toString().hashCode().toString()
        )

        // Main meta info: UUID, eTag (md5) and modification type
        fileNode.setProperty(SdkNames.NODE_PROPERTY_UID, uuid)
        if (Str.notEmpty(treeNode.etag)) {
            fileNode.setProperty(SdkNames.NODE_PROPERTY_ETAG, treeNode.etag)
        }
        val mTime = treeNode.mtime
        if (mTime != null) {
            fileNode.setProperty(SdkNames.NODE_PROPERTY_MTIME, treeNode.mtime)
        }

        // Path info
        fileNode.setProperty(SdkNames.NODE_PROPERTY_WORKSPACE_SLUG, slug)
        fileNode.setProperty(SdkNames.NODE_PROPERTY_PATH, path)
        fileNode.setProperty(SdkNames.NODE_PROPERTY_FILENAME, name)

        // File or Folder
        val isFile = treeNode.type === TreeNodeType.LEAF
        fileNode.setProperty(SdkNames.NODE_PROPERTY_IS_FILE, isFile.toString())

        // Mime based on remote info
        val type: String?
        type = if (isFile) {
            // This needs API level 24
            // type = meta.getOrDefault(SdkNames.NODE_PROPERTY_MIME, SdkNames.NODE_MIME_DEFAULT);
            if (meta.containsKey(SdkNames.NODE_PROPERTY_MIME)) {
                extractJSONString(meta[SdkNames.NODE_PROPERTY_MIME])
            } else {
                // Log.e(logTag, "No mime found for " + path);
                // TODO rather leave this at null?
                SdkNames.NODE_MIME_DEFAULT
            }
        } else {
            if (SdkNames.RECYCLE_BIN_NAME == name) {
                SdkNames.NODE_MIME_RECYCLE
            } else if ("/" == path) {
                SdkNames.NODE_MIME_WS_ROOT
            } else {
                SdkNames.NODE_MIME_FOLDER
            }
        }
        fileNode.setProperty(SdkNames.NODE_PROPERTY_MIME, type)

        // Size
        var sizeStr = treeNode.propertySize
        if (Str.empty(sizeStr)) {
            sizeStr = "0"
        }
        fileNode.setProperty(SdkNames.NODE_PROPERTY_BYTESIZE, sizeStr)

        // Permissions
        fileNode.setProperty(SdkNames.NODE_PROPERTY_FILE_PERMS, treeNode.mode.toString())

        // Share info
        val wsSharesStr = meta[SdkNames.META_KEY_WS_SHARES]
        if (wsSharesStr != null) {
            val gson = Gson()
            val objType = object : TypeToken<Array<Map<String?, Any?>?>?>() {}.type
            val shares = gson.fromJson<Array<Map<String, Any>>>(wsSharesStr, objType)
            for (currShare in shares) {
                // Filter out cells (scope = 2) at this point
                if (currShare.containsKey("Scope") && currShare["Scope"] as Double == 3.0) {
                    fileNode.setProperty(SdkNames.NODE_PROPERTY_SHARED, "true")
                    fileNode.setProperty(
                        SdkNames.NODE_PROPERTY_SHARE_UUID,
                        currShare["UUID"] as String?
                    )
                    break
                }
            }
        }
        val bookmark = meta["bookmark"]
        if (Str.notEmpty(bookmark)) {
            fileNode.setProperty(SdkNames.NODE_PROPERTY_BOOKMARK, bookmark)
        }

        // Image specific info.
        val isImage = "true" == meta["is_image"]
        fileNode.setProperty(SdkNames.NODE_PROPERTY_IS_IMAGE, isImage.toString())
        if (isImage) {
            fileNode.setProperty(SdkNames.NODE_PROPERTY_IMAGE_WIDTH, meta["image_width"])
            fileNode.setProperty(SdkNames.NODE_PROPERTY_IMAGE_HEIGHT, meta["image_height"])
        }
        // Also supports generated thumbs for other files (pdf, docs...) with recent Cells server
        if (meta.containsKey(SdkNames.META_KEY_IMG_THUMBS)) {
            fileNode.setProperty(SdkNames.NODE_PROPERTY_HAS_THUMB, true.toString())
        }
        if (isImage) {
            fileNode.setProperty(SdkNames.NODE_PROPERTY_IS_PRE_VIEWABLE, true.toString())
        }
        return fileNode
    }

    /**
     * The API (and SDK java) add leading and trailing double quotes to JSON Strings
     * that must be removed before handling the real String value.
     * Note that this does nothing if the String does not:
     * - have leading and trailing double quotes
     * - is shorter than 3 characters
     */
    fun extractJSONString(jsonStr: String?): String? {
        var jsonStr = jsonStr
        if (jsonStr!!.length > 2 && jsonStr.startsWith("\"") && jsonStr.endsWith("\"")) {
            jsonStr = jsonStr.substring(1, jsonStr.length - 1)
        }
        return jsonStr
    }

    fun getNameFromPath(path: String): String {
        val index = path.lastIndexOf("/")
        return if (index < 0) {
            path
        } else path.substring(index + 1)
    }

    private fun nameFrom(treeNodePath: String): String {
        val index = treeNodePath.indexOf("/")
        return if (index == -1) {
            "/"
        } else {
            treeNodePath.substring(treeNodePath.lastIndexOf("/") + 1)
        }
    }

    private fun slugFrom(treeNodePath: String): String {
        val index = treeNodePath.indexOf("/")
        return if (index == -1) {
            treeNodePath
        } else {
            treeNodePath.substring(0, index)
        }
    }

    private fun pathFrom(treeNodePath: String): String {
        val index = treeNodePath.indexOf("/")
        return if (index == -1) {
            "/"
        } else {
            val parts = treeNodePath.substring(index + 1).split("/".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val pathBuilder = StringBuilder()
            for (part in parts) {
                pathBuilder.append("/").append(part)
            }
            pathBuilder.toString()
        }
    }

    fun toTreeNodePath(ws: String, path: String): String {
        return ws + path
    }

    fun toEncodedTreeNodePath(ws: String, path: String): String {
        // Log.d(logTag, "Sanitizing: [" + ws + path + "]");
        val index = path.indexOf("/")
        return if (index == -1) {
            "/"
        } else {
            val parts =
                path.substring(index + 1).split("/".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val pathBuilder = StringBuilder()
            for (part in parts) {
                pathBuilder.append("/").append(utf8Encode(part))
            }
            Log.d(logTag, "   now: [$ws$pathBuilder]")
            ws + pathBuilder
        }
    }
}