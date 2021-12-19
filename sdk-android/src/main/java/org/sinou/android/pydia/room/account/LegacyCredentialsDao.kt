package org.sinou.android.pydia.room.account

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query

@Dao
interface LegacyCredentialsDao {

    @Insert
    fun insert(credentials: LegacyCredentials)

    @Update
    fun update(credentials: LegacyCredentials)

    @Delete
    fun delete(credentials: LegacyCredentials)

    @Query("SELECT * FROM legacy_credentials_table WHERE account_id = :accountId LIMIT 1")
    fun getCredential(accountId: String): LegacyCredentials?

}
