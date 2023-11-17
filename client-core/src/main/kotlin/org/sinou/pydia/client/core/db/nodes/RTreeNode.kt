package org.sinou.pydia.client.core.db.nodes

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.sinou.pydia.client.core.AppNames
import org.sinou.pydia.client.core.db.CellsConverters
import org.sinou.pydia.sdk.api.SdkNames
import org.sinou.pydia.sdk.api.ui.WorkspaceNode
import org.sinou.pydia.sdk.transport.StateID
import java.util.*

@Entity(tableName = "tree_nodes")
@TypeConverters(CellsConverters::class)
data class RTreeNode(

    @PrimaryKey
    @ColumnInfo(name = "encoded_state") var encodedState: String,

    // Two nodes in our local index can have the same UUID -> through policies they points toward the same S3 file.
    @ColumnInfo(name = "uuid") val uuid: String,

    @ColumnInfo(name = "workspace") val workspace: String,

    @ColumnInfo(name = "parent_path") val parentPath: String,

    @ColumnInfo(name = "name") val name: String,

    @ColumnInfo(name = "mime") var mime: String,

    @ColumnInfo(name = "etag") var etag: String?,

    @ColumnInfo(name = "size") var size: Long = -1L,

    @ColumnInfo(name = "remote_mod_ts") var remoteModificationTS: Long,

    @ColumnInfo(name = "last_check_ts") var lastCheckTS: Long = -1L,

    @ColumnInfo(name = "local_mod_ts") var localModificationTS: Long = -1L,

    @ColumnInfo(name = "local_mod_status") var localModificationStatus: String? = null,

    // We store all the well known properties that we use
    @ColumnInfo(name = "properties") val properties: Properties,

    // Arbitrary Key - Values to locally store meta exposed by the remote server
    // (being Cells or a Legacy P8)
    @ColumnInfo(name = "meta") val meta: Properties,

    // In the SDK Java layer, we compute a hash of the meta returned by the remote server
    // to ease later diff processing
    @ColumnInfo(name = "meta_hash") val metaHash: Int,

    // Default order column to simply display folders before files before the recycle
    @ColumnInfo(name = "sort_name") var sortName: String? = null,

    // Ease query against a given characteristic of the nodes (bookmarked, shared...)
    @ColumnInfo(name = "flags") var flags: Int = 0,

    // Files management: Files are now managed with the RLocalFile object
) {

    fun getStateID(): StateID {
        return StateID.safeFromId(encodedState)
    }

    fun getAccountID(): StateID {
        return getStateID().account()
    }

    fun isFolder(): Boolean {
        return isFolderFromMime(mime)
    }

    fun isFile(): Boolean {
        return !isFolder()
    }

    fun isWorkspaceRoot(): Boolean = mime == SdkNames.NODE_MIME_WS_ROOT

    fun isInRecycle(): Boolean {
        return parentPath.startsWith("/${SdkNames.RECYCLE_BIN_NAME}")
    }

    fun isRecycle(): Boolean {
        return name == SdkNames.RECYCLE_BIN_NAME
    }

    /** Returns the updated flags (why not?) */
    private fun setFlag(flag: Int, value: Boolean): Int {
        // TODO smelly code
        if (isFlag(flag)) {
            if (!value) {
                // removeFlag(flag)
                flags = flags and flag.inv()
            }
        } else if (value) {
            flags = flags or flag
        }
        return flags
    }

    private fun isFlag(flag: Int): Boolean {
        return flags and flag == flag
    }

//    fun toFileNode(): FileNode {
//        // TODO double check: we might drop some info that we have missed on first draft implementation
//        //   Rather directly use the properties
//        val fn = FileNode()
//        fn.setProperty(SdkNames.NODE_PROPERTY_UID, uuid)
//        fn.setProperty(SdkNames.NODE_PROPERTY_ETAG, etag)
//        fn.setProperty(SdkNames.NODE_PROPERTY_MTIME, "$remoteModificationTS")
//        fn.setProperty(SdkNames.NODE_PROPERTY_PATH, getStateID().path)
//        fn.setProperty(SdkNames.NODE_PROPERTY_WORKSPACE_SLUG, workspace)
//        fn.setProperty(SdkNames.NODE_PROPERTY_FILENAME, name)
//        fn.setProperty(SdkNames.NODE_PROPERTY_IS_FILE, "${isFile()}")
//        fn.setProperty(SdkNames.NODE_PROPERTY_MIME, mime)
//        fn.setProperty(SdkNames.NODE_PROPERTY_BYTESIZE, "$size")
//        return fn
//    }

    companion object {
        private const val logTag = "RTreeNode"

        /**
         * @param stateID use to retrieve the account ID, typically with search result,
         * we do not have the parent ID. But any node with the same accountID is OK.
         * @param fileNode the newly retrieved node
         */


        fun fromWorkspaceNode(stateID: StateID, node: WorkspaceNode): RTreeNode {
            try {

                val currSortName = when (node.type) {
                    SdkNames.WS_TYPE_PERSONAL -> "1_2_${node.label}"
                    SdkNames.WS_TYPE_CELL -> "1_8_${node.label}"
                    else -> "1_5_${node.label}"
                }

                return RTreeNode(
                    encodedState = stateID.id,
                    workspace = node.slug,
                    parentPath = "",
                    name = node.label ?: "",
                    uuid = stateID.id, // TODO fix this
                    etag = "",
                    mime = SdkNames.NODE_MIME_WS_ROOT,
                    size = 0L,
                    remoteModificationTS = 0,
                    // TODO re-implement the 2 below
                    properties = Properties(),
                    meta = Properties(),
                    metaHash = 0,
                    sortName = currSortName,
                )


//                val nodeUuid = if (Str.notEmpty(node.id)) node.id else node.slug

//                val storedID = // Retrieve the account from the passed state
//                    StateID.safeFromId(stateID.accountId).withPath("/${node.slug}")
//
//                // FIXME WorkspaceNode is also deprecated
//                throw RuntimeException("TODO re-implement without FileNode legacy layer")
//
//                return RTreeNode(
//                    encodedState = storedID.id,
//                    workspace = storedID.slug,
//                    parentPath = "",
//                    name = node.name,
//                    uuid = nodeUuid,
//                    etag = "",
//                    mime = SdkNames.NODE_MIME_WS_ROOT,
//                    size = 0L,
//                    remoteModificationTS = 0,
//                    properties = node.properties,
//                    // TODO manage this
//                    meta = Properties(),
//                    metaHash = 0,
//                    sortName = currSortName,
//                )
            } catch (e: java.lang.Exception) {
                Log.e(
                    logTag, "could not create RTreeNode for " +
                            "ws root ${node.slug} at ${stateID}: ${e.message}"
                )
                throw e
            }
        }

        fun isFolderFromMime(mime: String): Boolean {
            return mime == SdkNames.NODE_MIME_FOLDER
                    || mime == SdkNames.NODE_MIME_WS_ROOT
                    || mime == SdkNames.NODE_MIME_RECYCLE
        }
    }

    // Boiler plate shortcuts

    fun isBookmarked(): Boolean {
        return isFlag(AppNames.FLAG_BOOKMARK)
    }

    fun setBookmarked(value: Boolean): Int {
        return setFlag(AppNames.FLAG_BOOKMARK, value)
    }

    fun isShared(): Boolean {
        return isFlag(AppNames.FLAG_SHARE)
    }

    fun getShareAddress(): String? {
        return properties.getProperty(SdkNames.NODE_PROPERTY_SHARE_LINK, null)
    }

    fun setShared(isShared: Boolean, linkURL: String?) {
        setFlag(AppNames.FLAG_SHARE, isShared)
        if (isShared) {
            properties.setProperty(SdkNames.NODE_PROPERTY_SHARE_LINK, linkURL)
        } else {
            properties.remove(SdkNames.NODE_PROPERTY_SHARE_LINK)
        }
    }

    fun isOfflineRoot(): Boolean {
        return isFlag(AppNames.FLAG_OFFLINE)
    }

    fun setOfflineRoot(value: Boolean): Int {
        return setFlag(AppNames.FLAG_OFFLINE, value)
    }

    fun hasThumb(): Boolean {
        return isFlag(AppNames.FLAG_HAS_THUMB)
    }

    fun setHasThumb(value: Boolean): Int {
        return setFlag(AppNames.FLAG_HAS_THUMB, value)
    }

    fun isPreViewable(): Boolean {
        return isFlag(AppNames.FLAG_PRE_VIEWABLE)
    }

    fun setPreViewable(value: Boolean): Int {
        return setFlag(AppNames.FLAG_PRE_VIEWABLE, value)
    }
}

// Only 5 flags are defined (TODO use bit shifting and constants)
//fun Int.showFlags(): String =
//    Integer.toBinaryString(this).padStart(5, '0')
//
//// Prints the full range of all possible flags
//fun Int.debugAsString(): String =
//    Integer.toBinaryString(this).padStart(Int.SIZE_BITS, '0')
//
//fun hasTreeNodeFlag(flags: Int, flag: Int): Boolean {
//    return flags and flag == flag
//}
