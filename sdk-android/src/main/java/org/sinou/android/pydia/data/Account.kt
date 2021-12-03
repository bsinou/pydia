package org.sinou.android.pydia.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "login") val login: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "skipVerify") var skipVerify: Boolean = false
)