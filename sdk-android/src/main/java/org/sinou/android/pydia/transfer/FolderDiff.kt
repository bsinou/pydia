package org.sinou.android.pydia.transfer

import android.util.Log
import com.pydio.cells.api.Client
import com.pydio.cells.api.SDKException
import com.pydio.cells.api.ui.FileNode
import com.pydio.cells.api.ui.PageOptions
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.*
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.db.nodes.RTreeNode
import org.sinou.android.pydia.db.nodes.TreeNodeDao
import org.sinou.android.pydia.services.FileService
import org.sinou.android.pydia.utils.areNodeContentEquals
import java.io.File

class FolderDiff(
    private val client: Client,
    private val fileService: FileService,
    private val dao: TreeNodeDao,
    private val fileDL: FileDownloader?,
    private val thumbDL: ThumbDownloader,
    private val parentId: StateID,
) {

    private val tag = FolderDiff::class.java.simpleName

    companion object {

        private const val PAGE_SIZE = 100

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
    @Throws(SDKException::class)
    suspend fun compareWithRemote() = withContext(Dispatchers.IO) {
        val remotes = RemoteNodeIterator(parentId)
        val locals = dao.getNodesForDiff(parentId.id, parentId.file).iterator()
        processChanges(remotes, locals)
        if (changeNumber > 0) {
            Log.d(tag, "Synced folder at $parentId with $changeNumber changes")
        }
        return@withContext changeNumber
    }

    private fun processChanges(rit: Iterator<FileNode>, lit: Iterator<RTreeNode>) {

        var local = if (lit.hasNext()) lit.next() else null
        while (rit.hasNext()) {
            val remote = rit.next()
            if (local == null) {
                putAddChange(remote)
                continue
            } else {
                var order = remote.name.compareTo(local.name)

                while (order > 0 && lit.hasNext()) { // Next local is lexicographically smaller
                    putDeleteChange(local!!)
                    local = lit.next()
                    order = remote.name.compareTo(local.name)
                }
                if (order > 0) {
                    // last local is smaller than next remote, no more matches for any next remote
                    local = null
                } else if (order == 0) {
                    if (areNodeContentEquals(remote, local!!, client.isLegacy)) {
                        // Found a match with no detected change,
                        // we yet insure necessary files (offline & thumbs for viewable) are present
                        alsoCheckFile(local)
                        alsoCheckThumb(remote, local)
                    } else {

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

    private fun putAddChange(remote: FileNode) {
        Log.d(tag, "add for ${remote.name}")
        changeNumber++
        val childStateID = parentId.child(remote.name)
        val rNode = RTreeNode.fromFileNode(childStateID, remote)
        dao.insert(rNode)
        downloadFilesIfNecessary(remote, childStateID)
    }

    private fun putUpdateChange(remote: FileNode, local: RTreeNode) {
        Log.d(tag, "update for ${remote.name}")

        changeNumber++

        // TODO: Insure corner cases are correctly handled, typically on type switch
        val childStateID = parentId.child(remote.name)
        val rNode = RTreeNode.fromFileNode(childStateID, remote)

        if (local.isFolder() && remote.isFile) {
            deleteLocalFolder(local)
            dao.insert(rNode)
        } else {
            dao.update(rNode)
        }
        downloadFilesIfNecessary(remote, childStateID)
    }

    private fun putDeleteChange(local: RTreeNode) {
        Log.d(tag, "delete for ${local.name}")
        changeNumber++

        when {
            local.isFolder() -> {
                deleteLocalFolder(local)
            }
            else -> deleteLocalFile(local)
        }
    }

    /* LOCAL HELPERS */

    private fun alsoCheckFile(local: RTreeNode) {
        fileDL?.let {
            diffScope.launch {
                if (local.isFolder()) { // we do only files
                    return@launch
                }
                var doIt = local.localFilePath == null
                if (!doIt) { // we might have recorded the name but have a missing file
                    doIt = File(local.localFilePath!!).exists()
                }
                if (doIt) {
                    it.orderFileDL(local.encodedState)
                }
            }
        }
    }

    private fun alsoCheckThumb(remote: FileNode, local: RTreeNode) {
        // FIXME we miss the event when we have a filename but the thumb is not present
        if (remote.isImage && local.thumbFilename == null) {
            diffScope.launch {
                val childStateID = parentId.child(remote.name)
                thumbDL.orderThumbDL(childStateID.id)
            }
        }
    }

    private fun downloadFilesIfNecessary(remote: FileNode, stateID: StateID) {
        if (remote.isImage) {
            diffScope.launch {
                thumbDL.orderThumbDL(stateID.id)
            }
        }
        fileDL?.let {
            diffScope.launch {
                it.orderFileDL(stateID.id)
            }
        }
    }

    private fun deleteLocalFile(local: RTreeNode) {
        // Local cache or offline file
        val file = File(fileService.getLocalPath(local, getCurrentFileType()))
        if (file.exists()) {
            file.delete()
        }
        // Corresponding thumb
        fileService.getThumbPath(local)?.let {
            val tf = File(it)
            if (tf.exists()) {
                tf.delete()
            }
        }
        dao.delete(local.encodedState)
    }

    private fun deleteLocalFolder(local: RTreeNode) {

        val file = File(fileService.getLocalPath(local, getCurrentFileType()))
        if (file.exists()) {
            file.deleteRecursively()
        }

        // TODO look for all thumbs defined for pre-viewable files that are in the child path
        //   and remove them.
        dao.deleteUnder(local.encodedState)
        dao.delete(local.encodedState)
    }

    /** Detect current context (offline or cache) relying on the presence of a FileDownloader */
    private fun getCurrentFileType(): String {
        val isOfflineContext = fileDL != null
        return if (isOfflineContext)
            AppNames.LOCAL_FILE_TYPE_CACHE
        else
            AppNames.LOCAL_FILE_TYPE_OFFLINE
    }

    /**
     * Convenience class that iterates on remote pages to list the full content of
     * a remote workspace or folder
     */
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

            if (client.isLegacy) {
                getAllSorted()
            } else {
                nextPage = client.ls(parentId.workspace, parentId.file, page) {
                    if (it !is FileNode) {
                        Log.w(tag, "could not store node: $it")
                    } else {
                        nodes.add(it)
                    }
                }
            }
        }

        // P8 specific, we must retrieve all nodes at this point and sort them for our
        // diff algorithm to work
        private fun getAllSorted() {
            val unsorted = mutableListOf<FileNode>()
            while (nextPage.currentPage != nextPage.totalPages) {
                nextPage = client.ls(parentId.workspace, parentId.file, nextPage) {
                    if (it !is FileNode) {
                        Log.w(tag, "could not store node: $it")
                    } else {
                        unsorted.add(it)
                    }
                }
                nodes.addAll(unsorted.sorted())
                nodeIterator = nodes.iterator()
            }
        }
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
}
