package org.sinou.android.pydia.db.runtime

import androidx.lifecycle.LiveData
import androidx.room.*
import org.sinou.android.pydia.db.Converters

@Dao
@TypeConverters(Converters::class)
interface TransferDao {

    @Insert
    fun insert(transfer: RTransfer): Long

    @Update
    fun update(transfer: RTransfer)

    @Query("SELECT * FROM transfers WHERE encoded_state = :stateId LIMIT 1")
    fun getByState(stateId: String): RTransfer?

    @Query("SELECT * FROM transfers WHERE transferId = :transferUID LIMIT 1")
    fun getById(transferUID: Long): RTransfer?

    @Query("SELECT * FROM transfers WHERE transferId = :transferUID LIMIT 1")
    fun getLiveById(transferUID: Long): LiveData<RTransfer?>

    @Query("SELECT * FROM transfers WHERE start_ts = -1")
    fun getAllNew(): List<RTransfer>

    @Query("SELECT * FROM transfers ORDER BY start_ts DESC")
    fun getActiveTransfers(): LiveData<List<RTransfer>?>

    @Query("DELETE FROM transfers WHERE done_ts > 0")
    fun clearTerminatedTransfers()

    @Query("DELETE FROM transfers WHERE transferId = :transferUID")
    fun deleteTransfer(transferUID: Long)
}
