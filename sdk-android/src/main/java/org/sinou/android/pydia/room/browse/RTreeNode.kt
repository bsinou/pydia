package org.sinou.android.pydia.room.browse

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.sinou.android.pydia.room.Converters
import java.util.*

@Entity(tableName = "tree_node_table")
@TypeConverters(Converters::class)
data class RTreeNode(

    @PrimaryKey
    @ColumnInfo(name = "encoded_state") val encodedState: String,

    @ColumnInfo(name = "workspace") val workspace: String,

    @ColumnInfo(name = "parent_path") val parentPath: String,

    @ColumnInfo(name = "name") val name: String,

    @ColumnInfo(name = "mime") var mime: String,

    @ColumnInfo(name = "etag") var etag: String?,

    @ColumnInfo(name = "size") var size: Long = -1L,

    @ColumnInfo(name = "remote_mod_ts") var remoteModificationTS: Long,

    @ColumnInfo(name = "local_mod_ts") var localModificationTS: Long = 0L,

    @ColumnInfo(name = "last_check_ts") var lastCheckTS: Long = 0L,

    @ColumnInfo(name = "is_offline") var isOfflineRoot: Boolean = false,

    @ColumnInfo(name = "is_bookmarked") var isBookmarked: Boolean = false,

    @ColumnInfo(name = "is_shared") var isShared: Boolean = false,

    @ColumnInfo(name = "meta") val meta: Properties,

    @ColumnInfo(name = "sort_name") var sortName: String? = null,

    @ColumnInfo(name = "thumb") var thumbFilename: String? = null,

    @ColumnInfo(name = "local") var localFilename: String? = null,
)

