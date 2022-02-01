package org.sinou.android.pydia.transfer

import android.util.Log
import com.pydio.cells.api.Client
import com.pydio.cells.api.ui.FileNode
import com.pydio.cells.api.ui.PageOptions
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.*
import org.sinou.android.pydia.db.browse.RTreeNode
import org.sinou.android.pydia.db.browse.TreeNodeDao
import org.sinou.android.pydia.services.NodeService
import java.io.File

class FolderDiff(
    private val client: Client,
    private val dao: TreeNodeDao,
    private val thumbDL: ThumbDownloader,
    private val parentId: StateID
) {

    companion object {

        private const val PAGE_SIZE = 100
        private val TAG = FolderDiff::class.java.simpleName

        fun firstPage(): PageOptions {
            val page = PageOptions()
            page.limit = PAGE_SIZE
            page.offset = 0
            page.currentPage = 0
            page.total = -1
            page.totalPages = -1
            return page
        }
    }

    private val folderDiffJob = Job()
    private val diffScope = CoroutineScope(Dispatchers.IO + folderDiffJob)

    private var changeNumber = 0

    /** Retrieve the meta of all readable nodes that are at the passed stateID */
    suspend fun compareWithRemote(): String? = withContext(Dispatchers.IO) {
        try {
            val remotes = RemoteNodeIterator(parentId)
            val locals = dao.getNodesForDiff(parentId.id, parentId.file).iterator()
            // val locals = LocalNodeIterator(dao.getNodesForDiff(parentId.id, parentId.file).iterator())
            processChanges(remotes, locals)
            Log.d(TAG, "Done with $changeNumber changes")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    private fun processChanges(rit: Iterator<FileNode>, lit: Iterator<RTreeNode>) {

        var local = if (lit.hasNext()) lit.next() else null
        while (rit.hasNext()) {
            val remote = rit.next()
            if (local == null) {
                putAddChange(remote)
                continue
            } else {
                var order = remote.label.compareTo(local.name)

                while (order > 0 && lit.hasNext()) { // Next local is lexicographically smaller
                    putDeleteChange(local!!)
                    local = lit.next()
                    order = remote.label.compareTo(local.name)
                }
                if (order > 0) {
                    // last local is smaller than next remote, no more matches for any next remote
                    local = null
                } else if (order == 0) {
                    if (contentAreEquals(remote, local!!)) { // Found a match, no change to report.
                        alsoCheckThumb(remote, local)
                        // Move local cursor to next and restart the loop
                        local = if (lit.hasNext()) lit.next() else null
                        continue
                    } else {
                        putUpdateChange(remote, local)
                    }
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

    private fun contentAreEquals(remote: FileNode, local: RTreeNode): Boolean {
        return remote.eTag != null
                && remote.eTag == local.etag
                && local.remoteModificationTS == remote.lastModified()
                // Also compare meta hash: timestamp is not updated when a meta changes
                && remote.metaHashCode == local.metaHash
    }

    private fun alsoCheckThumb(remote: FileNode, local: RTreeNode) {
        if (remote.isImage && local.thumbFilename == null) {
            diffScope.launch {
                val childStateID = parentId.child(remote.label)
                thumbDL.orderThumbDL(childStateID.id)
            }
        }
    }

    private fun putAddChange(remote: FileNode) {
        changeNumber++
        val childStateID = parentId.child(remote.label)
        val rNode = NodeService.toRTreeNode(childStateID, remote)
        dao.insert(rNode)
        if (remote.isImage) {
            diffScope.launch {
                thumbDL.orderThumbDL(childStateID.id)
            }
        }
    }

    private fun putUpdateChange(remote: FileNode, local: RTreeNode) {
        changeNumber++

        // TODO: Insure corner cases are correctly handled, typically on type switch
        val childStateID = parentId.child(remote.label)
        val rNode = NodeService.toRTreeNode(childStateID, remote)

        if (local.isFolder() && remote.isFile) {
            dao.delete(local.encodedState)
            dao.deleteUnder(local.encodedState)
            // TODO also delete  thumbs **and** local files that have been created for this folder
//            val file = File(it)
//            if (file.exists()) {
//                file.delete()
//            }
            dao.insert(rNode)
        } else {
            dao.update(rNode)
        }
        if (remote.isImage) {
            diffScope.launch {
                thumbDL.orderThumbDL(childStateID.id)
            }
        }
    }

    private fun putDeleteChange(local: RTreeNode) {
        changeNumber++

        // Also remove:
        // folder recursively
        // thumbs and thumb for children if we are in the case of a folder
        when {
            local.isFolder() -> {
                deleteLocalFolder(local)
            }
            else -> deleteLocalFile(local)
        }

    }

    private fun deleteLocalFile(local: RTreeNode) {
        // Cache or Offline
        NodeService.getLocalPath(local, NodeService.TYPE_CACHED_FILE)?.let {
            val file = File(it)
            if (file.exists()) {
                file.delete()
            }
        }
        // thumbs
        NodeService.getLocalPath(local, NodeService.TYPE_THUMB)?.let {
            val tf = File(it)
            if (tf.exists()) {
                tf.delete()
            }
        }
        dao.delete(local.encodedState)
    }

    private fun deleteLocalFolder(local: RTreeNode) {
        // Cache or Offline
        NodeService.getLocalPath(local, NodeService.TYPE_CACHED_FILE)?.let {
            val file = File(it)
            if (file.exists()) {
                file.deleteRecursively()
            }
        }
        // TODO look for all thumbs defined for pre-viewable files that are in the child path
        //   and remove them.
        dao.delete(local.encodedState)
        dao.deleteUnder(local.encodedState)
    }

    // Temp wrapper to add more logs
//    inner class LocalNodeIterator(private val nodes: Iterator<RTreeNode>) : Iterator<RTreeNode> {
//        override fun hasNext(): Boolean {
//            return nodes.hasNext()
//        }
//
//        override fun next(): RTreeNode {
//            val next = nodes.next()
//            Log.i(TAG, "Local: ${next.name}")
//            return next
//        }
//    }

    inner class RemoteNodeIterator(private val parentId: StateID) : Iterator<FileNode> {

        private val nodes = mutableListOf<FileNode>()

        private var nodeIterator = nodes.iterator()
        private var nextPage = firstPage()

        override fun hasNext(): Boolean {
            if (nodeIterator.hasNext()) {
                return true
            }

            if (nextPage.currentPage != nextPage.totalPages) {
                getNextPage(nextPage)
                nodeIterator = nodes.iterator()
            }

            return nodeIterator.hasNext()
        }

        override fun next(): FileNode {
            return nodeIterator.next()
        }

        private fun getNextPage(page: PageOptions) {
            nodes.clear()
            nextPage = client.ls(parentId.workspace, parentId.file, page) {
                if (it !is FileNode) {
                    Log.w(TAG, "could not store node: $it")
                } else {
                    nodes.add(it)
                }
            }
        }
    }
}
