package org.sinou.android.pydia.transfer

import android.util.Log
import com.pydio.cells.api.Client
import com.pydio.cells.api.SDKException
import com.pydio.cells.api.ui.Node
import com.pydio.cells.api.ui.WorkspaceNode
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.db.accounts.RWorkspace
import org.sinou.android.pydia.db.nodes.RTreeNode
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.services.FileService
import org.sinou.android.pydia.services.NodeService
import org.sinou.android.pydia.utils.areWsNodeContentEquals
import java.io.File

class WorkspaceDiff(

    private val accountId: StateID,
    private val client: Client,
    private val accountService: AccountService,
    nodeService: NodeService,
    private val fileService: FileService,

    ) {

    private val TAG = "WorkspaceDiff"

    private val wsDiffJob = Job()
    private val diffScope = CoroutineScope(Dispatchers.IO + wsDiffJob)

    private var changeNumber = 0

    private val nodeDB = nodeService.nodeDB(accountId)
    private val wsDao = accountService.accountDB.workspaceDao()

    /** Retrieve the meta of all readable nodes that are at the passed stateID */
    suspend fun compareWithRemote() = withContext(Dispatchers.IO) {
        val remotes = RemoteWsIterator()
        remotes.listRemoteWorkspaces()
        // val locals =wsDao.getWsForDiff(accountId.id).iterator()
        val locs = wsDao.getWsForDiff(accountId.id)
        val it = locs.iterator()
        Log.d(TAG, "Got a list of local workspaces: ")
        while (it.hasNext()) {
            Log.d(TAG, it.next().slug)
        }
        val locals = LocalWsIterator(locs.iterator())
        processChanges(remotes, locals)
        Log.d(TAG, "Done with $changeNumber changes")
    }

    private fun processChanges(rit: Iterator<WorkspaceNode>, lit: Iterator<RWorkspace>) {

        var local = if (lit.hasNext()) lit.next() else null
        while (rit.hasNext()) {
            val remote = rit.next()
            if (local == null) {
                putAddChange(remote)
                continue
            } else {
                var order = remote.slug.compareTo(local.slug)

                while (order > 0 && lit.hasNext()) { // Next local is lexicographically smaller
                    putDeleteChange(local!!)
                    local = lit.next()
                    order = remote.slug.compareTo(local.slug)
                }
                if (order > 0) {
                    // last local is smaller than next remote, no more matches for any next remote
                    local = null
                } else if (order == 0) {
                    if (!areWsNodeContentEquals(
                            remote,
                            local!!
                        )
                    ) { // Found a match, no change to report.
                        putUpdateChange(remote, local)
                    }
                    // Move local cursor to next and restart the loop
                    local = if (lit.hasNext()) lit.next() else null
                    continue
                } else {
                    putAddChange(remote)
                    continue
                }
            }
        }

        // Delete remaining local nodes that have name greater than the last remote node
        local?.let { putDeleteChange(it) }
        while (lit.hasNext()) {
            local = lit.next()
            putDeleteChange(local)
        }
    }

    private fun putAddChange(remote: WorkspaceNode) {
        Log.d(TAG, "add for ${remote.label}")
        changeNumber++
        // We add this both on the ws and on the node table
        val rNode = RWorkspace.createChild(accountId, remote)
        accountService.accountDB.workspaceDao().insert(rNode)
        nodeDB.treeNodeDao()
            .insert(RTreeNode.fromWorkspaceNode(StateID.fromId(rNode.encodedState), remote))
    }

    private fun putUpdateChange(remote: WorkspaceNode, local: RWorkspace) {
        Log.d(TAG, "update for ${remote.label}")
        changeNumber++
        val childStateID = accountId.child(remote.slug)
        val rNode = RTreeNode.fromWorkspaceNode(childStateID, remote)
        // TODO handle bookmarks
        nodeDB.treeNodeDao().update(rNode)
    }

    private fun putDeleteChange(local: RWorkspace) {
        Log.d(TAG, "delete for ${local.slug} (${local.label})")
        changeNumber++

        deleteLocalWorkspace(local)
    }

    private fun deleteLocalWorkspace(local: RWorkspace) {

        val suffix = "/${local.slug}"

        // delete cached files
        val cacheParPath = fileService.dataParentPath(local.getStateID(), AppNames.LOCAL_FILE_TYPE_CACHE)
        val cache = File(cacheParPath + suffix)
        if (cache.exists()) {
            cache.deleteRecursively()
        }

        // delete thumbs
        val thumbParPath = fileService.dataParentPath(local.getStateID(), AppNames.LOCAL_FILE_TYPE_THUMB)
        for (node in nodeDB.treeNodeDao().getUnder(local.encodedState)) {
            node.thumbFilename?.let {
                Log.e(TAG, "Got a file to delete: $it")
                val thumb = File("${thumbParPath}/$it")
                if (thumb.exists()) {
                    thumb.delete()
                }
            }
        }

        // remove corresponding index
        nodeDB.treeNodeDao().deleteUnder(local.encodedState)

        // delete main workspace in account DB
        accountService.accountDB.workspaceDao().forgetWorkspace(local.encodedState)

        // TODO handle offline when implemented
    }

    // Temp wrapper to add more logs
    inner class LocalWsIterator(private val nodes: Iterator<RWorkspace>) : Iterator<RWorkspace> {
        override fun hasNext(): Boolean {
            return nodes.hasNext()
        }

        override fun next(): RWorkspace {
            val next = nodes.next()
            Log.i(TAG, "Local: ${next.slug}")
            return next
        }
    }

    inner class RemoteWsIterator() : Iterator<WorkspaceNode> {
        private val nodes = mutableListOf<WorkspaceNode>()
        private lateinit var nodeIterator: Iterator<WorkspaceNode>

        @Throws(SDKException::class)
        fun listRemoteWorkspaces() {
            client.workspaceList { node: Node? ->
                if (node is WorkspaceNode) {
                    nodes.add(node)
                }
            }
            nodes.sort()

            val it = nodes.iterator()
            Log.d(TAG, "Got a list of remote workspaces: ")
            while (it.hasNext()) {
                Log.d(TAG, it.next().slug)
            }

            nodeIterator = nodes.iterator()
        }

        override fun hasNext(): Boolean {
            val hasNext = nodeIterator.hasNext()
            return hasNext
        }

        override fun next(): WorkspaceNode {
            val next = nodeIterator.next()
            Log.i(TAG, "Remote: ${next.slug}")
            return next

            // return nodeIterator.next()
        }
    }
}
