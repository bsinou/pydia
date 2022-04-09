package org.sinou.android.pydia.services

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.db.accounts.RSession
import org.sinou.android.pydia.db.accounts.SessionDao
import org.sinou.android.pydia.db.nodes.TreeNodeDB

class TreeNodeRepository(private val applicationContext: Context, private val sessionDao: SessionDao) {

    private val logTag = TreeNodeRepository::class.simpleName
    private var treeNodeRepoJob = Job()
    private val treeNodeRepoScope = CoroutineScope(Dispatchers.IO + treeNodeRepoJob)

    // Holds a map to find DB and files for a given account
    private val _sessions = mutableMapOf<String, RSession>()
    val sessions: Map<String, RSession>
        get() = _sessions

    init {
        treeNodeRepoScope.launch {
            refreshSessionCache()
        }
    }

    suspend fun refreshSessionCache() = withContext(Dispatchers.IO) {
        Log.e(logTag, "... About to refresh session cache, we have:")

        val sessions = sessionDao.getSessions()
        _sessions.clear()
        for (rSession in sessions) {
            _sessions[rSession.accountID] = rSession
            Log.e(logTag, "   - ${rSession.accountID} at ${rSession.dirName}")
        }
    }

    fun closeNodeDb(accountId: String) {

        val accId = sessions[accountId]
            ?: throw IllegalStateException("No dir name found for $accountId")

        TreeNodeDB.closeDatabase(
            applicationContext,
            accountId,
            accId.dbName,
        )
    }
}