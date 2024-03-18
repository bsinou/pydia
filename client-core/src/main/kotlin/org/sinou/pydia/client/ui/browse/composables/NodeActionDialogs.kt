package org.sinou.pydia.client.ui.browse.composables

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.sinou.pydia.client.R
import org.sinou.pydia.client.core.JobStatus
import org.sinou.pydia.client.core.db.nodes.RTreeNode
import org.sinou.pydia.client.core.util.formatBytesToMB
import org.sinou.pydia.client.ui.browse.models.NodeActionsVM
import org.sinou.pydia.client.ui.core.composables.DialogTitle
import org.sinou.pydia.client.ui.core.composables.animations.SmoothLinearProgressIndicator
import org.sinou.pydia.client.ui.core.composables.dialogs.AskForConfirmation
import org.sinou.pydia.client.ui.core.composables.dialogs.AskForFolderName
import org.sinou.pydia.client.ui.core.composables.dialogs.AskForNewName
import org.sinou.pydia.client.ui.models.DownloadVM
import org.sinou.pydia.client.ui.theme.CellsIcons
import org.sinou.pydia.sdk.api.ErrorCodes
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.transport.StateID

private const val LOG_TAG = "NodeActionDialogs.kt"

@Composable
fun CreateFolder(
    nodeActionsVM: NodeActionsVM = koinViewModel(),
    stateID: StateID,
    dismiss: (Boolean) -> Unit,
) {

    val doCreate: (StateID, String) -> Unit = { parentID, name ->
        nodeActionsVM.createFolder(parentID, name)
        // TODO implement a user feedback via flows
//                if (Str.notEmpty(errMsg)) {
//                    showMessage(ctx, errMsg!!)
//                } else {
//                    browseRemoteVM.watch(parentID) // This force resets the backoff ticker
//                    showMessage(ctx, "Folder created at ${parentID.file}.")
//                }
    }

    AskForFolderName(
        parStateID = stateID,
        createFolderAt = { parentId, name ->
            doCreate(parentId, name)
            dismiss(true)
        },
        dismiss = { dismiss(false) },
    )
}

@Composable
fun Download(
    stateID: StateID,
    downloadVM: DownloadVM,
    dismiss: () -> Unit,
) {
    val context = LocalContext.current
    val rTransfer = downloadVM.transfer.collectAsState(null)
    val rTreeNode = downloadVM.treeNode.collectAsState()

    LaunchedEffect(key1 = rTransfer.value?.status) {
        val status = rTransfer.value?.status
        Log.d(LOG_TAG, "... New download status for $stateID: $status")

        if (JobStatus.DONE.id == status) {
            try {
                delay(1200L)
                downloadVM.viewFile(context, stateID, true)
            } catch (se: SDKException) {
                if (se.code == ErrorCodes.no_local_file) {
                    Log.w(LOG_TAG, "... DL is over, but no local file has been found")
                    // FIXME give more time to register new file
                    delay(2000L)
                    try {
                        downloadVM.viewFile(context, stateID, true)
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "File has been downloaded but is not found")
                        e.printStackTrace()
                    }
                }
            }
            dismiss()
        }
    }

    val progress = rTransfer.value?.let {
        if (it.byteSize > 0) {
            it.progress.toFloat().div(it.byteSize.toFloat())
        } else {
            0f
        }
    } ?: 0f

    AlertDialog(
        title = { DialogTitle(text = stringResource(R.string.running_download_title)) },
        text = {
            Column {
                Text(text = rTreeNode.value?.name ?: "")
                SmoothLinearProgressIndicator(
                    indicatorProgress = progress,
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.margin))
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = dismiss) { Text(stringResource(R.string.button_run_in_background)) }
        },
        confirmButton = {
            Button(
                onClick = {
                    downloadVM.cancelDownload()
                    dismiss()
                }
            ) { Text(stringResource(R.string.button_cancel)) }
        },
        onDismissRequest = dismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            securePolicy = SecureFlagPolicy.Inherit
        )
    )
    LaunchedEffect(key1 = stateID) {
        Log.i(LOG_TAG, "... and also launching download")
        downloadVM.launchDownload()
    }
}

