package org.sinou.android.pydia.db.nodes

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface LiveOfflineRootDao {
    @Query("SELECT * FROM RLiveOfflineRoot")
    fun getLiveOfflineRoots(): LiveData<List<RLiveOfflineRoot>>
}