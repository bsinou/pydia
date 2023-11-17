package org.sinou.pydia.client.core.services

import android.content.Context
import android.util.Log
import org.sinou.pydia.client.core.db.accounts.RSession
import org.sinou.pydia.client.core.db.accounts.SessionDao
import org.sinou.pydia.client.core.db.nodes.RTreeNode
import org.sinou.pydia.client.core.db.nodes.TreeNodeDB
import org.sinou.pydia.client.core.utils.currentTimestamp
import org.sinou.pydia.sdk.transport.StateID
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TreeNodeRepository(
    private val applicationContext: Context,
    coroutineService: CoroutineService,
    private val sessionDao: SessionDao,
) {

    private val logTag = "TreeNodeRepository"
    private val treeNodeRepoScope = coroutineService.cellsIoScope
    private val ioDispatcher = coroutineService.ioDispatcher

    // Holds a map to find DB and files for a given account
    private val _sessions = mutableMapOf<String, RSession>()
    val sessions: Map<String, RSession>
        get() = _sessions

    init {
        treeNodeRepoScope.launch {
            refreshSessionCache()
        }
    }

    suspend fun refreshSessionCache() = withContext(ioDispatcher) {
        val sessions = sessionDao.getSessions()
        var msg = "... Refreshing session cache. Known accounts:\n"
        _sessions.clear()
        for (rSession in sessions) {
            _sessions[rSession.accountID] = rSession
            msg += "\t- ${rSession.account()} at ./${rSession.dirName}\n"
        }
        Log.i(logTag, msg)
    }

    fun nodeDB(stateID: StateID): TreeNodeDB {
        // TODO cache this
        val rSession = sessions[stateID.accountId]
            ?: throw IllegalStateException("No session found for $stateID, cannot retrieve dbName")
        return TreeNodeDB.getDatabase(
            applicationContext,
            stateID.accountId,
            rSession.dbName,
        )
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

    fun persistUpdated(rTreeNode: RTreeNode, modificationTS: Long = -1) {
        rTreeNode.localModificationTS =
            if (modificationTS > 0) modificationTS else rTreeNode.remoteModificationTS
        val dao = nodeDB(rTreeNode.getStateID()).treeNodeDao()
        dao.getNode(rTreeNode.getStateID().id)
            ?.let { dao.update(rTreeNode) }
            ?: let { dao.insert(rTreeNode) }
    }

    fun persistLocallyModified(rTreeNode: RTreeNode, modificationType: String) {
        rTreeNode.localModificationTS = currentTimestamp()
        rTreeNode.localModificationStatus = modificationType
        nodeDB(rTreeNode.getStateID()).treeNodeDao().update(rTreeNode)
    }

//    suspend fun abortLocalChanges(stateID: StateID) = withContext(ioDispatcher) {
//        val node = nodeDB(stateID).treeNodeDao().getNode(stateID.id) ?: return@withContext
//        node.localModificationTS = node.remoteModificationTS
//        node.localModificationStatus = null
//        nodeDB(stateID).treeNodeDao().update(node)
//    }
}
