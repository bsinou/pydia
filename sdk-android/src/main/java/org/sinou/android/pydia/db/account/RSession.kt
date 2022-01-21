package org.sinou.android.pydia.db.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.pydio.cells.api.ui.WorkspaceNode
import org.sinou.android.pydia.db.Converters

@Entity(tableName = "session_table")
@TypeConverters(Converters::class)
data class RSession(

    @PrimaryKey
    @ColumnInfo(name = "account_id") val accountID: String,

    @ColumnInfo(name = "base_dir") val baseDir: String,

    @ColumnInfo(name = "lifecycle_state") var lifecycleState: String, // foreground, background or paused

    @ColumnInfo(name = "workspaces") var workspaces: List<WorkspaceNode>?,

    @ColumnInfo(name = "offline_roots") var offlineRoots: List<String>?,

    @ColumnInfo(name = "bookmark_cache") var bookmarkCache: List<String>?,

    @ColumnInfo(name = "share_cache") var shareCache: List<String>?,

    )
