package org.sinou.pydia.client.core.ui.core.composables.dialogs

import android.view.KeyEvent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import org.sinou.pydia.client.R
import org.sinou.pydia.client.core.ui.core.composables.DialogTitle
import org.sinou.pydia.client.core.ui.theme.CellsIcons
import org.sinou.pydia.sdk.transport.StateID

@Composable
fun AskForNewName(
    srcID: StateID,
    rename: (srcID: StateID, name: String) -> Unit,
    dismiss: () -> Unit
) {
    val newName = remember { mutableStateOf(srcID.fileName) }
    val updateValue: (String) -> Unit = { newName.value = it }
    val doRename: () -> Unit = { rename(srcID, newName.value!!) }

    AlertDialog(
        title = {
            DialogTitle(
                text = stringResource(R.string.rename_dialog_title),
                icon = CellsIcons.Edit
            )
        },
        text = { AskForNameContent(srcID.fileName!!, newName.value!!, updateValue, doRename) },
        confirmButton = { TextButton(onClick = doRename) { Text(stringResource(R.string.rename_dialog_confirm_button)) } },
        dismissButton = { TextButton(onClick = dismiss) { Text(stringResource(R.string.button_cancel)) } },
        onDismissRequest = { dismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            securePolicy = SecureFlagPolicy.Inherit
        )
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AskForNameContent(
    oldName: String,
    value: String,
    updateValue: (String) -> Unit,
    doRename: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val imeAction = ImeAction.Done

    val onDone: () -> Unit = {
        doRename()
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    val keyboardActions = KeyboardActions(
        onNext = { focusManager.moveFocus(FocusDirection.Down) },
        onDone = { onDone() }
    )

    val focusRequester = FocusRequester()

    val modifier = Modifier
        .focusRequester(focusRequester)
        .onPreviewKeyEvent {
            if (it.key == Key.Tab && it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                focusManager.moveFocus(FocusDirection.Down)
                true
            } else if (it.key == Key.Enter) {
                onDone()
                true
            } else {
                false
            }
        }
        .fillMaxWidth()

    OutlinedTextField(
        value = value,
        onValueChange = { updateValue(it) },
        modifier = modifier,
        label = { Text(stringResource(R.string.name_label)) },
        supportingText = { Text(stringResource(R.string.rename_dialog_message, oldName)) },
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = keyboardActions,
    )

    LaunchedEffect(key1 = true) {
        focusRequester.requestFocus()
    }
}
