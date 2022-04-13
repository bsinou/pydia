package org.sinou.android.pydia.db.accounts

import androidx.lifecycle.LiveData
import androidx.room.*
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.db.Converters

@Dao
@TypeConverters(Converters::class)
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(session: RSession)

    @Update
    fun update(session: RSession)

    @Query("DELETE FROM sessions WHERE account_id = :accountID")
    fun forgetSession(accountID: String)

    @Query("SELECT * FROM sessions WHERE account_id = :accountID LIMIT 1")
    fun getSession(accountID: String): RSession?

    @Query("SELECT * FROM sessions WHERE account_id = :accountID LIMIT 1")
    fun getLiveSession(accountID: String): LiveData<RSession?>

    @Query("SELECT * FROM sessions")
    fun getSessions(): List<RSession>

    @Query("SELECT * FROM sessions")
    fun getLiveSessions(): LiveData<List<RSession>>

    @Query("SELECT * FROM sessions WHERE lifecycle_state = :state")
    fun getForegroundSession(state: String = AppNames.LIFECYCLE_STATE_FOREGROUND): LiveData<RSession?>

    /**
     * Convenience method to insure we reset all sessions to be in the background before
     * putting one live.
     * // TODO rather return not paused sessions
     */
    @Query("SELECT * FROM sessions WHERE lifecycle_state = :state")
    fun foregroundSessions(state: String = AppNames.LIFECYCLE_STATE_FOREGROUND): List<RSession>

    @Query("SELECT * FROM sessions WHERE lifecycle_state = :state")
    fun getBackgroundSessions(state: String = AppNames.LIFECYCLE_STATE_BACKGROUND): List<RSession>

    @Query("SELECT * FROM sessions WHERE dir_name = :dirName")
    fun getWithDirName(dirName: String): List<RSession>

}