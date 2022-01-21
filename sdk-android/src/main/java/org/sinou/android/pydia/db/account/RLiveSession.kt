package org.sinou.android.pydia.db.account

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.TypeConverters
import com.pydio.cells.api.ui.WorkspaceNode
import org.sinou.android.pydia.db.Converters

@DatabaseView(
    "SELECT session_table.account_id, " +
            "session_table.workspaces, " +
            "session_table.lifecycle_state, " +
            "session_table.offline_roots, " +
            "session_table.bookmark_cache, " +
            "session_table.share_cache, " +
            "account_table.url, " +
            "account_table.username, " +
            "account_table.auth_status, " +
            "account_table.tls_mode, " +
            "account_table.is_legacy, " +
            "account_table.server_label, " +
            "account_table.welcome_message " +
            "FROM session_table INNER JOIN account_table " +
            "ON session_table.account_id = account_table.account_id"
)
@TypeConverters(Converters::class)
data class RLiveSession(
    @ColumnInfo(name = "account_id") val accountID: String,
    @ColumnInfo(name = "lifecycle_state") val lifecycleState: String,
    @ColumnInfo(name = "workspaces") var workspaces: List<WorkspaceNode>?,
    @ColumnInfo(name = "offline_roots") var offlineRoots: List<String>?,
    @ColumnInfo(name = "bookmark_cache") var bookmarkCache: List<String>?,
    @ColumnInfo(name = "share_cache") var shareCache: List<String>?,

    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "auth_status") var authStatus: String,
    @ColumnInfo(name = "tls_mode") var tlsMode: Int,
    @ColumnInfo(name = "is_legacy") var isLegacy: Boolean,
    @ColumnInfo(name = "server_label") val serverLabel: String?,
    @ColumnInfo(name = "welcome_message") val welcomeMessage: String?,
)

//// Not very useful for the time being, kept here for the pattern
//fun List<RLiveSession>.asDomainModel(): List<RLiveSession> {
//    return map {
//        it
//    }
//}
