package org.sinou.android.pydia.services

import android.util.Log
import com.pydio.cells.api.Client
import com.pydio.cells.api.CustomEncoder
import com.pydio.cells.api.ErrorCodes
import com.pydio.cells.api.SDKException
import com.pydio.cells.api.ui.Node
import com.pydio.cells.api.ui.PageOptions
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.room.browse.TreeNodeDB
import org.sinou.android.pydia.utils.AndroidCustomEncoder

class NodeService(
    private val nodeDB: TreeNodeDB,
    private val accountService: AccountService,
    private val workingDir: String?
) {

    private val encoder: CustomEncoder = AndroidCustomEncoder()

    private val TAG = "NodeService"

//    @WorkerThread
//    suspend fun ls(stateID: StateID): LiveData<List<TreeNode>> {
//        return nodeDB.treeNodeDao().ls(stateID.accountId, stateID.path)
//    }


    companion object {
        fun firstPage(): PageOptions {
            var page = PageOptions()
            page.limit = 1000
            page.offset = 0
            page.currentPage = 1
            return page
        }
    }

    fun pull(stateID: StateID) {
        try {
            val client: Client = accountService.sessionFactory.getUnlockedClient(stateID.accountId)
                ?: throw SDKException(
                    ErrorCodes.internal_error,
                    "no client found for account " + stateID.id
                )
            val page = firstPage()
            val nextPage = client.ls(
                stateID.workspace, stateID.path, page
            ) { node: Node? ->
                Log.i(TAG, "Found node " + node?.path)

                // TODO update the DB
            }

        } catch (e: SDKException) {
            Log.e(TAG, "could not perform ls for " + stateID.id)
            e.printStackTrace()
        }
    }

    fun listWorkspaces(accountID: String) {
        try {
            val client: Client = accountService.sessionFactory.getUnlockedClient(accountID)
                ?: throw SDKException(
                    ErrorCodes.internal_error,
                    "no client found for account " + accountID
                )
            val nextPage = client.workspaceList { node: Node? ->
                Log.i(TAG, "Found workspace " + node?.path)
            }

        } catch (e: SDKException) {
            Log.e(TAG, "could not perform ls for " + accountID)
            e.printStackTrace()
        }
        Log.e(TAG, "workspace listed for " + accountID)

    }
}
