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
import com.pydio.cells.api.ui.Stats
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.IoHelpers
import com.pydio.cells.utils.Str
import kotlinx.coroutines.*
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.db.browse.RTreeNode
import org.sinou.android.pydia.db.browse.TreeNodeDB
import org.sinou.android.pydia.transfer.FolderDiff
import org.sinou.android.pydia.transfer.ThumbDownloader
import org.sinou.android.pydia.utils.getMimeType
import java.io.*
import java.util.*

class NodeService(
    private val nodeDB: TreeNodeDB,
    private val accountService: AccountService
) {

    private val nodeServiceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + nodeServiceJob)
    private val mimeMap = MimeTypeMap.getSingleton()

    /* Expose DB content as LiveData for the ViewModels */

    fun ls(stateID: StateID): LiveData<List<RTreeNode>> {
        return nodeDB.treeNodeDao().ls(stateID.id, stateID.file)
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

    /* Retrieve Objects directly with suspend functions */

    suspend fun getNode(stateID: StateID): RTreeNode? = withContext(Dispatchers.IO) {
        nodeDB.treeNodeDao().getNode(stateID.id)
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


    /* Calls to query both the cache and the remote server */

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

    /** Retrieve the meta of all readable nodes that are at the passed stateID */
    suspend fun pull(stateID: StateID): String? = withContext(Dispatchers.IO) {
        try {
            val client = getClient(stateID)
            val dao = nodeDB.treeNodeDao()
            val thumbDL = ThumbDownloader(client, nodeDB, dataDir(stateID, TYPE_THUMB))
            val folderDiff = FolderDiff(client, dao, thumbDL, stateID)
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
            val file = File(getLocalPath(rTreeNode, TYPE_CACHED_FILE))
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
                isOK == null && rTreeNode.localFileType != AppNames.LOCAL_FILE_TYPE_NONE
                -> getLocalPath(rTreeNode, TYPE_CACHED_FILE)?.let { File(it) }
                isOK == null && rTreeNode.localFileType == AppNames.LOCAL_FILE_TYPE_NONE -> null
                isOK ?: false -> File(getLocalPath(rTreeNode, TYPE_CACHED_FILE)!!)
            }

            val stateID = rTreeNode.getStateID()
            val baseDir = dataDir(stateID, TYPE_OFFLINE_FILE)
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
                rTreeNode.localModificationTS = Calendar.getInstance().timeInMillis / 1000L

                nodeDB.treeNodeDao().update(rTreeNode)
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

    fun enqueueDownload(stateID: StateID, uri: Uri) {
        serviceScope.launch {
            saveToSharedStorage(stateID, uri)
        }
    }

    private suspend fun saveToSharedStorage(stateID: StateID, uri: Uri) =
        withContext(Dispatchers.IO) {

            val rTreeNode = nodeDB.treeNodeDao().getNode(stateID.id) ?: return@withContext
            val resolver = CellsApp.instance.contentResolver
            var out: OutputStream? = null
            try {
                out = resolver.openOutputStream(uri)
                if (isCacheVersionUpToDate(rTreeNode) ?: return@withContext) {
                    var input: InputStream? = null
                    try {
                        input = FileInputStream(getLocalFile(rTreeNode, TYPE_CACHED_FILE)!!)
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


//    suspend fun saveToExternalStorage(rTreeNode: RTreeNode) = withContext(Dispatchers.IO) {
//
//        val parent = CellsApp.instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
//        if (parent == null) {
//            // TODO manage this better
//            Log.e(TAG, "no external storage found")
//            return@withContext
//        }
//        val stateID = rTreeNode.getStateID()
//        val resolver = CellsApp.instance.contentResolver
//        val toUri = getSharedStorageTargetUri(resolver, rTreeNode)
//        // File("${parent.absolutePath}/${rTreeNode.name}")
//        var out: OutputStream? = null
//        if (toUri == null) {
//            // TODO manage this better
//            Log.e(TAG, "could not generate shared storage Uri")
//            return@withContext
//        }
//
//        try {
//            out = resolver.openOutputStream(toUri)
//            if (isCacheVersionUpToDate(rTreeNode) ?: return@withContext) {
//                var input: InputStream? = null
//                try {
//                    input = FileInputStream(getLocalFile(rTreeNode, TYPE_CACHED_FILE)!!)
//                    IoHelpers.pipeRead(input, out)
//                } finally {
//                    IoHelpers.closeQuietly(input)
//                }
//                // File(getLocalPath(rTreeNode, AppNames.LOCAL_FILE_TYPE_CACHE)).copyTo(to, true)
//            } else {
//                // Directly download to final destination
//                val sf = accountService.sessionFactory
//                val client: Client = sf.getUnlockedClient(stateID.accountId)
//                // TODO handle progress
//                client.download(stateID.workspace, stateID.file, out, null)
//            }
//            Log.i(TAG, "... File has been copied to ${toUri.path}")
//
//        } catch (se: SDKException) { // Could not retrieve thumb, failing silently for the end user
//            Log.e(TAG, "could not perform DL for " + stateID.id)
//            se.printStackTrace()
//        } catch (ioe: IOException) {
//            // TODO handle this: what should we do ?
//            Log.e(TAG, "cannot write at ${toUri.path}: ${ioe.message}")
//            ioe.printStackTrace()
//        } finally {
//            IoHelpers.closeQuietly(out)
//        }
//    }


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


    /* Constants and helpers */
    private fun getClient(stateId: StateID): Client {
        return accountService.sessionFactory.getUnlockedClient(stateId.accountId)
    }

    private fun persistUpdated(rTreeNode: RTreeNode) {
        rTreeNode.localModificationTS = Calendar.getInstance().timeInMillis / 1000L
        nodeDB.treeNodeDao().update(rTreeNode)
    }

    //    private fun handleSdkException(msg: String, se: SDKException): SDKException {
    private fun handleSdkException(msg: String, se: SDKException) {
        Log.e(TAG, msg)
        Log.e(TAG, "Error code: ${se.code}")
        se.printStackTrace()
//         return se
    }

    fun enqueueUpload(parentID: StateID, uri: Uri) {
        serviceScope.launch {

            var name: String? = null
            var size: Long? = null


            val cr = CellsApp.instance.contentResolver
            cr.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                name = cursor.getString(nameIndex)
                size = cursor.getLong(sizeIndex)
            }

            name = name ?: uri.lastPathSegment!!

            Log.i(TAG, "Received enqueue call for $uri at $parentID")
            Log.i(TAG, "Received file name: $name")
            Log.i(TAG, "Received file size: $size")

            if (Str.empty(name)) {
                return@launch
            }
            val filename = name!!

            // TODO improve
            var inputStream: InputStream? = null
            try {
                inputStream = cr.openInputStream(uri)
                val mime = cr.getType(uri)
                mimeMap.getExtensionFromMimeType(mime)?.let {
                    // TODO make a better check
                    //   - retrieve file extension
                    //   - only append if the extension seems to be invalid
                    if (!filename.endsWith(it, true)) {
                        name += ".$it"
                    }
                }
                val error = uploadAt(parentID, name!!, inputStream!!)
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            }
        }
    }

    companion object {
        private const val TAG = "NodeService"
        const val TYPE_THUMB = "thumb"
        const val TYPE_CACHED_FILE = "cached_file"
        const val TYPE_OFFLINE_FILE = "offline_file"

        private const val THUMB_PARENT_DIR = "thumbs"
        private const val CACHED_FILE_PARENT_DIR = "cached"
        private const val OFFLINE_FILE_PARENT_DIR = "files"

        fun dataDir(stateID: StateID, type: String): File {
            val ps = File.separator

            return when (type) {
                TYPE_THUMB -> File(
                    CellsApp.instance.cacheDir.absolutePath
                            + ps + stateID.accountId + ps + THUMB_PARENT_DIR
                )
                TYPE_CACHED_FILE -> File(
                    CellsApp.instance.cacheDir.absolutePath
                            + ps + stateID.accountId + ps + CACHED_FILE_PARENT_DIR
                )
                TYPE_OFFLINE_FILE -> File(
                    CellsApp.instance.filesDir.absolutePath
                            + ps + stateID.accountId + ps + OFFLINE_FILE_PARENT_DIR
                )
                else -> throw IllegalStateException("Unknown file type: $type")
            }
        }

        @Throws(java.lang.IllegalStateException::class)
        fun getLocalPath(item: RTreeNode, type: String): String? {
            val stat = StateID.fromId(item.encodedState)
            return when (type) {
                TYPE_THUMB
                -> if (Str.empty(item.thumbFilename)) {
                    null
                } else {
                    "${dataDir(stat, type)}${File.separator}${item.thumbFilename}"
                }
                TYPE_CACHED_FILE
                -> "${dataDir(stat, type)}${stat.file}"
                // So that we do not store offline files also in the cache
                TYPE_OFFLINE_FILE
                -> "${dataDir(stat, TYPE_OFFLINE_FILE)}${stat.file}"
                else -> throw IllegalStateException("Unable to generate local path for $type file: ${item.encodedState} ")
            }
        }

        fun getLocalFile(item: RTreeNode, type: String): File? {

            // Trick so that we do not store offline files also in the cache... double check
            if (type == TYPE_CACHED_FILE && item.localFileType == AppNames.LOCAL_FILE_TYPE_OFFLINE
                || type == TYPE_OFFLINE_FILE
            ) {
                val p = getLocalPath(item, TYPE_OFFLINE_FILE)
                return if (p != null) {
                    File(p)
                } else {
                    null
                }
            }

            val p = getLocalPath(item, type)
            return if (p != null) {
                File(p)
            } else {
                null
            }
        }

        fun toRTreeNode(stateID: StateID, fileNode: FileNode): RTreeNode {
            val node = RTreeNode(
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
                metaHash = fileNode.metaHashCode
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
}

