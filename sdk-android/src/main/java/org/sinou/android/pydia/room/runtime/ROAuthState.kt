package org.sinou.android.pydia.room.runtime

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pydio.cells.api.ServerURL

/**
 *  Stores a map between the state that are generated during the OAuth process
 * and the corresponding {@code ServerURL}
 */
@Entity(tableName = "oauth_state_table")
data class ROAuthState(

    @PrimaryKey(autoGenerate = true)
    var uploadId: Long = 0L,

    @ColumnInfo(name = "oauth_state") val state: String,

    @ColumnInfo(name = "server_url") val serverURL: ServerURL,

    @ColumnInfo(name = "start_ts") val startTimestamp: Long,

    )
