package org.sinou.pydia.client.core.db.auth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OAuthStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(roAuthState: ROAuthState)

    @Query("SELECT * FROM oauth_states WHERE oauth_state = :state LIMIT 1")
    fun get(state: String): ROAuthState?

    @Query("DELETE FROM oauth_states WHERE oauth_state = :state")
    fun delete(state: String)
}
