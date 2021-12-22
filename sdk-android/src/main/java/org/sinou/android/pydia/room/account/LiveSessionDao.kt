package org.sinou.android.pydia.room.account

import androidx.lifecycle.LiveData
import androidx.room.*
import org.sinou.android.pydia.room.Converters

@Dao
interface LiveSessionDao {

    @Query("SELECT * FROM RLiveSession")
    fun getLiveSessions(): LiveData<List<RLiveSession>>

}