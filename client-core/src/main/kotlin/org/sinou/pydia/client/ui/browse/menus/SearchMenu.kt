package org.sinou.pydia.client.ui.browse.menus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import org.sinou.pydia.client.R
import org.sinou.pydia.client.core.services.models.ConnectionState
import org.sinou.pydia.client.ui.browse.composables.NodeAction
import org.sinou.pydia.client.ui.core.composables.Thumbnail
import org.sinou.pydia.client.ui.core.composables.menus.BottomSheetHeader
import org.sinou.pydia.client.ui.core.composables.menus.BottomSheetListItem
import org.sinou.pydia.client.ui.models.TreeNodeItem
import org.sinou.pydia.client.ui.theme.CellsIcons
import org.sinou.pydia.sdk.transport.StateID

// private const val logTag = "SearchMenu"

@Composable
fun SearchMenu(
    connectionState: ConnectionState,
    stateID: StateID,
    nodeItem: TreeNodeItem,
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
            thumb = { Thumbnail(nodeItem) },
            title = stateID.fileName ?: "",
            desc = stateID.parentFolder().path,
        )

        if (nodeItem.isFolder) {
            BottomSheetListItem(
                icon = CellsIcons.OpenLocation,
                title = stringResource(R.string.open_in_workspaces),
                onItemClick = { launch(NodeAction.OpenInApp) })

        } else {
            BottomSheetListItem(
                icon = CellsIcons.OpenLocation,
                title = stringResource(R.string.open_parent_in_workspaces),
                onItemClick = { launch(NodeAction.OpenParentLocation) }
            )
            if (connectionState.serverConnection.isConnected() || nodeItem.isCached) {
                BottomSheetListItem(
                    icon = CellsIcons.DownloadToDevice,
                    title = stringResource(R.string.download_to_device),
                    onItemClick = { launch(NodeAction.DownloadToDevice) },
                )
            }
        }
    }
}
