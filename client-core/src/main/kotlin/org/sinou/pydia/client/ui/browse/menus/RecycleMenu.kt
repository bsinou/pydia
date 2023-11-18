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
import org.sinou.pydia.client.core.services.ConnectionState
import org.sinou.pydia.client.ui.browse.composables.NodeAction
import org.sinou.pydia.client.ui.core.composables.Thumbnail
import org.sinou.pydia.client.ui.core.composables.menus.BottomSheetHeader
import org.sinou.pydia.client.ui.core.composables.menus.BottomSheetListItem
import org.sinou.pydia.client.ui.core.composables.menus.BottomSheetNoAction
import org.sinou.pydia.client.ui.models.TreeNodeItem
import org.sinou.pydia.client.ui.theme.CellsIcons
import org.sinou.pydia.sdk.transport.StateID

// private const val logTag = "RecycleParentMenu"

@Composable
fun RecycleParentMenu(
    connectionState: ConnectionState,
    stateID: StateID,
    rTreeNode: TreeNodeItem,
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
            thumb = { Thumbnail(rTreeNode) },
            title = stringResource(id = R.string.recycle_bin_label),
            desc = stateID.getParentPath() ?: "",
        )

        if (connectionState.serverConnection.isConnected()) {
            BottomSheetListItem(
                icon = CellsIcons.EmptyRecycle,
                title = stringResource(R.string.empty_recycle),
                onItemClick = { launch(NodeAction.EmptyRecycle) },
            )
        } else {
            BottomSheetNoAction()
        }
    }
}

@Composable
fun RecycleMenu(
    connectionState: ConnectionState,
    stateID: StateID,
    rTreeNode: TreeNodeItem,
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
            thumb = { Thumbnail(rTreeNode) },
            title = stringResource(id = R.string.recycle_bin_label),
            desc = stateID.getParentPath(),
        )
        if (connectionState.serverConnection.isConnected()) {
            BottomSheetListItem(
                icon = CellsIcons.RestoreFromTrash,
                title = stringResource(R.string.restore_content),
                onItemClick = { launch(NodeAction.RestoreFromTrash) },
            )
            BottomSheetListItem(
                icon = CellsIcons.DeleteForever,
                title = stringResource(R.string.permanently_remove),
                onItemClick = { launch(NodeAction.PermanentlyRemove) },
            )
        } else {
            BottomSheetNoAction()
        }
    }
}
