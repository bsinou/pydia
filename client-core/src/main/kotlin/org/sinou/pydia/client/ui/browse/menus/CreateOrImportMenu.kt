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
import org.sinou.pydia.client.core.db.accounts.RWorkspace
import org.sinou.pydia.client.core.services.ConnectionState
import org.sinou.pydia.client.ui.browse.composables.NodeAction
import org.sinou.pydia.client.ui.core.composables.Thumbnail
import org.sinou.pydia.client.ui.core.composables.menus.BottomSheetHeader
import org.sinou.pydia.client.ui.core.composables.menus.BottomSheetListItem
import org.sinou.pydia.client.ui.core.composables.menus.BottomSheetNoAction
import org.sinou.pydia.client.ui.models.TreeNodeItem
import org.sinou.pydia.client.ui.theme.CellsIcons
import org.sinou.pydia.sdk.transport.StateID

// private const val logTag = "CreateOrImportMenu"

@Composable
fun CreateOrImportMenu(
    connectionState: ConnectionState,
    stateID: StateID,
    rTreeNode: TreeNodeItem,
    rWorkspace: RWorkspace?,
    launch: (NodeAction) -> Unit,
) {

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.bottom_sheet_v_spacing))
            .verticalScroll(scrollState)

    ) {
        val title = stateID.fileName ?: run {
            rWorkspace?.label
        }
        val desc = stateID.getParentPath() ?: run {
            "${stateID.username}@${stateID.serverUrl}"
        }
        BottomSheetHeader(
            thumb = { Thumbnail(rTreeNode) },
            title = title ?: "",
            desc = desc,
        )

        if (connectionState.serverConnection.isConnected()) {
            BottomSheetListItem(
                icon = CellsIcons.CreateFolder,
                title = stringResource(R.string.create_folder),
                onItemClick = { launch(NodeAction.CreateFolder) },
            )
            BottomSheetListItem(
                icon = CellsIcons.ImportFile,
                title = stringResource(R.string.import_files),
                onItemClick = { launch(NodeAction.ImportFile) },
            )
            BottomSheetListItem(
                icon = CellsIcons.TakePicture,
                title = stringResource(R.string.take_picture),
                onItemClick = { launch(NodeAction.TakePicture) },
            )
        } else {
            BottomSheetNoAction()
        }
    }
}
