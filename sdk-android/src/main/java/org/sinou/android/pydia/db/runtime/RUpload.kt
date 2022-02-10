package org.sinou.android.pydia.db.runtime

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pydio.cells.transport.StateID

@Entity(tableName = "uploads")
data class RUpload(
    @PrimaryKey(autoGenerate = true)
    var uploadId: Long = 0L,

    @ColumnInfo(name = "encoded_state") val encodedState: String,

    @ColumnInfo(name = "source") val source: String,

    @ColumnInfo(name = "byte_size") val byteSize: Long,

    @ColumnInfo(name = "mime") val mime: String,

    @ColumnInfo(name = "start_ts") var startTimestamp: Long = -1L,

    @ColumnInfo(name = "done_ts") var doneTimestamp: Long = -1L,

    @ColumnInfo(name = "error") var error: String? = null,

    @ColumnInfo(name = "progress") val progress: Int = 0,
) {

    fun getStateId(): StateID{
        return StateID.fromId(encodedState)
    }

    companion object {
        fun fromState(
            encodedState: String,
            source: String,
            byteSize: Long,
            mime: String
        ): RUpload {
            return RUpload(
                encodedState = encodedState,
                source = source,
                byteSize = byteSize,
                mime = mime,
            )
        }
    }
}

