package org.sinou.android.pydia.db.browse

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.pydio.cells.api.SdkNames
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.db.Converters
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
}
