package org.sinou.android.pydia.db.browse

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.pydio.cells.api.SdkNames
import com.pydio.cells.api.ui.FileNode
import com.pydio.cells.api.ui.WorkspaceNode
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.db.Converters
import org.sinou.android.pydia.utils.getMimeType
import java.util.*

@Entity(tableName = "tree_node_table")
@TypeConverters(Converters::class)
data class RTreeNode(

    @PrimaryKey
    @ColumnInfo(name = "encoded_state") val encodedState: String,

    @ColumnInfo(name = "uuid") val UUID: String? = null,

    @ColumnInfo(name = "workspace") val workspace: String,

    @ColumnInfo(name = "parent_path") val parentPath: String,

    @ColumnInfo(name = "name") val name: String,

    @ColumnInfo(name = "mime") var mime: String,

    @ColumnInfo(name = "etag") var etag: String?,

    @ColumnInfo(name = "size") var size: Long = -1L,

    @ColumnInfo(name = "remote_mod_ts") var remoteModificationTS: Long,

    @ColumnInfo(name = "local_mod_ts") var localModificationTS: Long = 0L,

    @ColumnInfo(name = "local_mod_status") var localModificationStatus: String? = null,

    @ColumnInfo(name = "last_check_ts") var lastCheckTS: Long = 0L,

    @ColumnInfo(name = "is_offline_root") var isOfflineRoot: Boolean = false,

    @ColumnInfo(name = "is_bookmarked") var isBookmarked: Boolean = false,

    @ColumnInfo(name = "is_shared") var isShared: Boolean = false,

    @ColumnInfo(name = "meta") val meta: Properties,

    @ColumnInfo(name = "meta_hash") val metaHash: Int,

    @ColumnInfo(name = "sort_name") var sortName: String? = null,

    @ColumnInfo(name = "thumb") var thumbFilename: String? = null,

    // Can be: none, cache, offline, external
    @ColumnInfo(name = "local_file_type") var localFileType: String = AppNames.LOCAL_FILE_TYPE_NONE,

    // When necessary, we store the full path to the
    // relevant local resource somewhere in the external storage.
    @ColumnInfo(name = "localPath") var localFilePath: String? = null,
) {

    fun getStateID(): StateID {
        return StateID.fromId(encodedState)
    }

    fun isFolder(): Boolean {
        return mime == SdkNames.NODE_MIME_FOLDER
                || mime == SdkNames.NODE_MIME_WS_ROOT
                || mime == SdkNames.NODE_MIME_RECYCLE
    }

    fun isFile(): Boolean {
        return !isFolder()
    }

    fun isInRecycle(): Boolean {
        return parentPath.startsWith("/${SdkNames.RECYCLE_BIN_NAME}")
    }

    fun isRecycle(): Boolean {
        return name == SdkNames.RECYCLE_BIN_NAME
    }

    companion object {
        private const val TAG = "RTreeNode"

        fun fromFileNode(stateID: StateID, fileNode: FileNode): RTreeNode {
            Log.w(TAG, "... toRTreeNodeNew $stateID")
            Log.w(TAG, "  - WS: ${fileNode.workspace}")
            Log.w(TAG, "  - Path: ${fileNode.path}")
            Log.w(TAG, "  - Label: ${fileNode.label}")
            val childStateID = // Retrieve the account from the passed state
                StateID.fromId(stateID.accountId)
                    // Construct the path from file node info
                    .withPath("/${fileNode.workspace}${fileNode.path}")
            try {
                val node = RTreeNode(
                    encodedState = childStateID.id,
                    workspace = childStateID.workspace,
                    parentPath = childStateID.parentFile ?: "",
                    name = childStateID.fileName ?: childStateID.workspace,
                    UUID = fileNode.id,
                    etag = fileNode.eTag,
                    mime = fileNode.mimeType,
                    size = fileNode.size,
                    isBookmarked = fileNode.isBookmark,
                    isShared = fileNode.isShared,
                    remoteModificationTS = fileNode.lastModified,
                    meta = fileNode.properties,
                    metaHash = fileNode.metaHashCode
                )

                // Use Android library to precise MimeType when possible
                if (SdkNames.NODE_MIME_DEFAULT == node.mime) {
                    node.mime = getMimeType(node.name, SdkNames.NODE_MIME_DEFAULT)
                }

                // Add a technical name to easily have a canonical sorting by default,
                // that is: folders, files, recycle bin.
                node.sortName = when (node.mime) {
                    SdkNames.NODE_MIME_WS_ROOT -> "1_${node.name}"
                    SdkNames.NODE_MIME_FOLDER -> "3_${node.name}"
                    SdkNames.NODE_MIME_RECYCLE -> "8_${node.name}"
                    else -> "5_${node.name}"
                }
                return node

            } catch (e: java.lang.Exception) {
                Log.w(TAG, "could not create RTreeNode for ${childStateID}: ${e.message}")
                throw e
            }
        }

        fun fromWorkspaceNode(stateID: StateID, node: WorkspaceNode): RTreeNode {
            Log.w(TAG, "... fromWorkspaceNod $stateID")
            Log.w(TAG, "  - Slug: ${node.slug}")
            Log.w(TAG, "  - Label: ${node.label}")
            Log.w(TAG, "  - Desc: ${node.description}")
            try {
                val currSortName = when (node.workspaceType){
                    SdkNames.WS_TYPE_PERSONAL -> "1_2_${node.label}"
                    SdkNames.WS_TYPE_CELL -> "1_8_${node.label}"
                    else -> "1_5_${node.label}"
                }

                val outNode = RTreeNode(
                    encodedState = stateID.id,
                    workspace = node.slug,
                    parentPath = "",
                    name = node.label,
                    // TODO rather handle the UUID
                    UUID = node.slug,
                    etag = "",
                    mime = SdkNames.NODE_MIME_WS_ROOT,
                    size = 0L,
                    isBookmarked = false,
                    isShared = false,
                    remoteModificationTS = 0,
                    meta = node.properties,
                    // TODO manage this
                    metaHash = 0,
                    sortName = currSortName,
                )

                return outNode
            } catch (e: java.lang.Exception) {
                Log.w(
                    TAG, "could not create RTreeNode for " +
                            "ws root ${node.slug} at ${stateID}: ${e.message}"
                )
                throw e
            }
        }
    }
}
