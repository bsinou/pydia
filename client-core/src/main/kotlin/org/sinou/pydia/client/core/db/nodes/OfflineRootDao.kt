package org.sinou.pydia.client.core.db.nodes

import androidx.room.*
import org.sinou.pydia.client.core.AppNames

@Dao
interface OfflineRootDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(treeNode: ROfflineRoot)

    @Update
    fun update(treeNode: ROfflineRoot)

    @Query("DELETE FROM offline_roots WHERE encoded_state = :stateId")
    fun delete(stateId: String)

    @Query("SELECT * FROM offline_roots WHERE encoded_state = :encodedState LIMIT 1")
    fun get(encodedState: String): ROfflineRoot?

    @Query("SELECT * FROM offline_roots WHERE uuid = :uuid LIMIT 1")
    fun getByUuid(uuid: String): ROfflineRoot?

    @Query("SELECT * FROM offline_roots WHERE status != :lostStatus ORDER BY sort_name")
    fun getAllActive(lostStatus: String? = AppNames.OFFLINE_STATUS_LOST): List<ROfflineRoot>

    @Query("SELECT * FROM offline_roots ORDER BY sort_name")
    fun getAll(): List<ROfflineRoot>

//    @Query("SELECT * FROM offline_roots ORDER BY sort_name")
//    fun getAllLive(): Flow<List<ROfflineRoot>>
}