@Composable
fun ChooseDestination(
    nodeActionsVM: NodeActionsVM,
    stateID: StateID,
    dismiss: (Boolean) -> Unit,
) {
    Log.d(LOG_TAG, "Composing ChooseDestination for $stateID")
    val alreadyLaunched = rememberSaveable { mutableStateOf(false) }
    val destinationPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*"),
        onResult = { uri ->
            Log.e(LOG_TAG, "Got a destination for $stateID")
            if (stateID != StateID.NONE) {
                uri?.let {
                    nodeActionsVM.download(stateID, uri)
                }
            }
            dismiss(true)
        }
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.02f),
        content = {}
    )
    if (!alreadyLaunched.value) {
        LaunchedEffect(key1 = stateID) {
            Log.e(LOG_TAG, "Launching 'pick destination' for $stateID")
            delay(100)
            destinationPicker.launch(stateID.fileName)
            alreadyLaunched.value = true
        }
    }
}

@Composable
fun ChooseFolderDestination(
    nodeActionsVM: NodeActionsVM,
    stateIDs: Set<StateID>,
    dismiss: (Boolean) -> Unit,
) {
    Log.d(LOG_TAG, "Composing ChooseFolderDestination for $stateIDs")
    val alreadyLaunched = rememberSaveable { mutableStateOf(false) }
    val destinationPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            Log.e(LOG_TAG, "Got a folder: ${uri.toString()}")
            uri?.let {
                nodeActionsVM.downloadMultiple(stateIDs, uri)
            }
            dismiss(true)
        }
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.02f),
        content = {}
    )
    if (!alreadyLaunched.value) {
        LaunchedEffect(key1 = stateIDs.toString()) {
            Log.e(LOG_TAG, "Launching 'pick folder destination' for $stateIDs")
            delay(100)
            destinationPicker.launch(null)
            alreadyLaunched.value = true
        }
    }
}

@Composable
fun ImportFile(
    nodeActionsVM: NodeActionsVM,
    targetParentID: StateID,
    dismiss: (Boolean) -> Unit,
) {
    val alreadyLaunched = rememberSaveable { mutableStateOf(false) }
    val fileImporter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            nodeActionsVM.importFiles(targetParentID, uris)
            dismiss(true)
        }
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.02f),
        content = {}
    )
    if (!alreadyLaunched.value) {
        LaunchedEffect(key1 = targetParentID) {
            Log.e(LOG_TAG, "Launching 'import file' to $targetParentID")
            delay(100)
            fileImporter.launch("*/*")
            alreadyLaunched.value = true
        }
    }
}

@Composable
fun TakePicture(
    nodeActionsVM: NodeActionsVM,
    targetParentID: StateID,
    dismiss: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val alreadyLaunched = rememberSaveable { mutableStateOf(false) }
    val photoTaker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { taken ->
            if (taken) {
                nodeActionsVM.uploadPhoto()
            } else {
                nodeActionsVM.cancelPhoto()
            }
            dismiss(taken)
        }
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.02f),
        content = {}
    )
    if (!alreadyLaunched.value) {
        LaunchedEffect(key1 = targetParentID) {
            Log.d(LOG_TAG, "Launching 'TakePicture' with parent $targetParentID")
            delay(100)
            nodeActionsVM.preparePhoto(context, targetParentID)?.also {
                photoTaker.launch(it)
            }
            alreadyLaunched.value = true
        }
    }
}

@Composable
fun TreeNodeRename(
    nodeActionsVM: NodeActionsVM,
    stateID: StateID,
    dismiss: (Boolean) -> Unit,
) {
    val doRename: (StateID, String) -> Unit = { srcID, name ->
        nodeActionsVM.rename(srcID, name)
    }

    AskForNewName(
        srcID = stateID,
        rename = { srcID, name ->
            doRename(srcID, name)
            dismiss(true)
        },
        dismiss = { dismiss(false) },
    )
}

