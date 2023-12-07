package org.sinou.pydia.client.core.transfer

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.sinou.pydia.client.core.AppNames
import org.sinou.pydia.client.core.db.nodes.RTreeNode
import org.sinou.pydia.client.core.db.nodes.TreeNodeDao
import org.sinou.pydia.client.core.services.CoroutineService
import org.sinou.pydia.client.core.services.FileService
import org.sinou.pydia.client.core.services.NetworkService
import org.sinou.pydia.client.core.services.NodeService
import org.sinou.pydia.client.core.util.areNodeContentEquals
import org.sinou.pydia.client.core.util.currentTimestamp
import org.sinou.pydia.client.core.util.fromTreeNode
import org.sinou.pydia.openapi.model.TreeNode
import org.sinou.pydia.sdk.api.Client
import org.sinou.pydia.sdk.api.ErrorCodes
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.ui.PageOptions
import org.sinou.pydia.sdk.transport.StateID
import org.sinou.pydia.sdk.utils.Log
import java.io.File

class TreeDiff(
    private val baseStateID: StateID,
    private val client: Client,
    private val dao: TreeNodeDao,
    private val fileDL: FileDownloader?,
) : KoinComponent {

    private val logTag = "TreeDiff"

    companion object {
        private const val PAGE_SIZE = 100
        fun firstPage(): PageOptions {
            return PageOptions(
                limit = PAGE_SIZE,
                offset = 0,
            )
        }
    }

    private val coroutineService: CoroutineService by inject()
    private val diffScope = coroutineService.cellsIoScope

    private val networkService: NetworkService by inject()
    private val nodeService: NodeService by inject()
    private val fileService: FileService by inject()

    private var alsoCheckFiles = fileDL != null

    private var changeNumber = 0

    /** Retrieve the meta of all readable nodes that are at the passed stateID */
    @Throws(SDKException::class)
    suspend fun compareWithRemote() = withContext(coroutineService.ioDispatcher) {
        if (alsoCheckFiles) {
            Log.i(logTag, "Launching diff for with check file $baseStateID")
        }
        // First insure node has not been erased on the server since last visit
        val local = dao.getNode(baseStateID.id)
        val treeNode: TreeNode? = try {
            client.statNode(baseStateID)
        } catch (e: SDKException) {
            val msg = "Stat failed at $baseStateID with error ${e.code}: ${e.message}"
            Log.e(logTag, msg)
            // Corner case: connection failed, we just return with no change
            if (e.isAuthorizationError || e.isNetworkError) {
                throw e
            }

            if (!networkService.isConnected()) {
                throw SDKException(
                    ErrorCodes.no_internet,
                    "Cannot compare with no connection to the server"
                )
            }

            // We have an unexpected error. Yet we do not want to swallow it.
            Log.e(logTag, "Unexpected error: $msg \n   Yet throwing forward")
            throw e
        }

        if (treeNode == null) {
            local?.let {
                putDeleteChange(it)
                return@withContext 1
            }
            return@withContext 0
        }

        val remote = fromTreeNode(baseStateID, treeNode)
        // Then perform real diff
        if (remote.isFolder()) {
            handleFolder(local, remote)
        } else {
            when {
                local == null -> {
                    putAddChange(remote)
                }

                areNodeContentEquals(local, remote) -> {
                    if (alsoCheckFiles) {
                        checkFiles(local.getStateID(), remote)
                    }
                }

                else -> {
                    putUpdateChange(local, remote)
                }
            }
        }

        if (changeNumber > 0) {
            Log.d(logTag, "Synced node at $baseStateID with $changeNumber changes")
        }

        return@withContext changeNumber
    }

    private suspend fun handleFolder(local: RTreeNode?, remote: RTreeNode) {
        val remotes = RemoteNodeIterator(baseStateID)

        if (baseStateID.file.isNullOrEmpty()) {
            throw IllegalArgumentException("Cannot handle folder that has an empty path")
        }
        val locals = dao.getNodesForDiff(baseStateID.id, baseStateID.file!!).iterator()
        processChanges(locals, remotes)

        // Update info for current folder
        if (baseStateID.file == "/") {
            // TODO we must perform better checks for workspace roots
            nodeService.getNode(baseStateID)?.let {
                it.lastCheckTS = currentTimestamp()
                dao.update(it)
            }
        } else if (baseStateID.file != null) {
            if (local == null || !areNodeContentEquals(local, remote) ||
                changeNumber > 0
            ) {
                nodeService.upsertNode(remote, true)
            } else { // Simply update last time checked TS on local object
                nodeService.getNode(baseStateID)?.let {
                    it.lastCheckTS = currentTimestamp()
                    dao.update(it)
                }
            }
        }
    }

    private suspend fun processChanges(lit: Iterator<RTreeNode>, rit: Iterator<RTreeNode>) {

        var local = if (lit.hasNext()) lit.next() else null
        while (rit.hasNext()) {
            val remote = rit.next()
            if (local == null) {
                putAddChange(remote)
                continue
            } else {
                var order = remote.name.compareTo(local.name)

                while (order > 0 && local != null) { // Next local is lexicographically smaller
                    putDeleteChange(local)
                    if (lit.hasNext()) {
                        local = lit.next()
                        order = remote.name.compareTo(local.name)
                    } else {
                        local = null
                        continue
                    }
                }

                if (order == 0) {
                    if (areNodeContentEquals(local!!, remote)) {
                        checkFiles(local.getStateID(), remote)
                    } else {
                        putUpdateChange(local, remote)
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

    private suspend fun putAddChange(remote: RTreeNode) {
        // Log.d(logTag, "add for ${remote.name}")
        changeNumber++
        val childStateID = baseStateID.child(remote.name)
        nodeService.upsertNode(remote)
        checkFiles(childStateID, remote)
    }

    private suspend fun putUpdateChange(local: RTreeNode, remote: RTreeNode) {
        // Log.d(logTag, "Updating ${remote.name} - ${remote.path}")

        changeNumber++

        // TODO: Insure corner cases are correctly handled, typically on type switch
        val childStateID = baseStateID.child(remote.name)
        if (local.isFolder() && !remote.isFolder()) {
            deleteLocalFolder(local)
        }
        nodeService.upsertNode(remote)
        checkFiles(childStateID, remote)
    }

    private fun putDeleteChange(local: RTreeNode) {
        Log.d(logTag, "delete for ${local.name}")
        changeNumber++
        when {
            local.isFolder() -> deleteLocalFolder(local)
            else -> deleteLocalFile(local)
        }
    }

    /* LOCAL HELPERS */
    private fun checkFiles(stateID: StateID, remote: RTreeNode) {

        if (!alsoCheckFiles || fileDL == null) {
            return
        }

        if (remote.hasThumb() &&
            fileService.needsUpdate(stateID, remote, AppNames.LOCAL_FILE_TYPE_THUMB)
        ) {
            diffScope.launch {
                // Log.e(logTag, "Launching thumb DL")
                fileDL.orderDL(stateID.id, AppNames.LOCAL_FILE_TYPE_THUMB)
            }
            changeNumber++
        }

        if (remote.isPreViewable() &&
            fileService.needsUpdate(stateID, remote, AppNames.LOCAL_FILE_TYPE_PREVIEW)
        ) {
            diffScope.launch {
                // Log.e(logTag, "Launching preview DL")
                fileDL.orderDL(stateID.id, AppNames.LOCAL_FILE_TYPE_PREVIEW)
            }
            changeNumber++
        }

        if (remote.isFile() &&
            fileService.needsUpdate(stateID, remote, AppNames.LOCAL_FILE_TYPE_FILE)
        ) {
            diffScope.launch {
                // Log.e(logTag, "Launching file DL")
                fileDL.orderDL(stateID.id, AppNames.LOCAL_FILE_TYPE_FILE, remote.size)
            }
            changeNumber++
        }
    }

    private fun deleteLocalFile(local: RTreeNode) {
        // Local thumbs and cached files
        fileService.deleteCachedFilesFor(local)
        // Remove from index.
        dao.delete(local.encodedState)
    }

    private fun deleteLocalFolder(local: RTreeNode) {
        // Local file deletion, we must use the index and delete them one by one
        // because thumb like files are all in a single bucket
        fileService.deleteCachedFileRecursively(local.getStateID())
        // Also remove folders in the tree structure
        val file = File(fileService.getLocalPath(local, AppNames.LOCAL_FILE_TYPE_FILE))
        if (file.exists()) {
            file.deleteRecursively()
        }
        // Remove current folder and children in the index
        dao.deleteUnder(local.encodedState)
        dao.delete(local.encodedState)
    }

    /**
     * Convenience class that iterates on remote pages to list the full content of
     * a remote workspace or folder
     */
    inner class RemoteNodeIterator(private val parentID: StateID) : Iterator<RTreeNode> {

        private val nodes = mutableListOf<RTreeNode>()

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

        override fun next(): RTreeNode {
            return nodeIterator.next()
        }

        private fun getNextPage(page: PageOptions) {
            nodes.clear()
            nextPage = client.ls(parentID.slug!!, parentID.file!!, page) {
                nodes.add(fromTreeNode(parentID, it))
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
