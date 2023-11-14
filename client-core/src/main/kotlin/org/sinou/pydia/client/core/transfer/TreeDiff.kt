package org.sinou.pydia.client.core.transfer

import android.util.Log
import org.sinou.pydia.client.core.AppNames
import org.sinou.pydia.client.core.db.nodes.RTreeNode
import org.sinou.pydia.client.core.db.nodes.TreeNodeDao
import org.sinou.pydia.client.core.services.CoroutineService
import org.sinou.pydia.client.core.services.FileService
import org.sinou.pydia.client.core.services.NetworkService
import org.sinou.pydia.client.core.utils.areNodeContentEquals
import org.sinou.pydia.client.core.utils.currentTimestamp
import org.sinou.pydia.sdk.api.Client
import org.sinou.pydia.sdk.api.ErrorCodes
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.ui.PageOptions
import org.sinou.pydia.sdk.transport.StateID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class TreeDiff(
    private val baseFolderStateId: StateID,
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
//                total = -1,
//                currentPage = 0,
//                totalPages = -1
            )

        }
    }
//
//    private val coroutineService: CoroutineService by inject()
//    private val diffScope = coroutineService.cellsIoScope
//
//    private val networkService: NetworkService by inject()
//    private val nodeService: NodeService by inject()
//    private val fileService: FileService by inject()
//
//    private var alsoCheckFiles = fileDL != null
//
//    private var changeNumber = 0
//
//    /** Retrieve the meta of all readable nodes that are at the passed stateID */
//    @Throws(SDKException::class)
//    suspend fun compareWithRemote() = withContext(Dispatchers.IO) {
//        if (alsoCheckFiles) {
//            Log.i(logTag, "Launching diff for with check file $baseFolderStateId")
//        }
//        // First insure node has not been erased on the server since last visit
//        val local = dao.getNode(baseFolderStateId.id)
//        val remote: FileNode? = try {
//            client.nodeInfo(baseFolderStateId.slug!!, baseFolderStateId.file!!)
//        } catch (e: SDKException) {
//            val msg = "Stat failed at $baseFolderStateId with error ${e.code}: ${e.message}"
//            Log.e(logTag, msg)
//            // Corner case: connection failed, we just return with no change
//            if (e.isAuthorizationError || e.isNetworkError) {
//                throw e
//            }
//
//            if (!networkService.isConnected()) {
//                throw SDKException(
//                    ErrorCodes.no_internet,
//                    "Cannot compare with no connection to the server"
//                )
//            }
//
//            // We have an unexpected error. Yet we do not want to swallow it.
//            Log.e(logTag, "Unexpected error: $msg \n   Yet throwing forward")
//            throw e
//        }
//
//        if (remote == null) {
//            local?.let {
//                putDeleteChange(it)
//                return@withContext 1
//            }
//            return@withContext 0
//        }
//
//        // Then perform real diff
//        if (remote.isFolder) {
//            handleFolder(remote, local)
//        } else {
//            when {
//                local == null -> {
//                    putAddChange(remote)
//                }
//
//                areNodeContentEquals(remote, local, false) -> {
//                    if (alsoCheckFiles) {
//                        checkFiles(local.getStateID(), remote)
//                    }
//                }
//
//                else -> {
//                    putUpdateChange(remote, local)
//                }
//            }
//        }
//
//        if (changeNumber > 0) {
//            Log.d(logTag, "Synced node at $baseFolderStateId with $changeNumber changes")
//        }
//
//        return@withContext changeNumber
//    }
//
//    private suspend fun handleFolder(remote: FileNode, local: RTreeNode?) {
//        val remotes = RemoteNodeIterator(baseFolderStateId)
//
//        if (baseFolderStateId.file.isNullOrEmpty()){
//            throw IllegalArgumentException("Cannot handle folder that has an empty path")
//        }
//        val locals = dao.getNodesForDiff(baseFolderStateId.id, baseFolderStateId.file!!).iterator()
//        processChanges(remotes, locals)
//
//        // Update info for current folder
//        if (baseFolderStateId.file == "/") {
//            // TODO we must perform better checks for workspace roots
//            nodeService.getNode(baseFolderStateId)?.let {
//                it.lastCheckTS = currentTimestamp()
//                dao.update(it)
//            }
//        } else if (baseFolderStateId.file != null) {
//            if (local == null ||
//                !areNodeContentEquals(remote, local, false) ||
//                changeNumber > 0
//            ) {
//                nodeService.upsertNode(
//                    RTreeNode.fromFileNode(baseFolderStateId, remote),
//                    true
//                )
//            } else { // Simply update last time checked TS on local object
//                nodeService.getNode(baseFolderStateId)?.let {
//                    it.lastCheckTS = currentTimestamp()
//                    dao.update(it)
//                }
//            }
//        }
//    }
//
//    private suspend fun processChanges(rit: Iterator<FileNode>, lit: Iterator<RTreeNode>) {
//
//        var local = if (lit.hasNext()) lit.next() else null
//        while (rit.hasNext()) {
//            val remote = rit.next()
//            if (local == null) {
//                putAddChange(remote)
//                continue
//            } else {
//                var order = remote.name.compareTo(local.name)
//
//                while (order > 0 && local != null) { // Next local is lexicographically smaller
//                    putDeleteChange(local)
//                    if (lit.hasNext()) {
//                        local = lit.next()
//                        order = remote.name.compareTo(local.name)
//                    } else {
//                        local = null
//                        continue
//                    }
//                }
//
//                if (order == 0) {
//                    if (areNodeContentEquals(remote, local!!, false)) {
//                        checkFiles(local.getStateID(), remote)
//                    } else {
//                        putUpdateChange(remote, local)
//                    }
//                    // Move local cursor to next and restart the loop
//                    local = if (lit.hasNext()) lit.next() else null
//                    continue
//                } else {
//                    putAddChange(remote)
//                    continue
//                }
//            }
//        }
//
//        // Delete remaining local nodes that have name greater than the last remote node
//        local?.let { putDeleteChange(it) }
//        while (lit.hasNext()) {
//            local = lit.next()
//            putDeleteChange(local)
//        }
//    }
//
//    private suspend fun putAddChange(remote: FileNode) {
//        // Log.d(logTag, "add for ${remote.name}")
//        changeNumber++
//        val childStateID = baseFolderStateId.child(remote.name)
//        val rNode = RTreeNode.fromFileNode(childStateID, remote)
//        nodeService.upsertNode(rNode)
//        checkFiles(childStateID, remote)
//    }
//
//    private suspend fun putUpdateChange(remote: FileNode, local: RTreeNode) {
//        // Log.d(logTag, "Updating ${remote.name} - ${remote.path}")
//
//        changeNumber++
//
//        // TODO: Insure corner cases are correctly handled, typically on type switch
//        val childStateID = baseFolderStateId.child(remote.name)
//        val rNode = RTreeNode.fromFileNode(childStateID, remote)
//        if (local.isFolder() && remote.isFile) {
//            deleteLocalFolder(local)
//        }
//        nodeService.upsertNode(rNode)
//        checkFiles(childStateID, remote)
//    }
//
//    private fun putDeleteChange(local: RTreeNode) {
//        Log.d(logTag, "delete for ${local.name}")
//        changeNumber++
//        when {
//            local.isFolder() -> deleteLocalFolder(local)
//            else -> deleteLocalFile(local)
//        }
//    }
//
//    /* LOCAL HELPERS */
//    private fun checkFiles(stateID: StateID, remote: FileNode) {
//
//        if (!alsoCheckFiles || fileDL == null) {
//            return
//        }
//
//        if (remote.hasThumb() &&
//            fileService.needsUpdate(stateID, remote, AppNames.LOCAL_FILE_TYPE_THUMB)
//        ) {
//            diffScope.launch {
//                // Log.e(logTag, "Launching thumb DL")
//                fileDL.orderDL(stateID.id, AppNames.LOCAL_FILE_TYPE_THUMB)
//            }
//            changeNumber++
//        }
//
//        if (remote.isPreViewable &&
//            fileService.needsUpdate(stateID, remote, AppNames.LOCAL_FILE_TYPE_PREVIEW)
//        ) {
//            diffScope.launch {
//                // Log.e(logTag, "Launching preview DL")
//                fileDL.orderDL(stateID.id, AppNames.LOCAL_FILE_TYPE_PREVIEW)
//            }
//            changeNumber++
//        }
//
//        if (remote.isFile &&
//            fileService.needsUpdate(stateID, remote, AppNames.LOCAL_FILE_TYPE_FILE)
//        ) {
//            diffScope.launch {
//                // Log.e(logTag, "Launching file DL")
//                fileDL.orderDL(stateID.id, AppNames.LOCAL_FILE_TYPE_FILE, remote.size)
//            }
//            changeNumber++
//        }
//    }
//
//    private fun deleteLocalFile(local: RTreeNode) {
//        // Local thumbs and cached files
//        fileService.deleteCachedFilesFor(local)
//        // Remove from index.
//        dao.delete(local.encodedState)
//    }
//
//    private fun deleteLocalFolder(local: RTreeNode) {
//        // Local file deletion, we must use the index and delete them one by one
//        // because thumb like files are all in a single bucket
//        fileService.deleteCachedFileRecursively(local.getStateID())
//        // Also remove folders in the tree structure
//        val file = File(fileService.getLocalPath(local, AppNames.LOCAL_FILE_TYPE_FILE))
//        if (file.exists()) {
//            file.deleteRecursively()
//        }
//        // Remove current folder and children in the index
//        dao.deleteUnder(local.encodedState)
//        dao.delete(local.encodedState)
//    }
//
//    /**
//     * Convenience class that iterates on remote pages to list the full content of
//     * a remote workspace or folder
//     */
//    inner class RemoteNodeIterator(private val parentId: StateID) : Iterator<FileNode> {
//
//        private val nodes = mutableListOf<FileNode>()
//
//        private var nodeIterator = nodes.iterator()
//        private var nextPage = firstPage()
//
//        override fun hasNext(): Boolean {
//            if (nodeIterator.hasNext()) {
//                return true
//            }
//
//            if (nextPage.currentPage != nextPage.totalPages) {
//                getNextPage(nextPage)
//                nodeIterator = nodes.iterator()
//            }
//
//            return nodeIterator.hasNext()
//        }
//
//        override fun next(): FileNode {
//            return nodeIterator.next()
//        }
//
//        private fun getNextPage(page: PageOptions) {
//            nodes.clear()
//
////            if (client.isLegacy) {
////                getAllSorted()
////            } else {
//                nextPage = client.ls(parentId.slug!!, parentId.file!!, page) {
//
//                    throw IllegalArgumentException("REIMPLEMENT ME")
//
////                    if (it !is FileNode) {
////                        Log.w(logTag, "could not store node: $it")
////                    } else {
////                        nodes.add(it)
////                    }
//                }
////            }
//        }
//
////        // P8 specific, we must retrieve all nodes at this point and sort them for our
////        // diff algorithm to work
////        private fun getAllSorted() {
////            val unsorted = mutableListOf<FileNode>()
////            while (nextPage.currentPage != nextPage.totalPages) {
////                nextPage = client.ls(parentId.slug, parentId.file, nextPage) {
////                    if (it !is FileNode) {
////                        Log.w(logTag, "could not store node: $it")
////                    } else {
////                        unsorted.add(it)
////                    }
////                }
////                nodes.addAll(unsorted.sorted())
////                nodeIterator = nodes.iterator()
////            }
////        }
//    }
//
//// Temp wrapper to add more logs
////    inner class LocalNodeIterator(private val nodes: Iterator<RTreeNode>) : Iterator<RTreeNode> {
////        override fun hasNext(): Boolean {
////            return nodes.hasNext()
////        }
////
////        override fun next(): RTreeNode {
////            val next = nodes.next()
////            Log.i(TAG, "Local: ${next.name}")
////            return next
////        }
////    }
}
