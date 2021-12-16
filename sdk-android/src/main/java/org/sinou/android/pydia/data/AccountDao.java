package org.sinou.android.pydia.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AccountDao {

    @Insert
    void insert(Account account);

    @Query("SELECT * FROM account_records ORDER BY login, url ASC ")
    LiveData<List<Account>> listAccounts();

    @Query("DELETE FROM account_records")
    void clearAll();

}

