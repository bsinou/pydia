package org.sinou.pydia.client.core.db.auth

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        RToken::class,
        ROAuthState::class,
    ],
    version = 3,
    exportSchema = true,
)

abstract class AuthDB : RoomDatabase() {

    abstract fun tokenDao(): TokenDao

    abstract fun authStateDao(): OAuthStateDao
}
