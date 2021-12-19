package org.sinou.android.pydia.room.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_table")
data class Account(

    @PrimaryKey(autoGenerate = true) val uid: Int = 0,

    @ColumnInfo(name = "username") val username: String,

    @ColumnInfo(name = "url") val url: String,

    @ColumnInfo(name = "skipVerify") var skipVerify: Boolean = false,

    @ColumnInfo(name = "isLegacy") var isLegacy: Boolean = false,

    @ColumnInfo(name = "isActive") var isActive: Boolean = false,

    @ColumnInfo(name = "serverLabel") val serverLabel: String?,

    @ColumnInfo(name = "welcomeMessage") val welcomeMessage: String?,
    /// Map<String, WorkspaceNode> cachedWorkspaces;

)
