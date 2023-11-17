package org.sinou.pydia.client.core.db.accounts

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        RAccount::class,
        RSession::class,
        RWorkspace::class,
    ],
    views = [RSessionView::class],
    version = 2,
    exportSchema = true,
//    autoMigrations = [
//        AutoMigration(from = 1, to = 2)
//    ],
)
abstract class AccountDB : RoomDatabase() {

    abstract fun accountDao(): AccountDao

    abstract fun sessionDao(): SessionDao

    abstract fun sessionViewDao(): SessionViewDao

    abstract fun workspaceDao(): WorkspaceDao

}
