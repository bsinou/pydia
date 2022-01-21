package org.sinou.android.pydia.db.account

import androidx.room.*

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: RAccount)

    @Update
    fun update(account: RAccount)

    @Query("DELETE FROM account_table WHERE account_id = :accountID")
    fun forgetAccount(accountID: String)

    @Query("SELECT * FROM account_table WHERE account_id = :accountID LIMIT 1")
    fun getAccount(accountID: String): RAccount?

    @Query("SELECT * FROM account_table WHERE username = :username AND url = :url LIMIT 1")
    fun getAccount(username: String, url: String): RAccount?

    @Query("SELECT * FROM account_table")
    fun getAccounts(): List<RAccount>

}