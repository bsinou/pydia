package org.sinou.android.pydia.room.account

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AccountDao {

    @Insert
    fun insert(account: Account)

    @Update
    fun update(account: Account)

    @Query("SELECT * FROM account_table WHERE username = :username AND url = :url LIMIT 1")
    fun getAccount(username: String, url: String): Account?

    //    @Query("SELECT * FROM account_table where isActive = 1 LIMIT 1")
    @Query("SELECT * FROM account_table LIMIT 1")
    fun getActiveAccount(): Account?

    @Query("SELECT * FROM account_table")
    fun getAllAccounts(): LiveData<List<Account>>

}