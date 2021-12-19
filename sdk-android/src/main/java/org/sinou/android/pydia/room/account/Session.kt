package org.sinou.android.pydia.room.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_table")
data class Session(

    @PrimaryKey(autoGenerate = true) val uid: Int = 0,

    @ColumnInfo(name = "account_id") val accountID: String,

    @ColumnInfo(name = "lifecycle_state") val lifecycleState: String, // foreground, background or idle

    @ColumnInfo(name = "base_dir") var baseDir: String,

    @ColumnInfo(name = "auth_status") val authStatus: String,

    @ColumnInfo(name = "workspaces") var workspaces: List<String>?,

    @ColumnInfo(name = "offline_roots") var offlineRoots: List<String>?,

    @ColumnInfo(name = "bookmark_cache") var bookmarkCache: List<String>?,

    @ColumnInfo(name = "share_cache") var shareCache: List<String>?,

)
