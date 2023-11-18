package org.sinou.pydia.client.core.db.nodes

import androidx.room.ColumnInfo
import androidx.room.Entity
import org.sinou.pydia.client.core.AppNames
import org.sinou.pydia.client.core.util.currentTimestamp
import org.sinou.pydia.sdk.transport.StateID
import java.io.File

@Entity(
    tableName = "local_files",
    primaryKeys = [
        "encoded_state",
        "type"
    ],
)
data class RLocalFile(

    @ColumnInfo(name = "encoded_state") val encodedState: String,

    // Thumb, Preview, File
    @ColumnInfo(name = "type") val type: String,

    // Might be the file (for thumbs, preview) or the rel path from base dir (for files)
    // e.g. common-files/test/my-image.jpg
    @ColumnInfo(name = "file") val file: String,

    // The e-tag of the **main** file: it is used to detect when the file is probably out-dated
    // and thus that we need to re-trigger the download of the corresponding file.
    @ColumnInfo(name = "etag") var etag: String?,

    @ColumnInfo(name = "size") var size: Long = -1L,

    @ColumnInfo(name = "remote_mod_ts") var remoteTS: Long,

    @ColumnInfo(name = "local_mod_ts") var localTS: Long = -1L,
) {

    fun getStateID(): StateID {
        return StateID.safeFromId(encodedState)
    }

    fun getAccountID(): StateID {
        return getStateID().account()
    }

    companion object {
//        private val logTag = "RLocalFile"

        fun fromFile(stateID: StateID, type: String, file: File, eTag: String?, remoteTS: Long)
                : RLocalFile {
            val filename = if (type == AppNames.LOCAL_FILE_TYPE_FILE) {
                // TODO check this
                stateID.path!!.substring(1) // we remove the leading / for easier later use
            } else {
                file.name
            }

            return RLocalFile(
                encodedState = stateID.id,
                type = type,
                file = filename,
                etag = eTag,
                size = file.length(),
                remoteTS = remoteTS,
                localTS = currentTimestamp()
            )
        }
    }
}
