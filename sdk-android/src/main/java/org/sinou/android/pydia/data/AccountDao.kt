package org.sinou.android.pydia.data

import androidx.room.*

@Dao
interface AccountDao {


    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): List<Account>
    //fun getAllAccounts(): LiveData<List<Account>>
/*

    @Query("SELECT * FROM accounts WHERE uid IN (:accountIds)")
    fun loadAllByIds(accountIds: IntArray): List<Account>
*/


    //    @Insert
//    suspend fun insertAll(vararg accounts: Account)
    @Insert
    fun insertAll(vararg accounts: Account): List<Long>

    //    @Insert
//    suspend fun insert(account: Account): Int
    @Insert
    fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Delete
    fun delete(account: Account)
}