@Composable
fun ConfirmDownloadOnLimitedConnection(
    nodeActionsVM: NodeActionsVM = koinViewModel(),
    stateID: StateID,
    dismiss: (Boolean) -> Unit,
) {
    var ready by remember { mutableStateOf(false) }
    var node: RTreeNode? by remember { mutableStateOf(null) }

    LaunchedEffect(key1 = stateID) {
        node = nodeActionsVM.getNode(stateID)
        ready = true
    }

    if (ready) {
        node?.let {
            AskForConfirmation(
                // icon = CellsIcons.Delete,
                title = stringResource(R.string.confirm_dl_on_metered_title),
                desc = stringResource(
                    R.string.confirm_dl_on_metered_desc,
                    stateID.fileName ?: "NaN",
                    formatBytesToMB(it.size)
                ),
                confirm = { dismiss(true) },
                dismiss = { dismiss(false) },
            )
        } ?: run {
            Log.w(LOG_TAG, "Cannot get node for $stateID, aborting download")
            dismiss(false)
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.02f)
        )
    }
}

@Composable
fun ConfirmDeletion(
    nodeActionsVM: NodeActionsVM,
    stateID: StateID,
    dismiss: (Boolean) -> Unit,
) {
    AskForConfirmation(
        // icon = CellsIcons.Delete,
        title = stringResource(id = R.string.confirm_move_to_recycle_title),
        desc = stringResource(id = R.string.confirm_move_to_recycle_desc, stateID.fileName ?: ""),
        confirm = {
            nodeActionsVM.delete(stateID)
            dismiss(true)
        },
        dismiss = { dismiss(false) },
    )
}

@Composable
fun ConfirmPermanentDeletion(
    nodeActionsVM: NodeActionsVM,
    stateID: StateID,
    dismiss: (Boolean) -> Unit,
) {
    AskForConfirmation(
        icon = CellsIcons.Delete,
        title = stringResource(id = R.string.confirm_permanent_deletion_title),
        desc = stringResource(
            id = R.string.confirm_permanent_deletion_desc,
            stateID.fileName ?: ""
        ),
        confirm = {
            nodeActionsVM.delete(stateID) // TODO this should be enough
            dismiss(true)
        },
        dismiss = { dismiss(false) },
    )
}

@Composable
fun ConfirmEmptyRecycle(
    nodeActionsVM: NodeActionsVM,
    stateID: StateID,
    dismiss: (Boolean) -> Unit,
) {
    AskForConfirmation(
        icon = CellsIcons.EmptyRecycle,
        title = stringResource(id = R.string.confirm_permanent_deletion_title),
        desc = stringResource(id = R.string.confirm_empty_recycle_message, stateID.fileName ?: ""),
        confirm = {
            nodeActionsVM.emptyRecycle(stateID)
            dismiss(true)
        },
        dismiss = { dismiss(false) },
    )
}

@Composable
fun ShowQRCode(
    nodeActionsVM: NodeActionsVM,
    stateID: StateID,
    dismiss: () -> Unit,
) {
    val context = LocalContext.current

    val linkUrl = remember {
        mutableStateOf<String?>(null)
    }
    val bitmap = remember {
        mutableStateOf<ImageBitmap?>(null)
    }

    val writer = QRCodeWriter()
    LaunchedEffect(stateID) {
        nodeActionsVM.getShareLink(stateID)?.let { linkStr ->

            linkUrl.value = linkStr
            val bitMatrix = writer.encode(
                linkStr,
                BarcodeFormat.QR_CODE,
                context.resources.getInteger(R.integer.qrcode_width),
                context.resources.getInteger(R.integer.qrcode_width)
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val tmpBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    tmpBitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap.value = tmpBitmap.asImageBitmap()
        }
    }

    bitmap.value?.let {
        AlertDialog(
            title = { DialogTitle(text = stringResource(R.string.display_as_qrcode_dialog_title)) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        bitmap = it,
                        contentDescription = "A QRCode representation of the link ${linkUrl.value}",
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(
                        modifier = Modifier.size(dimensionResource(id = R.dimen.margin_medium))
                    )
                    Text(stringResource(R.string.display_as_qrcode_dialog_desc, stateID.fileName ?: "(no name)", linkUrl.value?: "NaN"))
                }
            },
            confirmButton = {
                TextButton(onClick = dismiss) { Text(stringResource(R.string.button_ok)) }
            },
            onDismissRequest = dismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                securePolicy = SecureFlagPolicy.Inherit
            )
        )
    }
}
