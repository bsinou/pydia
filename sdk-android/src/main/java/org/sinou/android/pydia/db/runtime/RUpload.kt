package org.sinou.android.pydia.db.runtime

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upload_table")
data class RUpload(

    @PrimaryKey(autoGenerate = true)
    var uploadId: Long = 0L,

    @ColumnInfo(name = "target_state") val targetState: String,

    @ColumnInfo(name = "uri") val uri: String,

    @ColumnInfo(name = "bytesize") val bytesize: Long,

    @ColumnInfo(name = "mime") val mime: String,

    @ColumnInfo(name = "start_ts") var startTimestamp: Long = -1L,

    @ColumnInfo(name = "done_ts") var doneTimestamp: Long = -1L,

    @ColumnInfo(name = "error") var error: String?,

    @ColumnInfo(name = "progress") val progress: Int = 0,

    )

