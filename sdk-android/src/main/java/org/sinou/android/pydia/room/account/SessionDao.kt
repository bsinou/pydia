package org.sinou.android.pydia.room.account

import androidx.lifecycle.LiveData
import androidx.room.*
import org.sinou.android.pydia.room.Converters

@Dao
@TypeConverters(Converters::class)
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(session: RSession)

    @Update
    fun update(session: RSession)

    @Query("DELETE FROM session_table WHERE account_id = :accountID")
    fun forgetSession(accountID: String)

    @Query("SELECT * FROM session_table WHERE account_id = :accountID LIMIT 1")
    fun getSession(accountID: String): RSession?

    @Query("SELECT * FROM session_table WHERE account_id = :accountID LIMIT 1")
    fun getLiveSession(accountID: String): LiveData<RSession?>

    @Query("SELECT * FROM session_table")
    fun getLiveSessions(): LiveData<List<RSession>>

    @Query("SELECT * FROM session_table WHERE lifecycle_state = 'foreground'")
    fun getForegroundSession(): LiveData<RSession?>

    /**
     * Convenience method to insure we reset all sessions to be in the background before
     * putting one live.
     * // TODO rather return not paused sessions
     */
    @Query("SELECT * FROM session_table WHERE lifecycle_state = 'foreground'")
    fun foregroundSessions(): List<RSession>

    @Query("SELECT * FROM session_table WHERE lifecycle_state = 'background'")
    fun getBackgroundSessions(): List<RSession>

}