package org.sinou.android.pydia.db.runtime

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.TypeConverters
import androidx.room.Update
import org.sinou.android.pydia.db.Converters

@Dao
@TypeConverters(Converters::class)
interface UploadDao {

    @Insert
    fun insert(upload: RUpload)

    @Update
    fun update(upload: RUpload)

}