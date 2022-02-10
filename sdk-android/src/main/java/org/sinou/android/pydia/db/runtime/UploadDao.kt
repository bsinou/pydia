package org.sinou.android.pydia.db.runtime

import androidx.lifecycle.LiveData
import androidx.room.*
import org.sinou.android.pydia.db.Converters

@Dao
@TypeConverters(Converters::class)
interface UploadDao {

    @Insert
    fun insert(upload: RUpload) : Long

    @Update
    fun update(upload: RUpload)

    @Query("SELECT * FROM uploads WHERE encoded_state = :stateId LIMIT 1")
    fun  get(stateId: String): RUpload?

    @Query("SELECT * FROM uploads WHERE uploadId = :uploadId LIMIT 1")
    fun  getById(uploadId: Long): RUpload?

    @Query("SELECT * FROM uploads WHERE rowid = :rowId")
    fun  getByRowId(rowId: Long): RUpload?

    @Query("SELECT * FROM uploads WHERE start_ts = -1")
    fun  getAllNew(): List<RUpload>

    @Query("SELECT * FROM uploads ORDER BY start_ts DESC")
    fun getActiveTransfers(): LiveData<List<RUpload>?>

}