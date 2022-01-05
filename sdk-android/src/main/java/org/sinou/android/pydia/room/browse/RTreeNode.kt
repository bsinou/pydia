package org.sinou.android.pydia.room.browse

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.room.Converters
import java.lang.reflect.Type
import java.util.*

@Entity(tableName = "tree_node_table")
@TypeConverters(Converters::class)
data class RTreeNode(

    @PrimaryKey
    @ColumnInfo(name = "encoded_state") val encodedState: String,

    @ColumnInfo(name = "workspace") val workspace: String,

    @ColumnInfo(name = "parent_path") val parentPath: String,

    @ColumnInfo(name = "name") val name: String,

    @ColumnInfo(name = "type") var type: String,

    @ColumnInfo(name = "local_mod_ts") var localModificationTS: Long,

    @ColumnInfo(name = "remote_mod_ts") var remoteModificationTS: Long,

    @ColumnInfo(name = "last_check_ts") var lastCheckTS: Long,

    @ColumnInfo(name = "is_offline") var isOfflineRoot: Boolean = false,

    @ColumnInfo(name = "is_bookmarked") var isBookmarked: Boolean = false,

    @ColumnInfo(name = "is_shared") val isShared: Boolean = false,

    @ColumnInfo(name = "meta") val meta: Properties,
)
