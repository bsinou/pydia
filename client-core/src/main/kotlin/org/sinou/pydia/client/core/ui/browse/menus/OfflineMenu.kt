package org.sinou.pydia.client.core.ui.browse.menus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import org.sinou.pydia.client.R
import org.sinou.pydia.client.core.services.ConnectionState
import org.sinou.pydia.client.core.ui.browse.composables.NodeAction
import org.sinou.pydia.client.core.ui.core.composables.Thumbnail
import org.sinou.pydia.client.core.ui.core.composables.menus.BottomSheetHeader
import org.sinou.pydia.client.core.ui.core.composables.menus.BottomSheetListItem
import org.sinou.pydia.client.core.ui.models.TreeNodeItem
import org.sinou.pydia.client.core.ui.theme.CellsIcons
import org.sinou.pydia.sdk.transport.StateID

// private const val logTag = "OfflineMoreMenuView"

@Composable
fun OfflineMenu(
    connectionState: ConnectionState,
    stateID: StateID,
    nodeItem: TreeNodeItem,
    launch: (NodeAction) -> Unit,
) {

    val scrollState = rememberScrollState()

    var dialogOpen by remember {
        mutableStateOf(false)
    }

    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        launch(NodeAction.ToggleOffline(false))
                        dialogOpen = false
                    }
                ) { Text(text = stringResource(R.string.button_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { dialogOpen = false }) {
                    Text(text = stringResource(R.string.button_cancel))
                }
            },
            title = { Text(text = stringResource(R.string.confirm_remove_offline_title)) },
            text = { Text(text = stringResource(R.string.confirm_remove_offline_desc)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.margin)),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.bottom_sheet_v_spacing))
            .verticalScroll(scrollState)

    ) {
        BottomSheetHeader(
            thumb = { Thumbnail(nodeItem) },
            title = stateID.fileName ?: "",
            desc = stateID.parentPath,
        )
        if (connectionState.serverConnection.isConnected()) {

            BottomSheetListItem(
                icon = CellsIcons.Refresh,
                title = stringResource(R.string.force_resync),
                onItemClick = { launch(NodeAction.ForceResync) },
            )
        }

        BottomSheetListItem(
            icon = CellsIcons.OpenLocation,
            title = if (nodeItem.isFolder) {
                stringResource(R.string.open_in_workspaces)
            } else {
                stringResource(R.string.open_parent_in_workspaces)
            },
            onItemClick = { launch(NodeAction.OpenInApp) },
        )

        if (!nodeItem.isFolder && (connectionState.serverConnection.isConnected() || nodeItem.isCached)) {
            BottomSheetListItem(
                icon = CellsIcons.DownloadToDevice,
                title = stringResource(R.string.download_to_device),
                onItemClick = { launch(NodeAction.DownloadToDevice) },
            )
        }

        BottomSheetListItem(
            icon = CellsIcons.KeepOffline,
            title = stringResource(R.string.remove_from_offline),
            onItemClick = {
                dialogOpen = true
            },
        )
    }
}
