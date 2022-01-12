package org.sinou.android.pydia.services

import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import com.pydio.cells.api.Client
import com.pydio.cells.api.CustomEncoder
import com.pydio.cells.api.SDKException
import com.pydio.cells.api.SdkNames
import com.pydio.cells.api.ui.FileNode
import com.pydio.cells.api.ui.Node
import com.pydio.cells.api.ui.PageOptions
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.room.browse.RTreeNode
import org.sinou.android.pydia.room.browse.TreeNodeDB
import org.sinou.android.pydia.transfer.ThumbDownloader
import org.sinou.android.pydia.utils.AndroidCustomEncoder
import java.io.File

class NodeService(
    private val nodeDB: TreeNodeDB,
    private val accountService: AccountService,
    private val filesDir: File
) {
    private val tag = "NodeService"

    private val encoder: CustomEncoder = AndroidCustomEncoder()
    private val utf8Slash = encoder.utf8Encode("/")

    fun ls(stateID: StateID): LiveData<List<RTreeNode>> {
        return nodeDB.treeNodeDao().ls(stateID.id, stateID.file)
    }

//    fun searchUnder(stateID: StateID): LiveData<List<RTreeNode>> {
//        var encodedId = stateID.id
//        // quick and dirty workaround to avoid listing the parent folder
//        stateID.fileName?.let{
//            encodedId += utf8Slash
//        }
//        return nodeDB.treeNodeDao().ls(encodedId)
//    }


    suspend fun pull(stateID: StateID) = withContext(Dispatchers.IO) {
        try {
            val client: Client = accountService.sessionFactory.getUnlockedClient(stateID.accountId)
            val page = firstPage()
            val dao = nodeDB.treeNodeDao()

            val downloader = ThumbDownloader(client, nodeDB, dataDir(filesDir, stateID, "thumbs"))

            val nextPage = client.ls(
                stateID.workspace, stateID.file, page
            ) { node: Node? ->

                if (node == null || node !is FileNode) {
                    Log.w(tag, "could not store node: $node")
                } else {
                    val childStateID = stateID.child(node.label)
                    val rNode = toRTreeNode(childStateID, node)
                    val old = dao.getNode(childStateID.id)
                    if (old == null) {
                        dao.insert(rNode)
                        // TODO also check if image has changed
                        if (node.isImage) {
                            launch {
                                downloader.orderThumbDL(childStateID.id)
                            }
                        }
                    } else {
                        var hasChanged = false
                        if (old.remoteModificationTS != node.lastModified()){
                            Log.e(tag, "${old.name} has changed, \n" +
                                    "old TS: ${old.remoteModificationTS}, \n" +
                                    "new TS  ${node.lastModified()}")
                            dao.update(rNode)
                            hasChanged = true
                        }
                        if (node.isImage && (hasChanged || old.thumbFilename == null)) {
                            launch {
                                downloader.orderThumbDL(childStateID.id)
                            }
                        }
                    }
                }
            }

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
            Log.e(tag, "could not perform ls for " + stateID.id)
            e.printStackTrace()
        }
    }


    companion object {
        fun firstPage(): PageOptions {
            val page = PageOptions()
            page.limit = 1000
            page.offset = 0
            page.currentPage = 1
            return page
        }

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
                mime = fileNode.mimeType,
                etag = fileNode.eTag,
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

        fun getMimeType(url: String, fallback: String = "*/*"): String {
            return MimeTypeMap.getFileExtensionFromUrl(url)
                ?.run { MimeTypeMap.getSingleton().getMimeTypeFromExtension(url.lowercase()) }
                ?: fallback
        }
    }
}
