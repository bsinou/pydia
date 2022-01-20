package org.sinou.android.pydia.room.runtime

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(RUpload::class), version = 1, exportSchema = false)
abstract class RuntimeDB : RoomDatabase() {

    abstract fun uploadDao(): UploadDao

    companion object {
        @Volatile
        private var INSTANCE: RuntimeDB? = null

        fun getDatabase(context: Context): RuntimeDB {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RuntimeDB::class.java,
                    "runtime_objects"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
