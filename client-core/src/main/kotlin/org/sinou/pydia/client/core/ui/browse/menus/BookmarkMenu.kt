package org.sinou.pydia.client.core.ui.browse.menus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import org.sinou.pydia.client.R
import org.sinou.pydia.client.core.services.ConnectionState
import org.sinou.pydia.client.core.ui.browse.composables.NodeAction
import org.sinou.pydia.client.core.ui.browse.models.TreeNodeVM
import org.sinou.pydia.client.core.ui.core.composables.DefaultTitleText
import org.sinou.pydia.client.core.ui.core.composables.M3IconThumb
import org.sinou.pydia.client.core.ui.core.composables.Thumbnail
import org.sinou.pydia.client.core.ui.core.composables.menus.BottomSheetHeader
import org.sinou.pydia.client.core.ui.core.composables.menus.BottomSheetListItem
import org.sinou.pydia.client.core.ui.core.composables.menus.BottomSheetNoAction
import org.sinou.pydia.client.core.ui.models.MultipleItem
import org.sinou.pydia.client.core.ui.models.TreeNodeItem
import org.sinou.pydia.client.core.ui.theme.CellsIcons
import org.sinou.pydia.sdk.transport.StateID

// private const val LOG_TAG = "BookmarkMenu.kt"

@Composable
fun BookmarkMenu(
    connectionState: ConnectionState,
    treeNodeVM: TreeNodeVM,
    stateID: StateID,
    rTreeNode: TreeNodeItem,
    launch: (NodeAction, StateID) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.bottom_sheet_v_spacing))
            .verticalScroll(scrollState)

    ) {
        BottomSheetHeader(
            thumb = { Thumbnail(rTreeNode) },
            title = stateID.fileName ?: "",
            desc = stateID.getParentPath(),
        )
        if (connectionState.serverConnection.isConnected()) {
            BottomSheetListItem(
                icon = CellsIcons.Bookmark,
                title = stringResource(R.string.remove_bookmark),
                onItemClick = { launch(NodeAction.ToggleBookmark(false), stateID) }
            )
        }
        if (!rTreeNode.isFolder) {
            BottomSheetListItem(
                icon = CellsIcons.DownloadToDevice,
                title = stringResource(R.string.download_to_device),
                onItemClick = { launch(NodeAction.DownloadToDevice, stateID) },
            )
        }

        val appearsIn = remember { mutableStateOf<MultipleItem?>(null) }

        appearsIn.value?.let {
            DefaultTitleText(
                text = if (rTreeNode.isFolder) {
                    stringResource(R.string.open_in_workspaces)
                } else {
                    stringResource(R.string.open_parent_in_workspaces)
                },
                modifier = Modifier.padding(
                    start = dimensionResource(R.dimen.bottom_sheet_start_padding),
                    end = dimensionResource(R.dimen.bottom_sheet_start_padding),
                    top = dimensionResource(R.dimen.bottom_sheet_v_spacing),
                    bottom = dimensionResource(R.dimen.bottom_sheet_v_padding),
                ),
            )
            // Handle multiple path
            for (peerID in it.appearsIn) {
                BottomSheetListItem(
                    icon = CellsIcons.OpenLocation,
                    title = peerID.getParentPath() ?: "",
                    onItemClick = { launch(NodeAction.OpenInApp, peerID) },
                )
            }
        }
        LaunchedEffect(key1 = stateID) {
            appearsIn.value = treeNodeVM.appearsIn(stateID)
        }
    }
}

@Composable
fun BookmarksMenu(
    connectionState: ConnectionState,
    containsFolders: Boolean,
    launch: (NodeAction) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.bottom_sheet_v_spacing))
            .verticalScroll(scrollState)

    ) {
        BottomSheetHeader(
            thumb = {
                M3IconThumb(
                    R.drawable.multiple_action,
                    MaterialTheme.colorScheme.onSurface
                )
            },
            title = "Choose an action",
        )
// TODO This is still broken
//        if (!containsFolders) {
//            BottomSheetListItem(
//                icon = CellsIcons.DownloadToDevice,
//                title = stringResource(R.string.download_to_device),
//                onItemClick = { launch(NodeAction.DownloadMultipleToDevice) },
//            )
//        }
        if (connectionState.serverConnection.isConnected()) {
            BottomSheetListItem(
                icon = CellsIcons.Bookmark,
                title = stringResource(R.string.remove_bookmarks),
                onItemClick = { launch(NodeAction.ToggleBookmark(false)) }
            )
        }
        BottomSheetListItem(
            icon = CellsIcons.Deselect,
            title = stringResource(R.string.deselect_all),
            onItemClick = { launch(NodeAction.UnSelectAll) }
        )
        BottomSheetNoAction()
    }
}
