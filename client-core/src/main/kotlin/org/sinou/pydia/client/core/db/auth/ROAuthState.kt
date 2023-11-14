package org.sinou.pydia.client.core.db.auth

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.sinou.pydia.client.core.db.CellsConverters
import org.sinou.pydia.sdk.api.ServerURL

/**
 * Stores the OAuth state that is used as unique identifier during the Credentials flow
 * and the corresponding {@code ServerURL}
 */
@Entity(tableName = "oauth_states")
@TypeConverters(CellsConverters::class)
data class ROAuthState(

    @PrimaryKey
    @ColumnInfo(name = "oauth_state") val state: String,

    @ColumnInfo(name = "server_url") val serverURL: ServerURL,

    @ColumnInfo(name = "start_ts") val startTimestamp: Long,

    @ColumnInfo(name = "next") val next: String?,
)
    