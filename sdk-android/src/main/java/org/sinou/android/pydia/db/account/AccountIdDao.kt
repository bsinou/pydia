package org.sinou.android.pydia.db.account

import androidx.room.*

@Dao
interface AccountIdDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: RAccountId)

    @Query("SELECT * FROM account_id_table WHERE account_id = :accountID LIMIT 1")
    fun get(accountID: String): RAccountId?

    @Query("SELECT * FROM account_id_table")
    fun getAll(): List<RAccountId>

    @Query("SELECT * FROM account_id_table WHERE dir_name = :dirName")
    fun getWithDirName(dirName: String): List<RAccountId>

    @Query("DELETE FROM account_id_table WHERE account_id = :accountID")
    fun forgetAccount(accountID: String)

}