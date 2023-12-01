package org.sinou.pydia.client.ui.browse.composables

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sinou.pydia.client.core.ListType
import org.sinou.pydia.client.core.db.accounts.RWorkspace
import org.sinou.pydia.client.core.services.models.ConnectionState
import org.sinou.pydia.client.ui.browse.menus.BookmarkMenu
import org.sinou.pydia.client.ui.browse.menus.CreateOrImportMenu
import org.sinou.pydia.client.ui.browse.menus.OfflineMenu
import org.sinou.pydia.client.ui.browse.menus.RecycleMenu
import org.sinou.pydia.client.ui.browse.menus.RecycleParentMenu
import org.sinou.pydia.client.ui.browse.menus.SearchMenu
import org.sinou.pydia.client.ui.browse.menus.SingleNodeMenu
import org.sinou.pydia.client.ui.browse.menus.SortByMenu
import org.sinou.pydia.client.ui.browse.models.TreeNodeVM
import org.sinou.pydia.client.ui.models.TreeNodeItem
import org.sinou.pydia.sdk.transport.StateID
import org.koin.androidx.compose.koinViewModel

enum class NodeMoreMenuType {
    NONE, MORE, SEARCH, OFFLINE, BOOKMARK, CREATE, SORT_BY,
}

@Composable
fun NodeMoreMenuData(
    connectionState: ConnectionState,
    type: NodeMoreMenuType,
    subjectID: StateID,
    launch: (NodeAction, StateID) -> Unit,
    treeNodeVM: TreeNodeVM = koinViewModel(),
) {
    val logTag = "NodeMoreMenuData"

    val item: MutableState<TreeNodeItem?> = remember { mutableStateOf(null) }
    val workspace: MutableState<RWorkspace?> = remember { mutableStateOf(null) }

    LaunchedEffect(key1 = subjectID) {
        if (subjectID != StateID.NONE) {
            treeNodeVM.getTreeNodeItem(subjectID)?.let { currNode ->
                item.value = currNode
            } ?: { Log.e(logTag, "No node found for $subjectID, aborting") }

            if (subjectID.isWorkspaceRoot) {
                treeNodeVM.getWS(subjectID)?.let { currNode ->
                    workspace.value = currNode
                }
            }
        }
    }

    if (type == NodeMoreMenuType.SORT_BY) {
        SortByMenu(
            type = ListType.DEFAULT,
            done = { launch(NodeAction.SortBy, subjectID) },
        )
    } else if (subjectID.slug != null) {
        item.value?.let { myItem ->
            when {
                myItem.isRecycle -> RecycleParentMenu(
                    connectionState = connectionState,
                    stateID = subjectID,
                    rTreeNode = myItem,
                    launch = { launch(it, subjectID) },
                )

                myItem.isInRecycle -> RecycleMenu(
                    connectionState = connectionState,
                    stateID = subjectID,
                    rTreeNode = myItem,
                    launch = { launch(it, subjectID) },
                )

                type == NodeMoreMenuType.CREATE -> CreateOrImportMenu(
                    connectionState = connectionState,
                    stateID = subjectID,
                    rTreeNode = myItem,
                    rWorkspace = workspace.value,
                    launch = { launch(it, subjectID) },
                )

                type == NodeMoreMenuType.OFFLINE -> OfflineMenu(
                    connectionState = connectionState,
                    stateID = subjectID,
                    nodeItem = myItem,
                    launch = { launch(it, subjectID) },
                )

                type == NodeMoreMenuType.BOOKMARK -> BookmarkMenu(
                    connectionState = connectionState,
                    treeNodeVM = treeNodeVM,
                    stateID = subjectID,
                    rTreeNode = myItem,
                    launch = launch,
                )

                type == NodeMoreMenuType.SEARCH -> SearchMenu(
                    connectionState = connectionState,
                    stateID = subjectID,
                    nodeItem = myItem,
                    launch = { launch(it, subjectID) },
                )

                type == NodeMoreMenuType.MORE ->
                    SingleNodeMenu(
                        connectionState = connectionState,
                        stateID = subjectID,
                        nodeItem = myItem,
                        rWorkspace = workspace.value,
                        launch = { launch(it, subjectID) },
                    )

                else -> Spacer(modifier = Modifier.height(1.dp))
            }
        }
    }
    // Prevent this error: java.lang.IllegalArgumentException: The initial value must have an associated anchor.
    // when no item is defined (This is the case at the beginning when we launch the Side Effect)
    // Log.d(logTag, "## No more menu for $toOpenStateID")
    Spacer(modifier = Modifier.height(1.dp))
}
