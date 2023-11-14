package org.sinou.pydia.client.core.db.accounts

import androidx.room.*
import org.sinou.pydia.client.core.AppNames

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(session: RSession)

    @Update
    fun update(session: RSession)

    @Query("DELETE FROM sessions WHERE account_id = :accountID")
    fun forgetSession(accountID: String)

    @Query("SELECT * FROM sessions WHERE account_id = :accountID LIMIT 1")
    fun getSession(accountID: String): RSession?

    @Query("SELECT * FROM sessions")
    fun getSessions(): List<RSession>

    @Query("SELECT * FROM sessions WHERE lifecycle_state = :lifecycleState LIMIT 1")
    fun getForegroundSession(lifecycleState: String = AppNames.LIFECYCLE_STATE_FOREGROUND): RSession?

    /**
     * Convenience method to insure we reset all sessions to be in the background before
     * putting one live.
     */
    @Query("SELECT * FROM sessions WHERE lifecycle_state = :lifecycleState")
    fun listAllForegroundSessions(lifecycleState: String = AppNames.LIFECYCLE_STATE_FOREGROUND): List<RSession>

    @Query("SELECT * FROM sessions WHERE lifecycle_state = :lifecycleState")
    fun listAllBackgroundSessions(lifecycleState: String = AppNames.LIFECYCLE_STATE_BACKGROUND): List<RSession>

    @Query("SELECT * FROM sessions WHERE dir_name = :dirName")
    fun getWithDirName(dirName: String): List<RSession>
}
