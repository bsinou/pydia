package org.sinou.android.pydia.db.account

import androidx.room.*

@Dao
interface OAuthStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: ROAuthState)

    @Query("SELECT * FROM oauth_state_table WHERE oauth_state = :state LIMIT 1")
    fun get(state: String): ROAuthState?

    @Query("DELETE FROM oauth_state_table WHERE oauth_state = :state")
    fun delete(state: String)
}