package org.sinou.android.pydia.room.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "token_table")
data class RToken(

    @PrimaryKey(autoGenerate = true) val uid: Int = 0,

    @ColumnInfo(name = "account_id") val accountID: String,

    // Set by Cells layers to contain the corresponding encoded accountID
    @ColumnInfo(name = "subject") val subject: String?,

    // value is the real useful token => access_token in OAuth2
    @ColumnInfo(name = "value") val value: String,

    // idToken contains encoded information about current session, typically the claims
    @ColumnInfo(name = "id_token") val idToken: String,

    @ColumnInfo(name = "scope") val scope: String?,

    @ColumnInfo(name = "token_type") val tokenType: String,

    @ColumnInfo(name = "refresh_token") val refreshToken: String?,

    @ColumnInfo(name = "expires_in") val expiresIn: Long = 0,

    @ColumnInfo(name = "expiration_time") val expirationTime: Long = 0,

    // valid, expired, refreshing...
    // @ColumnInfo(name = "status") val status: Int,
)
