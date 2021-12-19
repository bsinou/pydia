package org.sinou.android.pydia.room.account

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SessionDao {

    @Insert
    fun insert(session: Session)

    @Update
    fun update(session: Session)

    @Query("SELECT * FROM session_table WHERE account_id = :accountID LIMIT 1")
    fun getSession(accountID: String): Session?

    @Query("SELECT * FROM session_table WHERE lifecycle_state = 'foreground' LIMIT 1")
    fun getForegroundSession(): Session?

    @Query("SELECT * FROM session_table WHERE lifecycle_state = 'background'")
    fun getBackgroundSessions(): List<Session>

}