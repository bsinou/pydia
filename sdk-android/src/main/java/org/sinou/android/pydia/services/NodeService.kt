package org.sinou.android.pydia.services

import android.util.Log
import androidx.lifecycle.LiveData
import com.pydio.cells.api.Client
import com.pydio.cells.api.CustomEncoder
import com.pydio.cells.api.SDKException
import com.pydio.cells.api.ui.FileNode
import com.pydio.cells.api.ui.Node
import com.pydio.cells.api.ui.PageOptions
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.room.browse.RTreeNode
import org.sinou.android.pydia.room.browse.TreeNodeDB
import org.sinou.android.pydia.utils.AndroidCustomEncoder

class NodeService(
    private val nodeDB: TreeNodeDB,
    private val accountService: AccountService,
    private val workingDir: String?
) {
    private val TAG = "NodeService"

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

            val nextPage = client.ls(
                stateID.workspace, stateID.file, page
            ) { node: Node? ->

                if (node == null || node !is FileNode) {
                    Log.w(TAG, "could not store node: $node")
                } else {
                    val childStateID = stateID.child(node.label)
                    val rNode = toRTreeNode(childStateID, node)
                    val old = dao.getNode(childStateID.id)
                    if (old == null) {
                        //Log.i(TAG, "About to insert " + childStateID)
                        dao.insert(rNode)
                    } else {
                        // Log.i(TAG, "About to update " + childStateID)
                        dao.update(rNode)
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
            Log.e(TAG, "could not perform ls for " + stateID.id)
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

        fun toRTreeNode(stateID: StateID, fileNode: FileNode): RTreeNode {
            return RTreeNode(
                encodedState = stateID.id,
                workspace = stateID.workspace,
                parentPath = stateID.parentFile,
                name = stateID.fileName,
                type = "todo",
                localModificationTS = 0,
                remoteModificationTS = 0,
                lastCheckTS = 0,
                meta = fileNode.properties,
            )
        }
    }
}
