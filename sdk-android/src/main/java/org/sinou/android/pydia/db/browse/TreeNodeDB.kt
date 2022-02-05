package org.sinou.android.pydia.db.browse

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.ConcurrentHashMap

@Database(entities = arrayOf(RTreeNode::class), version = 1, exportSchema = false)
abstract class TreeNodeDB : RoomDatabase() {

    abstract fun treeNodeDao(): TreeNodeDao

    companion object {
        @Volatile
        private var INSTANCES: ConcurrentHashMap<String, TreeNodeDB> = ConcurrentHashMap()

        fun getDatabase(context: Context, accountId: String, dbName: String): TreeNodeDB {
            val tempInstance = INSTANCES[accountId]
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TreeNodeDB::class.java,
                    dbName
                ).build()
                INSTANCES.put(accountId, instance)
                return instance
            }
        }

        fun closeDatabase(context: Context, accountId: String, dbName: String) {
            val tempInstance = INSTANCES[accountId]
            if (tempInstance != null) {
                synchronized(this) {
                    INSTANCES.remove(accountId)
                }
                tempInstance.close()
                context.deleteDatabase(dbName)
            }
        }
    }
}
