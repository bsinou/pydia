package org.sinou.android.pydia.room.account

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TokenDao {

    @Insert
    fun insert(RToken: RToken)

    @Update
    fun update(RToken: RToken)

    @Query("SELECT * FROM token_table WHERE account_id = :accountID LIMIT 1")
    fun getToken(accountID: String): RToken?

    @Query("DELETE FROM token_table WHERE account_id = :accountID")
    fun forgetToken(accountID: String)
}