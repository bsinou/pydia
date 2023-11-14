package org.sinou.pydia.client.core.db.runtime

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        RJob::class,
        RJobCancellation::class,
        RLog::class,
    ],
    version = 1,
    exportSchema = true
)

abstract class RuntimeDB : RoomDatabase() {

    abstract fun jobDao(): JobDao

    abstract fun logDao(): LogDao

    companion object {
//        val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(db: SupportSQLiteDatabase) {
//            }
//        }
    }
}
