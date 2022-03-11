package org.sinou.android.pydia.services

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import com.pydio.cells.api.Client
import com.pydio.cells.api.SDKException
import com.pydio.cells.api.SdkNames
import com.pydio.cells.api.ui.FileNode
import com.pydio.cells.api.ui.Node
import com.pydio.cells.api.ui.Stats
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.IoHelpers
import com.pydio.cells.utils.Str
import kotlinx.coroutines.*
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.db.nodes.*
import org.sinou.android.pydia.transfer.FileDownloader
import org.sinou.android.pydia.transfer.FolderDiff
import org.sinou.android.pydia.transfer.ThumbDownloader
import org.sinou.android.pydia.utils.logException
import java.io.*
import java.util.*

class NodeService(
    private val accountService: AccountService,
    private val fileService: FileService,
) {
    private val nodeServiceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + nodeServiceJob)

    fun nodeDB(stateID: StateID): TreeNodeDB {
        // TODO cache this
        val accId = accountService.sessions[stateID.accountId]
            ?: throw IllegalStateException("No dir name found for $stateID")
        return TreeNodeDB.getDatabase(
            CellsApp.instance.applicationContext,
            stateID.accountId,
            accId.dbName,
        )
    }

    /* Expose DB content as LiveData for the ViewModels */

    fun ls(stateID: StateID): LiveData<List<RTreeNode>> {
        return nodeDB(stateID).treeNodeDao().ls(stateID.id, stateID.file)
    }

    fun listChildFolders(stateID: StateID): LiveData<List<RTreeNode>> {
        // Tweak to also be able to list workspaces roots
        var parPath = stateID.file
        var mime = SdkNames.NODE_MIME_FOLDER
        if (Str.empty(parPath)) {
            parPath = ""
            mime = SdkNames.NODE_MIME_WS_ROOT
        } else if (parPath == "/") {

        }
        Log.i(TAG, "Listing children of $stateID: parPath: $parPath, mime: $mime")
        return nodeDB(stateID).treeNodeDao().lsWithMime(stateID.id, parPath, mime)
    }

    fun listViewable(stateID: StateID, mimeFilter: String): LiveData<List<RTreeNode>> {
        var parPath = stateID.file
        Log.i(TAG, "Listing children of $stateID: parPath: $parPath, mime: $mimeFilter")
        return nodeDB(stateID).treeNodeDao().lsWithMimeFilter(stateID.id, parPath, mimeFilter)
    }

    fun listBookmarks(accountID: StateID): LiveData<List<RTreeNode>> {
        return nodeDB(accountID).treeNodeDao().getBookmarked()
    }

    fun listOfflineRoots(stateID: StateID): LiveData<List<RLiveOfflineRoot>> {
        return nodeDB(stateID).liveOfflineRootDao().getLiveOfflineRoots()
    }

    fun getLiveNode(stateID: StateID): LiveData<RTreeNode> {
        val liveData = nodeDB(stateID).treeNodeDao().getLiveNode(stateID.id)
        if (liveData.value == null) {
            Log.e(TAG, "no node found for ${stateID.id}")
        }
        return liveData
    }

    /* Retrieve Objects directly with suspend functions */

    suspend fun getNode(stateID: StateID): RTreeNode? = withContext(Dispatchers.IO) {
        nodeDB(stateID).treeNodeDao().getNode(stateID.id)
    }

    suspend fun queryLocally(query: String?, stateID: StateID): List<RTreeNode> =
        withContext(Dispatchers.IO) {

            return@withContext if (query == null) {
//            val emptyResult = LiveData<List<RTreeNode>>().apply {
//                it.value = listOf<RTreeNode>()
//            }
//            return emptyResult
                // TODO should rather returns an empty list
                nodeDB(stateID).treeNodeDao().query("")
            } else {
                nodeDB(stateID).treeNodeDao().query(query)
            }
        }

    /* Update nodes in the local store */
    suspend fun abortLocalChanges(stateID: StateID) = withContext(Dispatchers.IO) {
        val node = nodeDB(stateID).treeNodeDao().getNode(stateID.id) ?: return@withContext
        node.localModificationTS = node.remoteModificationTS
        nodeDB(stateID).treeNodeDao().update(node)
    }

    fun clearIndexFor(stateID: StateID) {
        val accId = accountService.sessions[stateID.accountId]
            ?: throw IllegalStateException("No dir name found for $stateID")
        TreeNodeDB.closeDatabase(
            CellsApp.instance.applicationContext,
            stateID.accountId,
            accId.dbName,
        )
    }

    /* Calls to query both the cache and the remote server */

    suspend fun toggleBookmark(rTreeNode: RTreeNode) = withContext(Dispatchers.IO) {
        val stateID = rTreeNode.getStateID()
        try {
            getClient(stateID).bookmark(stateID.workspace, stateID.file, !rTreeNode.isBookmarked)
            rTreeNode.isBookmarked = !rTreeNode.isBookmarked
            rTreeNode.localModificationTS = Calendar.getInstance().timeInMillis / 1000L
            nodeDB(stateID).treeNodeDao().update(rTreeNode)
        } catch (se: SDKException) { // Could not retrieve thumb, failing silently for the end user
            handleSdkException(stateID, "could not perform DL for $stateID", se)
            return@withContext null
        } catch (ioe: IOException) {
            Log.e(TAG, "cannot toggle bookmark for ${stateID}: ${ioe.message}")
            ioe.printStackTrace()
            return@withContext null
        }
    }

    suspend fun toggleShared(rTreeNode: RTreeNode) = withContext(Dispatchers.IO) {
        val stateID = rTreeNode.getStateID()
        try {
            val client = getClient(stateID)

            if (rTreeNode.isShared) {
                client.unshare(stateID.workspace, stateID.file)
            } else {
                // TODO we put default values for the time being
                //   But we must handle this better
                client.share(
                    stateID.workspace, stateID.file, stateID.fileName,
                    "Created at ${Calendar.getInstance()}",
                    null, true, true
                )
            }

            rTreeNode.isShared = !rTreeNode.isShared
            persistUpdated(rTreeNode)
        } catch (se: SDKException) {
            Log.e(TAG, "could update share link for " + stateID.id)
            se.printStackTrace()
            return@withContext null
        } catch (ioe: IOException) {
            Log.e(TAG, "could update share link for ${stateID}: ${ioe.message}")
            ioe.printStackTrace()
            return@withContext null
        }
    }

    suspend fun toggleOffline(rTreeNode: RTreeNode) = withContext(Dispatchers.IO) {
        val stateID = rTreeNode.getStateID()
        try {
            val db = nodeDB(stateID)
            val offlineDao = db.offlineRootDao()

            if (rTreeNode.isOfflineRoot) {
                offlineDao.delete(rTreeNode.encodedState)
            } else {
                // TODO should we check if this node is already a descendant of
                //  an existing offline root ?
                val newRoot = ROfflineRoot.fromTreeNode(rTreeNode)
                offlineDao.insert(newRoot)
            }
            rTreeNode.isOfflineRoot = !rTreeNode.isOfflineRoot
            persistUpdated(rTreeNode)
        } catch (se: SDKException) {
            Log.e(TAG, "could update offline sync status for " + stateID.id)
            se.printStackTrace()
            return@withContext null
        } catch (ioe: IOException) {
            Log.e(TAG, "could update offline sync status for ${stateID}: ${ioe.message}")
            ioe.printStackTrace()
            return@withContext null
        }
    }

    suspend fun syncAll(stateID: StateID) = withContext(Dispatchers.IO) {

        val offlineDao = nodeDB(stateID).offlineRootDao()
        val roots = offlineDao.getAll()
        val dao = nodeDB(stateID).treeNodeDao()

        for (offlineRoot in roots) {
            dao.getNode(offlineRoot.encodedState)?.let {
                // TODO handle case where remote node has disappeared on server
                launchSync(it)
            }
        }

    }

    suspend fun launchSync(rTreeNode: RTreeNode) = withContext(Dispatchers.IO) {
        val stateID = rTreeNode.getStateID()
        try {
            val client = getClient(stateID)

            val db = nodeDB(stateID)
            val treeNodeDao = nodeDB(stateID).treeNodeDao()
            val offlineDao = db.offlineRootDao()
            val currRoot = offlineDao.get(rTreeNode.encodedState)
                ?: return@withContext // should never happen

            val fileDL = FileDownloader(client, nodeDB(stateID))
            val thumbs = fileService.dataParentFolder(stateID, AppNames.LOCAL_FILE_TYPE_THUMB)
            val thumbDL = ThumbDownloader(client, nodeDB(stateID), thumbs)

            val changeNb = if (rTreeNode.isFolder()) {
                syncFolderAt(rTreeNode, client, treeNodeDao, fileDL, thumbDL)
            } else {
                syncFileAt(rTreeNode, client, treeNodeDao, fileDL, thumbDL)
            }

            if (changeNb > 0) {
                currRoot.localModificationTS = System.currentTimeMillis() / 1000L
                currRoot.message = null // TODO double check
            }
            currRoot.lastCheckTS = System.currentTimeMillis() / 1000L

            offlineDao.update(currRoot)
            // TODO add more info on the corresponding root RTreeNode ??
            persistUpdated(rTreeNode)
        } catch (se: SDKException) {
            Log.e(TAG, "could update offline sync status for " + stateID.id)
            se.printStackTrace()
            return@withContext
//           } catch (ioe: IOException) {
//            Log.e(TAG, "could update offline sync status for ${stateID}: ${ioe.message}")
//            ioe.printStackTrace()
//            return@withContext
        }
    }

    private suspend fun syncFileAt(
        rTreeNode: RTreeNode,
        client: Client,
        dao: TreeNodeDao,
        fileDL: FileDownloader,
        thumbDL: ThumbDownloader
    ): Int {
        return 0
    }

    private suspend fun syncFolderAt(
        rTreeNode: RTreeNode,
        client: Client,
        dao: TreeNodeDao,
        fileDL: FileDownloader,
        thumbDL: ThumbDownloader
    ): Int {

        val stateID = rTreeNode.getStateID()

        // First re-sync current level
        val folderDiff = FolderDiff(client, fileService, dao, fileDL, thumbDL, stateID)
        var changeNb = folderDiff.compareWithRemote()

        // Then retrieve child folders and call re-sync on each one
        val children = nodeDB(stateID).treeNodeDao()
            .listWithMime(stateID.id, stateID.file, SdkNames.NODE_MIME_FOLDER)
        for (child in children) {
            changeNb += syncFolderAt(child, client, dao, fileDL, thumbDL)
        }

        return changeNb
    }

    suspend fun createFolder(parentId: StateID, folderName: String) =
        withContext(Dispatchers.IO) {
            try {
                getClient(parentId).mkdir(parentId.workspace, parentId.file, folderName)
            } catch (e: SDKException) {
                val msg = "could not create folder at ${parentId.path}"
                handleSdkException(parentId, msg, e)
                return@withContext msg
            }
            return@withContext null
        }

    suspend fun copy(sources: List<StateID>, targetParent: StateID) =
        withContext(Dispatchers.IO) {
            try {
                val srcFiles = mutableListOf<String>()
                for (source in sources) {
                    srcFiles.add(source.file)
                }
                getClient(targetParent).copy(
                    targetParent.workspace,
                    srcFiles.toTypedArray(),
                    targetParent.file
                )
            } catch (e: SDKException) {
                val msg = "could not copy to $targetParent"
                handleSdkException(targetParent, msg, e)
                return@withContext msg
            }
            return@withContext null
        }

    suspend fun move(sources: List<StateID>, targetParent: StateID) =
        withContext(Dispatchers.IO) {
            try {
                val srcFiles = mutableListOf<String>()
                for (source in sources) {
                    srcFiles.add(source.file)
                }
                getClient(targetParent).move(
                    targetParent.workspace,
                    srcFiles.toTypedArray(),
                    targetParent.file
                )
            } catch (e: SDKException) {
                val msg = "could not move to $targetParent"
                handleSdkException(targetParent, msg, e)
                return@withContext msg
            }
            return@withContext null
        }

    /* Handle communication with the remote server to refresh locally stored data */

    fun enqueueDownload(stateID: StateID, uri: Uri) {
        serviceScope.launch {
            saveToSharedStorage(stateID, uri)
        }
    }

    suspend fun refreshBookmarks(stateID: StateID): String? = withContext(Dispatchers.IO) {
        try {
            // TODO rather use a cursor than loading everything in memory...
            val nodes = mutableListOf<FileNode>()
            getClient(stateID).getBookmarks { node: Node? ->
                if (node !is FileNode) {
                    Log.w(TAG, "could not store node: $node")
                } else {
                    nodes.add(node)
                }
            }
            // Manage results
            Log.w(TAG, "Got a bookmark list of ${nodes.size}, about to process")
            val dao = nodeDB(stateID).treeNodeDao()
            for (node in nodes) {
                val currNode = RTreeNode.fromFileNode(stateID, node)
                currNode.isBookmarked = true
                val oldNode = dao.getNode(currNode.encodedState)
                if (oldNode == null) {
                    dao.insert(currNode)
                } else if (!oldNode.isBookmarked) {
                    oldNode.isBookmarked = true
                    dao.update(oldNode)
                }
            }
        } catch (se: SDKException) {
            se.printStackTrace()
            return@withContext "Could not refresh bookmarks from server: ${se.message}"
        }
        return@withContext null
    }

    /** Retrieve the meta (and thumbs) of all readable nodes that are at the passed stateID */
    suspend fun pull(stateID: StateID): Pair<Int, String?> = withContext(Dispatchers.IO) {
        var result: Pair<Int, String?>

        try {
            val client = getClient(stateID)
            val dao = nodeDB(stateID).treeNodeDao()

            // First handle current (parent) node
            // Still TODO. Rather make this in the folder diff.
            // beware of the WS root corner case
//            val node = client.getNodeMeta(stateID.workspace, stateID.file)
//            Log.e(TAG, "Retrieved parent node: ${node.path}")

            // Then retrieves and compare children
            // WARNING: this browse **all** files that are in the folder
            val thumbDL =
                ThumbDownloader(
                    client,
                    nodeDB(stateID),
                    fileService.dataParentFolder(stateID, AppNames.LOCAL_FILE_TYPE_THUMB)
                )
            val folderDiff = FolderDiff(client, fileService, dao, null, thumbDL, stateID)
            val changeNb = folderDiff.compareWithRemote()
            result = Pair(changeNb, null)
            /*
            val childStateID = stateID.child(node.label)
            val rNode = toRTreeNode(childStateID, node)
            val old = dao.getSession(childStateID.id)
            if (old == null){
                dao.insert(rNode)
            } else {
                dao.update(rNode)
            }
*/
        } catch (e: SDKException) {
            val msg = "could not perform ls for ${stateID.id}, cause: ${e.message}"
            handleSdkException(stateID, msg, e)
            return@withContext Pair(0, msg)
        }
        return@withContext result
    }

    private suspend fun statRemoteNode(stateID: StateID): Stats? {
        try {
            return getClient(stateID).stats(stateID.workspace, stateID.file, true)
        } catch (e: SDKException) {
            handleSdkException(stateID, "could not stat at $stateID", e)
        }
        return null
    }

    suspend fun clearAccountCache(stateID: String): String? = withContext(Dispatchers.IO) {
        val account = StateID.fromId(StateID.fromId(stateID).accountId)
        try {
            // TODO also delete corresponding index rows
            //  Should we use 2 distinct tables for cache and offline ?

            // Delete  files:
            fileService.cleanFileCacheFor(account)
            return@withContext null
        } catch (e: Exception) {
            val msg = "Could not delete account $account"
            logException(TAG, msg, e)
            return@withContext msg
        }
    }

    private suspend fun isCacheVersionUpToDate(rTreeNode: RTreeNode): Boolean? {

        if (!accountService.isClientConnected(rTreeNode.encodedState)) {
            // Cannot tell without connection
            return null
            // We admit we are happy with any local version if present
            // return rTreeNode.localFilename != null
        }

        // Compare with remote if possible
        val remoteStats = statRemoteNode(StateID.fromId(rTreeNode.encodedState)) ?: return null
        if (rTreeNode.localFileType != AppNames.LOCAL_FILE_TYPE_NONE
            && rTreeNode.localModificationTS >= remoteStats.getmTime()
        ) {
            fileService.getLocalPath(rTreeNode, AppNames.LOCAL_FILE_TYPE_CACHE)?.let {
                val file = File(it)
                // TODO at this point we are not 100% sure the local file
                //  is in-line with remote, typically if update is in process
                if (file.exists()) {
                    return true
                }
            }
        }
        return false
    }

    suspend fun getOrDownloadFileToCache(rTreeNode: RTreeNode): File? =

        withContext(Dispatchers.IO) {

            Log.e(TAG, "In getOrDownloadFileToCache for ${rTreeNode.name}")

            // FIXME this smells
            val isOK = isCacheVersionUpToDate(rTreeNode)
            when {
                isOK == null && rTreeNode.localFileType != AppNames.LOCAL_FILE_TYPE_NONE
                -> fileService.getLocalPath(rTreeNode, AppNames.LOCAL_FILE_TYPE_CACHE)
                    ?.let { return@withContext File(it) }
                isOK == null && rTreeNode.localFileType == AppNames.LOCAL_FILE_TYPE_NONE
                -> {
                }
                isOK ?: false
                -> return@withContext File(
                    fileService.getLocalPath(
                        rTreeNode,
                        AppNames.LOCAL_FILE_TYPE_CACHE
                    )!!
                )
            }

            Log.e(TAG, "... Launching download for ${rTreeNode.name}")

            val stateID = rTreeNode.getStateID()
            val baseDir = fileService.dataParentPath(stateID, AppNames.LOCAL_FILE_TYPE_CACHE)
            val targetFile = File(baseDir, stateID.path.substring(1))
            targetFile.parentFile!!.mkdirs()
            var out: FileOutputStream? = null

            try {
                out = FileOutputStream(targetFile)

                // TODO handle progress
                getClient(stateID).download(stateID.workspace, stateID.file, out, null)

                // Success persist change
                rTreeNode.localFileType = AppNames.LOCAL_FILE_TYPE_CACHE
                rTreeNode.localModificationTS = rTreeNode.remoteModificationTS
                // rTreeNode.localModificationTS = Calendar.getInstance().timeInMillis / 1000L
                nodeDB(stateID).treeNodeDao().update(rTreeNode)
                Log.e(TAG, "... download done for ${rTreeNode.name}")
            } catch (se: SDKException) {
                // Could not retrieve thumb, failing silently for the end user
                val msg = "could not perform DL for " + stateID.id
                handleSdkException(stateID, msg, se)
                return@withContext null
            } catch (ioe: IOException) {
                // TODO handle this: what should we do ?
                Log.e(TAG, "cannot write at ${targetFile.absolutePath}: ${ioe.message}")
                ioe.printStackTrace()
                return@withContext null
            } finally {
                IoHelpers.closeQuietly(out)
            }
            targetFile
        }

    private suspend fun saveToSharedStorage(stateID: StateID, uri: Uri) =
        withContext(Dispatchers.IO) {

            val rTreeNode =
                nodeDB(stateID).treeNodeDao().getNode(stateID.id) ?: return@withContext
            val resolver = CellsApp.instance.contentResolver
            var out: OutputStream? = null
            try {
                out = resolver.openOutputStream(uri)
                if (isCacheVersionUpToDate(rTreeNode) ?: return@withContext) {
                    var input: InputStream? = null
                    try {
                        input = FileInputStream(
                            getLocalFile(
                                rTreeNode,
                                AppNames.LOCAL_FILE_TYPE_CACHE
                            )!!
                        )
                        IoHelpers.pipeRead(input, out)
                    } finally {
                        IoHelpers.closeQuietly(input)
                    }
                    // File(getLocalPath(rTreeNode, AppNames.LOCAL_FILE_TYPE_CACHE)).copyTo(to, true)
                } else {
                    // Directly download to final destination
                    // TODO handle progress
                    getClient(stateID).download(stateID.workspace, stateID.file, out, null)
                }
                Log.i(TAG, "... File has been copied to ${uri.path}")

            } catch (se: SDKException) { // Could not retrieve thumb, failing silently for the end user
                Log.e(TAG, "could not perform DL for " + stateID.id)
                se.printStackTrace()
            } catch (ioe: IOException) {
                // TODO handle this: what should we do ?
                Log.e(TAG, "cannot write at ${uri.path}: ${ioe.message}")
                ioe.printStackTrace()
            } finally {
                IoHelpers.closeQuietly(out)
            }
        }

    suspend fun restoreNode(stateID: StateID): String? = withContext(Dispatchers.IO) {
        try {
            val node = nodeDB(stateID).treeNodeDao().getNode(stateID.id)
                ?: return@withContext "No node found at $stateID, could not restore"
            remoteRestore(stateID)
            persistLocallyModified(node, AppNames.LOCAL_MODIF_RESTORE)
        } catch (se: SDKException) {
            se.printStackTrace()
            return@withContext "Could not restore node: ${se.message}"
        }
        return@withContext null
    }

    suspend fun emptyRecycle(stateID: StateID): String? = withContext(Dispatchers.IO) {
        try {
            val node = nodeDB(stateID).treeNodeDao().getNode(stateID.id)
                ?: return@withContext "No node found at $stateID, could not delete"
            remoteEmptyRecycle(stateID)
            persistLocallyModified(node, AppNames.LOCAL_MODIF_DELETE)
        } catch (se: SDKException) {
            se.printStackTrace()
            return@withContext "Could not empty recycle bin: ${se.message}"
        }
        return@withContext null
    }

    suspend fun rename(stateID: StateID, newName: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val node = nodeDB(stateID).treeNodeDao().getNode(stateID.id)
                    ?: return@withContext "No node found at $stateID, could not rename"
                remoteRename(stateID, newName)
                persistLocallyModified(node, AppNames.LOCAL_MODIF_RENAME)
            } catch (se: SDKException) {
                se.printStackTrace()
                return@withContext "Could not delete $stateID: ${se.message}"
            }
            return@withContext null
        }

    suspend fun delete(stateID: StateID): String? = withContext(Dispatchers.IO) {
        try {
            val node = nodeDB(stateID).treeNodeDao().getNode(stateID.id)
                ?: return@withContext "No node found at $stateID, could not delete"
            remoteDelete(stateID)
            persistLocallyModified(node, AppNames.LOCAL_MODIF_DELETE)
        } catch (se: SDKException) {
            se.printStackTrace()
            return@withContext "Could not delete $stateID: ${se.message}"
        }
        return@withContext null
    }

    /* Directly communicate with the distant server */
    suspend fun remoteQuery(stateID: StateID, query: String): List<RTreeNode> =
        withContext(Dispatchers.IO) {
            try {
                return@withContext getClient(stateID).search(stateID.path, query, 20)
                    .map { RTreeNode.fromFileNode(stateID, it) }
            } catch (se: SDKException) {
                se.printStackTrace()
                return@withContext listOf()
            }
        }

    private suspend fun remoteRestore(stateID: StateID): String? = withContext(Dispatchers.IO) {
        try {
            val node = nodeDB(stateID).treeNodeDao().getNode(stateID.id)
                ?: return@withContext "No node found at $stateID, could not restore"

            val nodes = arrayOf(node.toFileNode())
            getClient(stateID).restore(stateID.workspace, nodes)

            remoteDelete(stateID)
            persistLocallyModified(node, AppNames.LOCAL_MODIF_DELETE)
        } catch (se: SDKException) {
            se.printStackTrace()
            return@withContext "Could not delete $stateID: ${se.message}"
        }
        return@withContext null
    }

    @Throws(SDKException::class)
    fun remoteEmptyRecycle(stateID: StateID) {
        getClient(stateID).emptyRecycleBin(stateID.workspace)
    }

    @Throws(SDKException::class)
    fun remoteRename(stateID: StateID, newName: String) {
        getClient(stateID).rename(stateID.workspace, stateID.file, newName)
    }

    @Throws(SDKException::class)
    fun remoteDelete(stateID: StateID) {
        getClient(stateID).delete(stateID.workspace, arrayOf<String>(stateID.file))
    }

    /* Constants and helpers */
    private fun getClient(stateId: StateID): Client {
        return accountService.sessionFactory.getUnlockedClient(stateId.accountId)
    }

    private fun persistUpdated(rTreeNode: RTreeNode) {
        rTreeNode.localModificationTS = rTreeNode.remoteModificationTS
        nodeDB(rTreeNode.getStateID()).treeNodeDao().update(rTreeNode)
    }

    private fun persistLocallyModified(rTreeNode: RTreeNode, modificationType: String) {
        rTreeNode.localModificationTS = Calendar.getInstance().timeInMillis / 1000L
        rTreeNode.localModificationStatus = modificationType
        nodeDB(rTreeNode.getStateID()).treeNodeDao().update(rTreeNode)
    }

    //    private fun handleSdkException(msg: String, se: SDKException): SDKException {
    private suspend fun handleSdkException(stateID: StateID, msg: String, se: SDKException) {
        Log.e(TAG, msg)
        Log.e(TAG, "Error code: ${se.code}")
        accountService.notifyError(stateID, se.code)

        se.printStackTrace()
    }

    companion object {
        private const val TAG = "NodeService"

        fun getLocalFile(item: RTreeNode, type: String): File {
            val fs = CellsApp.instance.fileService
            // Trick so that we do not store offline files also in the cache... double check
            if (type == AppNames.LOCAL_FILE_TYPE_OFFLINE ||
                type == AppNames.LOCAL_FILE_TYPE_CACHE &&
                item.localFileType == AppNames.LOCAL_FILE_TYPE_OFFLINE
            ) {
                return File(fs.getLocalPath(item, AppNames.LOCAL_FILE_TYPE_OFFLINE))
            }
            return File(fs.getLocalPath(item, type))
        }
    }
}

