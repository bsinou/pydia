package org.sinou.android.pydia.db.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_table")
data class RAccount(

    @PrimaryKey
    @ColumnInfo(name = "account_id") val accountID: String,

    @ColumnInfo(name = "url") val url: String,

    @ColumnInfo(name = "username") val username: String,

    @ColumnInfo(name = "auth_status") var authStatus: String,

    // 0 = normal, 1 = skip verify, 2 = custom certificate (to be implemented)
    @ColumnInfo(name = "tls_mode") var tlsMode: Int = 0,

    @ColumnInfo(name = "is_legacy") var isLegacy: Boolean = false,

    @ColumnInfo(name = "server_label") val serverLabel: String?,

    @ColumnInfo(name = "welcome_message") val welcomeMessage: String?,
)
