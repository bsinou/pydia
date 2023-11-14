package org.sinou.pydia.client.core.db.runtime

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import androidx.room.Update
import org.sinou.pydia.client.core.JobStatus
import org.sinou.pydia.client.core.db.CellsConverters
import kotlinx.coroutines.flow.Flow

@Dao
@TypeConverters(CellsConverters::class)
interface JobDao {

    // MAIN JOB OBJECT

    // CRUDs

    @Insert
    fun insert(job: RJob): Long

    @Update
    fun update(job: RJob)

    @Query("SELECT * FROM jobs WHERE job_id = :jobId LIMIT 1")
    fun getById(jobId: Long): RJob?

    @Query("SELECT * FROM jobs WHERE template = :template AND done_ts = -1")
    fun getRunningForTemplate(template: String): List<RJob>

    @Query("SELECT * FROM jobs WHERE template = :template and done_ts < 1000 ORDER BY start_ts DESC LIMIT 1")
    fun getLatestRunning(template: String): RJob?

    @Query("SELECT * FROM jobs WHERE start_ts = -1")
    fun getAllNew(): List<RJob>

    @Query("DELETE FROM jobs WHERE done_ts > 0")
    fun clearTerminatedJobs()

    @Query("DELETE FROM jobs WHERE job_id = :jobId")
    fun deleteTransfer(jobId: Long)

    // Reactive queries

    @Query("SELECT * FROM jobs WHERE job_id = :jobId LIMIT 1")
    fun getJobById(jobId: Long): Flow<RJob?>

    @Query("SELECT * FROM jobs WHERE parent_id = :jobId AND status = :status ")
    fun getRunningChildren(jobId: Long, status: String = JobStatus.PROCESSING.id): List<RJob>

    @Query("SELECT * FROM jobs ORDER BY job_id DESC")
    fun getLiveJobs(): Flow<List<RJob>>

    @Query("SELECT * FROM jobs WHERE parent_id < 1 ORDER BY creation_ts DESC")
    fun getRootJobs(): Flow<List<RJob>>

    // JOB CANCELLATION
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cancellation: RJobCancellation)

    @Query("SELECT * FROM job_cancellation WHERE job_id = :jobId LIMIT 1")
    fun hasBeenCancelled(jobId: Long): RJobCancellation?

    @Query("DELETE FROM job_cancellation WHERE job_id = :jobId")
    fun deleteCancellation(jobId: Long)

}
