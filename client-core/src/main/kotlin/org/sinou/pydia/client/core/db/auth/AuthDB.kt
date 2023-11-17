package org.sinou.pydia.client.core.db.auth

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        RToken::class,
        RLegacyCredentials::class,
        ROAuthState::class,
    ],
    version = 2,
    exportSchema = true,
)

abstract class AuthDB : RoomDatabase() {

    abstract fun tokenDao(): TokenDao

    abstract fun legacyCredentialsDao(): LegacyCredentialsDao

    abstract fun authStateDao(): OAuthStateDao
}
