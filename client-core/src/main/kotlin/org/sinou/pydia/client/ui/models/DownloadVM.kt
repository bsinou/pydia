package org.sinou.pydia.client.ui.models

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.sinou.pydia.client.core.AppNames
import org.sinou.pydia.client.core.JobStatus
import org.sinou.pydia.client.core.db.nodes.RTransfer
import org.sinou.pydia.client.core.db.nodes.RTreeNode
import org.sinou.pydia.client.core.services.TransferService
import org.sinou.pydia.client.ui.core.AbstractCellsVM
import org.sinou.pydia.client.core.util.getTsAsString
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.transport.StateID

class DownloadVM(
    val isRemoteLegacy: Boolean,
    private val stateID: StateID,
    private val transferService: TransferService
) : AbstractCellsVM() {

    private val logTag = "DownloadVM"
    private val _rTreeNode = MutableStateFlow<RTreeNode?>(null)
    val treeNode: StateFlow<RTreeNode?> = _rTreeNode.asStateFlow()

    private val _transferID = MutableStateFlow(-1L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val transfer: Flow<RTransfer?> = _transferID.flatMapLatest { currID ->
        try {
            transferService.liveTransfer(stateID.account(), currID)
        } catch (ie: IllegalArgumentException) {
            Log.e(logTag, "Cannot get live transfer with TID  $currID: ${ie.message}")
            flow { }
        } catch (e: Exception) {
            Log.e(logTag, "Unexpected error for TID: $currID: ${e.message}")
            flow { }
        }
    }

    suspend fun launchDownload() {
        try {
            transferService.currentDownload(stateID)?.let {
                Log.w(logTag, "... About to launch DL, already have a transfer:")
                Log.d(logTag, "    - ${it.status}")
                Log.d(logTag, "    - ${it.creationTimestamp}")
                Log.d(logTag, "    - ${it.doneTimestamp}")
                if (it.status == JobStatus.PROCESSING.id || it.status == JobStatus.NEW.id) {
                    val msg =
                        "Transfer for $stateID exists since ${getTsAsString(it.creationTimestamp)}"
                    Log.e(logTag, "... $msg")
                    showError(ErrorMessage("No need to relaunch: $msg", -1, listOf()))
                    return
                }
            }
            val transferID = transferService.prepareDownload(stateID, AppNames.LOCAL_FILE_TYPE_FILE)
            _transferID.value = transferID
            transferService.runDownloadTransfer(stateID.account(), transferID, null)
        } catch (se: SDKException) {
            val msg = "Cannot download file for $stateID"
            Log.e(logTag, "$msg, cause: ${se.message ?: "-"} ")
            showError(ErrorMessage(msg, -1, listOf()))
        }
    }

    fun cancelDownload() {
        viewModelScope.launch {
            if (_transferID.value >= 0) {
                try {
                    transferService.cancelTransfer(
                        stateID,
                        _transferID.value,
                        AppNames.JOB_OWNER_USER,
                        isRemoteLegacy
                    )
                } catch (e: Exception) {
                    done(e)
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            nodeService.getNode(stateID)?.let {
                _rTreeNode.value = it
            }
        }
    }
}
