package org.sinou.android.pydia.db.runtime

import androidx.lifecycle.LiveData
import androidx.room.*
import org.sinou.android.pydia.db.Converters

@Dao
@TypeConverters(Converters::class)
interface UploadDao {

    @Insert
    fun insert(upload: RUpload)

    @Update
    fun update(upload: RUpload)

    @Query("SELECT * FROM upload_table ORDER BY start_ts DESC")
    fun getActiveTransfers(): LiveData<List<RUpload>?>

}