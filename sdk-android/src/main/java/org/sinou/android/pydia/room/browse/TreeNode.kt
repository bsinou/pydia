package org.sinou.android.pydia.room.browse

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

@Entity(tableName = "tree_node_table")
data class TreeNode(

    @PrimaryKey(autoGenerate = true) val uid: Int = 0,

    // TODO Rather make one DB per account. Is it possible?
    @ColumnInfo(name = "account_id") val accountID: String,

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

    @ColumnInfo(name = "json_meta") val jsonMeta: String?,

)
