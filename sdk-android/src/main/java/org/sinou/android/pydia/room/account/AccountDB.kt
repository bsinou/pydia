package org.sinou.android.pydia.room.account

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = arrayOf(RAccount::class, RToken::class, RLegacyCredentials::class, RSession::class),
    views = arrayOf(RLiveSession::class),
    version = 1,
    exportSchema = false,
)
abstract class AccountDB : RoomDatabase() {

    abstract fun accountDao(): AccountDao

    abstract fun tokenDao(): TokenDao

    abstract fun legacyCredentialsDao(): LegacyCredentialsDao

    abstract fun sessionDao(): SessionDao

    abstract fun liveSessionDao(): LiveSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AccountDB? = null

        fun getDatabase(context: Context): AccountDB {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AccountDB::class.java,
                    "accounts"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
