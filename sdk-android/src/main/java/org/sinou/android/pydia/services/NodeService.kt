package org.sinou.android.pydia.services

import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import com.pydio.cells.api.*
import com.pydio.cells.api.ui.FileNode
import com.pydio.cells.api.ui.PageOptions
import com.pydio.cells.api.ui.Stats
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.IoHelpers
import kotlinx.coroutines.*
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.db.browse.RTreeNode
import org.sinou.android.pydia.db.browse.TreeNodeDB
import org.sinou.android.pydia.transfer.ThumbDownloader
import org.sinou.android.pydia.utils.getMimeType
import java.io.*
import java.util.*

class NodeService(
    private val nodeDB: TreeNodeDB,
    private val accountService: AccountService,
    private val filesDir: File
) {

    private val nodeServiceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + nodeServiceJob)

    /* Expose DB content as LiveData for the view models */

    fun ls(stateID: StateID): LiveData<List<RTreeNode>> {
        return nodeDB.treeNodeDao().ls(stateID.id, stateID.file)
    }

    suspend fun query(query: String?, stateID: StateID): List<RTreeNode> =
        withContext(Dispatchers.IO) {

            return@withContext if (query == null) {
//            val emptyResult = LiveData<List<RTreeNode>>().apply {
//                it.value = listOf<RTreeNode>()
//            }
//            return emptyResult
                // TODO should rather returns an empty list
                nodeDB.treeNodeDao().query("")
            } else {
                nodeDB.treeNodeDao().query(query)
            }
        }

    fun listChildFolders(stateID: StateID): LiveData<List<RTreeNode>> {
        return nodeDB.treeNodeDao().lsWithMime(stateID.id, stateID.file, SdkNames.NODE_MIME_FOLDER)
    }

    fun listBookmarks(accountID: StateID): LiveData<List<RTreeNode>> {
        return nodeDB.treeNodeDao().getBookmarked(accountID.id)
        // TODO also watch remote folder to get new bookmarks when online.
    }

    fun getLiveNode(stateID: StateID): LiveData<RTreeNode> =
        nodeDB.treeNodeDao().getLiveNode(stateID.id)

    suspend fun getNode(stateID: StateID): RTreeNode? = withContext(Dispatchers.IO) {
        nodeDB.treeNodeDao().getNode(stateID.id)
    }

    /* Handle communication with the remote server to refresh locally stored data */

    /** Retrieve the meta of all readable nodes that are at the passed stateID */
    suspend fun pull(stateID: StateID): String? = withContext(Dispatchers.IO) {
        try {
            val client: Client = accountService.sessionFactory.getUnlockedClient(stateID.accountId)
            val dao = nodeDB.treeNodeDao()
            val thumbDL = ThumbDownloader(client, nodeDB, dataDir(filesDir, stateID, THUMB_DIR))

            val folderDiff = FolderDiff(client, dao, thumbDL,  stateID)
            folderDiff.compareWithRemote()

            // TODO Also update parent?
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
            handleSDKException("could not perform ls for " + stateID.id, e)
            return@withContext "Cannot connect to distant server"
        }
        return@withContext null
    }

    suspend fun stateRemoteNode(stateID: StateID): Stats? {
        try {
            val client: Client = accountService.sessionFactory.getUnlockedClient(stateID.accountId)
            return client.stats(stateID.workspace, stateID.file, true)
        } catch (e: SDKException) {
            handleSDKException("could not stat at ${stateID}", e)
            return null
            // throw SDKException(ErrorCodes.not_found, "could not stat at ${stateID}", e)
        }
    }

    suspend fun isCacheVersionUpToDate(rTreeNode: RTreeNode): Boolean? {

        if (!accountService.isClientConnected(rTreeNode.encodedState)) {
            // Cannot tell without connection
            return null
            // We admit we are happy with any local version if present
            // return rTreeNode.localFilename != null
        }

        // Compare with remote if possible
        val remoteStats = stateRemoteNode(StateID.fromId(rTreeNode.encodedState)) ?: return null
        if (rTreeNode.localFilename != null && rTreeNode.localModificationTS >= remoteStats.getmTime())
            rTreeNode.localFilename?.let {
                val file = File(it)
                // TODO at this point we are not 100% sure the local file
                //  is in-line with remote, typically if update is in process
                if (file.exists()) {
                    return true
                }
            }
        return false
    }

    suspend fun getOrDownloadFileToCache(rTreeNode: RTreeNode): File? =
        withContext(Dispatchers.IO) {

            val isOK = isCacheVersionUpToDate(rTreeNode)
            when {
                isOK == null && rTreeNode.localFilename != null -> File(rTreeNode.localFilename)
                isOK == null && rTreeNode.localFilename == null -> null
                isOK ?: false -> File(rTreeNode.localFilename)
            }

            val stateID = rTreeNode.getStateID()
            val baseDir = dataDir(filesDir, stateID, FILES_DIR)
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
                rTreeNode.localFilename = targetFile.absolutePath
                rTreeNode.localModificationTS = Calendar.getInstance().timeInMillis / 1000L

                nodeDB.treeNodeDao().update(rTreeNode)
            } catch (se: SDKException) {
                // Could not retrieve thumb, failing silently for the end user
                val msg = "could not perform DL for " + stateID.id
                handleSDKException(msg, se)
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

    suspend fun handleSDKException(msg: String, se: SDKException): SDKException {
        Log.e(TAG, msg)
        Log.e(TAG, "Error code: ${se.code}")
        se.printStackTrace()
        return se
    }

    suspend fun saveToExternalStorage(rTreeNode: RTreeNode) = withContext(Dispatchers.IO) {

        val parent = CellsApp.instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (parent == null) {
            // TODO manage this better
            Log.e(TAG, "no external storage found")
            return@withContext
        }
        val to = File("${parent.absolutePath}/${rTreeNode.name}")

        if (isCacheVersionUpToDate(rTreeNode) ?: return@withContext) {
            File(rTreeNode.localFilename!!).copyTo(to, true)
            Log.i(TAG, "... File has been copied to ${to.absolutePath}")
        } else {
            // Directly download to final destination
            val stateID = rTreeNode.getStateID()
            var out: FileOutputStream? = null
            try {
                val sf = accountService.sessionFactory
                val client: Client = sf.getUnlockedClient(stateID.accountId)
                out = FileOutputStream(to)
                // TODO handle progress
                client.download(stateID.workspace, stateID.file, out, null)
                Log.i(TAG, "... File has been downloaded to ${to.absolutePath}")
            } catch (se: SDKException) { // Could not retrieve thumb, failing silently for the end user
                Log.e(TAG, "could not perform DL for " + stateID.id)
                se.printStackTrace()
            } catch (ioe: IOException) {
                // TODO handle this: what should we do ?
                Log.e(TAG, "cannot write at ${to.absolutePath}: ${ioe.message}")
                ioe.printStackTrace()
            } finally {
                IoHelpers.closeQuietly(out)
            }
        }
    }


    @Throws(SDKException::class)
    suspend fun uploadAt(stateID: StateID, fileName: String, input: InputStream) =
        withContext(Dispatchers.IO) {
            try {
                val sf = accountService.sessionFactory
                val client: Client = sf.getUnlockedClient(stateID.accountId)
                client.upload(input, 0, stateID.workspace, stateID.file, fileName, true, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            null
        }

    suspend fun toggleBookmark(rTreeNode: RTreeNode) = withContext(Dispatchers.IO) {
        val stateID = rTreeNode.getStateID()
        try {
            val sf = accountService.sessionFactory
            val client: Client = sf.getUnlockedClient(stateID.accountId)
            client.bookmark(stateID.workspace, stateID.file, !rTreeNode.isBookmarked)
            rTreeNode.isBookmarked = !rTreeNode.isBookmarked
            rTreeNode.localModificationTS = Calendar.getInstance().timeInMillis / 1000L
            nodeDB.treeNodeDao().update(rTreeNode)
        } catch (se: SDKException) { // Could not retrieve thumb, failing silently for the end user
            Log.e(TAG, "could not perform DL for " + stateID.id)
            se.printStackTrace()
            return@withContext null
        } catch (ioe: IOException) {
            Log.e(TAG, "cannot toogle bookmark for ${stateID}: ${ioe.message}")
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

    /* Constants and static helpers */

    companion object {
        private const val TAG = "NodeService"
        private const val THUMB_DIR = "thumbs"
        private const val FILES_DIR = "files"

        fun dataDir(filesDir: File, stateID: StateID, type: String): File {
            val ps = File.separator
            return File(filesDir.absolutePath + ps + stateID.accountId + ps + type)
        }

        fun toRTreeNode(stateID: StateID, fileNode: FileNode): RTreeNode {
            var node = RTreeNode(
                encodedState = stateID.id,
                workspace = stateID.workspace,
                parentPath = stateID.parentFile,
                name = stateID.fileName,
                UUID = fileNode.id,
                etag = fileNode.eTag,
                mime = fileNode.mimeType,
                size = fileNode.size,
                isBookmarked = fileNode.isBookmark,
                isShared = fileNode.isShared,
                remoteModificationTS = fileNode.lastModified(),
                meta = fileNode.properties,
            )

            // Use Android library to precise MimeType when possible
            if (SdkNames.NODE_MIME_DEFAULT.equals(node.mime)) {
                node.mime = getMimeType(node.name, SdkNames.NODE_MIME_DEFAULT)
            }

            // Add a technical name to easily have a canonical sorting by default,
            // that is: folders, files, recycle bin.
            node.sortName = when (node.mime) {
                SdkNames.NODE_MIME_FOLDER -> "2_${node.name}"
                SdkNames.NODE_MIME_RECYCLE -> "8_${node.name}"
                else -> "5_${node.name}"
            }
            return node
        }
    }

    private fun persistUpdated(rTreeNode: RTreeNode) {
        rTreeNode.localModificationTS = Calendar.getInstance().timeInMillis / 1000L
        nodeDB.treeNodeDao().update(rTreeNode)
    }
}

fun RTreeNode.getStateID(): StateID {
    return StateID.fromId(encodedState)
}

