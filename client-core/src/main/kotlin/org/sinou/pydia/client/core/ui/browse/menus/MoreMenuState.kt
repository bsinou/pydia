package org.sinou.pydia.client.core.ui.browse.menus

import androidx.compose.material3.ExperimentalMaterial3Api
import org.sinou.pydia.client.core.ui.browse.composables.NodeMoreMenuType
import org.sinou.pydia.client.core.ui.core.composables.modal.ModalBottomSheetState
import org.sinou.pydia.sdk.transport.StateID

class MoreMenuState @OptIn(ExperimentalMaterial3Api::class) constructor(
    val sheetState: ModalBottomSheetState,
    val type: NodeMoreMenuType,
    val stateID: StateID,
    val openMoreMenu: (NodeMoreMenuType, StateID) -> Unit,
)

class SetMoreMenuState @OptIn(ExperimentalMaterial3Api::class) constructor(
    val sheetState: ModalBottomSheetState,
    val type: NodeMoreMenuType,
    val stateIDs: Set<StateID>,
    val openMoreMenu: (NodeMoreMenuType, Set<StateID>) -> Unit,
    val cancelSelection: () -> Unit,
)
