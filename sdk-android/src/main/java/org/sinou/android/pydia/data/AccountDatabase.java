package org.sinou.android.pydia.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Account.class}, version = 1, exportSchema = false)
public abstract class AccountDatabase extends RoomDatabase {

    private static AccountDatabase INSTANCE;

    public static AccountDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AccountDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AccountDatabase.class, "account_db")
                            // Wipes and rebuilds instead of migrating
                            // if no Migration object.
                            // Migration is not part of this practical.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }


    public abstract AccountDao accountDao();
}
