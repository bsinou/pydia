package org.sinou.android.pydia.room.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "legacy_credentials_table")
data class LegacyCredentials(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "account_id") val accountID: String,
    @ColumnInfo(name = "password") val password: String,
)
