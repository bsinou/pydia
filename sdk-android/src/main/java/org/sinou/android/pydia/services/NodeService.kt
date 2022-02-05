package org.sinou.android.pydia.services

import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
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
import org.sinou.android.pydia.db.account.RAccountId
import org.sinou.android.pydia.db.browse.RTreeNode
import org.sinou.android.pydia.db.browse.TreeNodeDB
import org.sinou.android.pydia.services.FileService.Companion.dataDir
import org.sinou.android.pydia.services.FileService.Companion.getLocalPath
import org.sinou.android.pydia.transfer.FolderDiff
import org.sinou.android.pydia.transfer.ThumbDownloader
import org.sinou.android.pydia.utils.logException
import java.io.*
import java.util.*

class NodeService(
    private val accountService: AccountService
) {

    private val mimeMap = MimeTypeMap.getSingleton()

    private val nodeServiceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + nodeServiceJob)

    private var accountIds = mutableMapOf<String, RAccountId>()

    init {
        refreshAccountIds()
    }

    fun refreshAccountIds() {
        val accounts = accountService.accountDB.accountIdDao().getAll()
        accountIds.clear()
        for (acc in accounts) {
            accountIds.put(acc.accountID, acc)
        }
    }

    fun clearIndexFor(stateID: StateID) {
        val accId = accountIds.get(stateID.accountId)
            ?: throw IllegalStateException("No dir name found for $stateID")
        TreeNodeDB.closeDatabase(
            CellsApp.instance.applicationContext,
            stateID.accountId,
            accId.dbName,
        )
        refreshAccountIds()
    }

    private fun nodeDB(stateID: StateID): TreeNodeDB {
        // TODO cache this
        val accId = accountIds.get(stateID.accountId)
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
        return nodeDB(stateID).treeNodeDao()
            .lsWithMime(stateID.id, stateID.file, SdkNames.NODE_MIME_FOLDER)
    }

    fun listBookmarks(accountID: StateID): LiveData<List<RTreeNode>> {
        return nodeDB(accountID).treeNodeDao().getBookmarked(accountID.id)
        // TODO also watch remote folder to get new bookmarks when online.
    }

    fun getLiveNode(stateID: StateID): LiveData<RTreeNode> =
        nodeDB(stateID).treeNodeDao().getLiveNode(stateID.id)

    /* Retrieve Objects directly with suspend functions */

    suspend fun getNode(stateID: StateID): RTreeNode? = withContext(Dispatchers.IO) {
        nodeDB(stateID).treeNodeDao().getNode(stateID.id)
    }

    suspend fun query(query: String?, stateID: StateID): List<RTreeNode> =
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

    /* Calls to query both the cache and the remote server */

    suspend fun toggleBookmark(rTreeNode: RTreeNode) = withContext(Dispatchers.IO) {
        val stateID = rTreeNode.getStateID()
        try {
            val sf = accountService.sessionFactory
            val client: Client = sf.getUnlockedClient(stateID.accountId)
            client.bookmark(stateID.workspace, stateID.file, !rTreeNode.isBookmarked)
            rTreeNode.isBookmarked = !rTreeNode.isBookmarked
            rTreeNode.localModificationTS = Calendar.getInstance().timeInMillis / 1000L
            nodeDB(stateID).treeNodeDao().update(rTreeNode)
        } catch (se: SDKException) { // Could not retrieve thumb, failing silently for the end user
            handleSdkException("could not perform DL for " + stateID.id, se)
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
            val sf = accountService.sessionFactory
            val client: Client = sf.getUnlockedClient(stateID.accountId)

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

    suspend fun createFolder(parentId: StateID, folderName: String) = withContext(Dispatchers.IO) {
        try {
            getClient(parentId).mkdir(parentId.workspace, parentId.file, folderName)
        } catch (e: SDKException) {
            val msg = "could not create folder at ${parentId.path}"
            handleSdkException(msg, e)
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
            val client = accountService.sessionFactory.getUnlockedClient(stateID.accountId)

            // TODO rather use a cursor than loading everything in memory...
            val nodes = mutableListOf<FileNode>()
            client.getBookmarks { node: Node? ->
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
                val currNode = RTreeNode.toRTreeNodeNew(stateID, node)
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

    /** Retrieve the meta of all readable nodes that are at the passed stateID */
    suspend fun pull(stateID: StateID): String? = withContext(Dispatchers.IO) {
        try {
            val client = getClient(stateID)
            val dao = nodeDB(stateID).treeNodeDao()

            // First handle current (parent) node
            val node = client.getNodeMeta(stateID.workspace, stateID.file)
            Log.e(TAG, "Retrieved parent node: ${node.path}")


            // Then retrieves and compare children
            // WARNING: this browse **all** files that are in the folder
            val thumbDL =
                ThumbDownloader(
                    client,
                    nodeDB(stateID),
                    dataDir(stateID, AppNames.LOCAL_FILE_TYPE_THUMB)
                )
            val folderDiff = FolderDiff(client, dao, thumbDL, stateID)
            folderDiff.compareWithRemote()
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
            handleSdkException("could not perform ls for " + stateID.id, e)
            return@withContext "Cannot connect to distant server"
        }
        return@withContext null
    }

    private fun statRemoteNode(stateID: StateID): Stats? {
        try {
            val client: Client = accountService.sessionFactory.getUnlockedClient(stateID.accountId)
            return client.stats(stateID.workspace, stateID.file, true)
        } catch (e: SDKException) {
            handleSdkException("could not stat at ${stateID}", e)
        }
        return null
    }


    suspend fun clearAccountCache(stateID: String): String? = withContext(Dispatchers.IO) {
        val account = StateID.fromId(StateID.fromId(stateID).accountId)
        try {

            // TODO Deletes Rows and Tables in the DB

            // Delete  files:
            FileService.getInstance().cleanFileCacheFor(account)
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
            getLocalPath(rTreeNode, AppNames.LOCAL_FILE_TYPE_CACHE)?.let {
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

            Log.e(TAG, "in getOrDownloadFileToCache for ${rTreeNode.name}")

            val isOK = isCacheVersionUpToDate(rTreeNode)
            when {
                isOK == null && rTreeNode.localFileType != AppNames.LOCAL_FILE_TYPE_NONE
                -> getLocalPath(rTreeNode, AppNames.LOCAL_FILE_TYPE_CACHE)?.let { File(it) }
                isOK == null && rTreeNode.localFileType == AppNames.LOCAL_FILE_TYPE_NONE -> null
                isOK ?: false -> File(getLocalPath(rTreeNode, AppNames.LOCAL_FILE_TYPE_CACHE)!!)
            }
            Log.e(TAG, "... launching download for ${rTreeNode.name}")

            val stateID = rTreeNode.getStateID()
            val baseDir = FileService.dataDir(stateID, AppNames.LOCAL_FILE_TYPE_OFFLINE)
            val targetFile = File(baseDir, stateID.path.substring(1))
            targetFile.parentFile.mkdirs()
            var out: FileOutputStream? = null
            try {
                val sf = accountService.sessionFactory
                val client: Client = sf.getUnlockedClient(stateID.accountId)
                out = FileOutputStream(targetFile)

                // TODO handle progress
                client.download(stateID.workspace, stateID.file, out, null)

                // Success persist change
                rTreeNode.localFileType = AppNames.LOCAL_FILE_TYPE_CACHE
                rTreeNode.localModificationTS = rTreeNode.remoteModificationTS
                // rTreeNode.localModificationTS = Calendar.getInstance().timeInMillis / 1000L
                nodeDB(stateID).treeNodeDao().update(rTreeNode)
                Log.e(TAG, "... download done for ${rTreeNode.name}")
            } catch (se: SDKException) {
                // Could not retrieve thumb, failing silently for the end user
                val msg = "could not perform DL for " + stateID.id
                handleSdkException(msg, se)
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

            val rTreeNode = nodeDB(stateID).treeNodeDao().getNode(stateID.id) ?: return@withContext
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
                    val sf = accountService.sessionFactory
                    val client: Client = sf.getUnlockedClient(stateID.accountId)
                    // TODO handle progress
                    client.download(stateID.workspace, stateID.file, out, null)
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

    fun enqueueUpload(parentID: StateID, uri: Uri) {
        serviceScope.launch {

            val cr = CellsApp.instance.contentResolver

            // Name and size
            var name: String? = null
            var size: Long = 0
            cr.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                name = cursor.getString(nameIndex)
                size = cursor.getLong(sizeIndex)
            }
            name = name ?: uri.lastPathSegment!!
            if (Str.empty(name)) {
                return@launch
            }
            val filename = name!!

            // Mime Type
            val mime = cr.getType(uri) ?: SdkNames.NODE_MIME_DEFAULT
            Log.e(TAG, "Enqueuing upload for $filename, MIME: [$mime]")
            mimeMap.getExtensionFromMimeType(mime)?.let {
                // TODO make a better check
                //   - retrieve file extension
                //   - only append if the extension seems to be invalid
                if (!filename.endsWith(it, true)) {
                    name += ".$it"
                }
            }

            // Real upload in single part
            var inputStream: InputStream? = null
            try {
                inputStream = cr.openInputStream(uri)
                val error = uploadAt(parentID, name!!, size, mime, inputStream!!)
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            } finally {
                IoHelpers.closeQuietly(inputStream)
            }
        }
    }

    @Throws(SDKException::class)
    suspend fun uploadAt(
        parentID: StateID, fileName: String, size: Long,
        mime: String, input: InputStream
    ) = withContext(Dispatchers.IO) {
        try {
            val sf = accountService.sessionFactory
            val client: Client = sf.getUnlockedClient(parentID.accountId)
            client.upload(
                input, size, mime, parentID.workspace, parentID.file, fileName,
                true, null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
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

    suspend fun rename(stateID: StateID, newName: String): String? = withContext(Dispatchers.IO) {
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

    @Throws(SDKException::class)
    fun remoteRestore(stateID: StateID) {
        val sf = accountService.sessionFactory
        val client: Client = sf.getUnlockedClient(stateID.accountId)
        client.restore(stateID.workspace, arrayOf<String>(stateID.path))
    }

    @Throws(SDKException::class)
    fun remoteEmptyRecycle(stateID: StateID) {
        val sf = accountService.sessionFactory
        val client: Client = sf.getUnlockedClient(stateID.accountId)
        client.emptyRecycleBin(stateID.workspace)
    }

    @Throws(SDKException::class)
    fun remoteRename(stateID: StateID, newName: String) {
        val sf = accountService.sessionFactory
        val client: Client = sf.getUnlockedClient(stateID.accountId)
        client.rename(stateID.workspace, stateID.file, newName)
    }

    @Throws(SDKException::class)
    fun remoteDelete(stateID: StateID) {
        val sf = accountService.sessionFactory
        val client: Client = sf.getUnlockedClient(stateID.accountId)
        client.delete(stateID.workspace, arrayOf<String>(stateID.file))
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
    private fun handleSdkException(msg: String, se: SDKException) {
        Log.e(TAG, msg)
        Log.e(TAG, "Error code: ${se.code}")
        se.printStackTrace()
//         return se
    }

    companion object {
        private const val TAG = "NodeService"

        fun getLocalFile(item: RTreeNode, type: String): File? {

            // Trick so that we do not store offline files also in the cache... double check
            if (type == AppNames.LOCAL_FILE_TYPE_CACHE && item.localFileType == AppNames.LOCAL_FILE_TYPE_OFFLINE
                || type == AppNames.LOCAL_FILE_TYPE_OFFLINE
            ) {
                val p = FileService.getLocalPath(item, AppNames.LOCAL_FILE_TYPE_OFFLINE)
                return if (p != null) {
                    File(p)
                } else {
                    null
                }
            }

            val p = FileService.getLocalPath(item, type)
            return if (p != null) {
                File(p)
            } else {
                null
            }
        }

//        fun toRTreeNode(stateID: StateID, fileNode: FileNode): RTreeNode {
//            val node = RTreeNode(
//                encodedState = stateID.id,
//                workspace = stateID.workspace,
//                parentPath = stateID.parentFile,
//                name = stateID.fileName,
//                UUID = fileNode.id,
//                etag = fileNode.eTag,
//                mime = fileNode.mimeType,
//                size = fileNode.size,
//                isBookmarked = fileNode.isBookmark,
//                isShared = fileNode.isShared,
//                remoteModificationTS = fileNode.getLastModified(),
//                meta = fileNode.properties,
//                metaHash = fileNode.metaHashCode
//            )
//
//            // Use Android library to precise MimeType when possible
//            if (SdkNames.NODE_MIME_DEFAULT.equals(node.mime)) {
//                node.mime = getMimeType(node.name, SdkNames.NODE_MIME_DEFAULT)
//            }
//
//            // Add a technical name to easily have a canonical sorting by default,
//            // that is: folders, files, recycle bin.
//            node.sortName = when (node.mime) {
//                SdkNames.NODE_MIME_FOLDER -> "2_${node.name}"
//                SdkNames.NODE_MIME_RECYCLE -> "8_${node.name}"
//                else -> "5_${node.name}"
//            }
//            return node
//        }
    }
}

