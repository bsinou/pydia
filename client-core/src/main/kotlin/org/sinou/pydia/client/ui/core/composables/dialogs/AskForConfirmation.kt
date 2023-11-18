package org.sinou.pydia.client.ui.core.composables.dialogs

import CellsAlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AskForConfirmation(
    icon: ImageVector? = null,
    title: String,
    desc: String,
    confirm: () -> Unit,
    dismiss: () -> Unit,
) {
    CellsAlertDialog(icon = icon, title = title, desc = desc, confirm = confirm, dismiss = dismiss)
}