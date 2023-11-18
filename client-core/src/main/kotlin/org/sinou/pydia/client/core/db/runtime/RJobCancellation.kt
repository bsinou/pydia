package org.sinou.pydia.client.core.db.runtime

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.sinou.pydia.client.core.util.currentTimestamp

@Entity(tableName = "job_cancellation")
data class RJobCancellation(

    @PrimaryKey
    @ColumnInfo(name = "job_id")
    var jobId: Long,

    @ColumnInfo(name = "request_ts") val requestTimestamp: Long,

    // TODO How do we pass "Cancel parent parameter"?
) {

    companion object {
        fun cancel(jobId: Long): RJobCancellation {
            return RJobCancellation(
                jobId = jobId,
                requestTimestamp = currentTimestamp(),
            )
        }
    }
}
