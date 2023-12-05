package org.sinou.pydia.client.ui.browse.models

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.pydia.client.core.services.CoroutineService
import org.sinou.pydia.client.core.services.FileService
import org.sinou.pydia.client.core.services.OfflineService
import org.sinou.pydia.client.core.services.TransferService
import org.sinou.pydia.client.ui.core.AbstractCellsVM
import org.sinou.pydia.client.ui.models.fromMessage
import org.sinou.pydia.client.core.util.DEFAULT_FILE_PROVIDER_ID
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.transport.StateID
import java.io.File
import java.io.IOException

/**  Centralize methods to manage a TreeNode */
class NodeActionsVM(
    private val coroutineService: CoroutineService,
    private val fileService: FileService,
    private val transferService: TransferService,
    private val offlineService: OfflineService,
) : AbstractCellsVM() {

    private val logTag = "NodeActionsVM"

    private fun localDone(err: String? = null, userMsg: String? = null) {
        if (!err.isNullOrEmpty()) {
            Log.e(logTag, "$err")
            done(fromMessage(userMsg!!))
        } else {
            done()
        }
    }

    // Fire and forget in viewModelScope
    fun createFolder(parentID: StateID, name: String) {
        viewModelScope.launch {
            try {
                nodeService.createFolder(parentID, name)
            } catch (e: SDKException) {
                localDone("#${e.code} - ${e.message}", "Could not create folder $name node at $parentID")
                return@launch
            }
            done()
        }
    }

    fun rename(srcID: StateID, name: String) {
        viewModelScope.launch {
            try {
                nodeService.rename(srcID, name)
            } catch (e: SDKException) {
                localDone("#${e.code} - ${e.message}", "Could not rename $srcID to $name")
                return@launch
            }
            done()
        }
    }

    fun copyTo(stateID: StateID, targetParentID: StateID) {
        viewModelScope.launch {
            try {
                nodeService.copy(listOf(stateID), targetParentID)
            } catch (e: SDKException) {
                localDone("#${e.code} - ${e.message}", "Could not copy node $stateID to $targetParentID")
                return@launch
            }
            done()
        }
    }

    fun moveTo(stateID: StateID, targetParentID: StateID) {
        viewModelScope.launch {
            try {
                nodeService.move(listOf(stateID), targetParentID)
            } catch (e: SDKException) {
                localDone("#${e.code} - ${e.message}", "Could not move node [$stateID] to [$targetParentID]")
                return@launch
            }
            done()
        }
    }

    fun delete(stateIDs: Set<StateID>) {
        viewModelScope.launch {
            for (stateID in stateIDs) {
                try {
                    nodeService.delete(stateID)
                } catch (e: SDKException) {
                    localDone("#${e.code} - ${e.message}", "Could not delete node at $stateID")
                    return@launch
                }
            }
            done()
        }
    }

    fun delete(stateID: StateID) {
        viewModelScope.launch {
            try {
                nodeService.delete(stateID)
            } catch (e: SDKException) {
                localDone("#${e.code} - ${e.message}", "Could not delete node at $stateID")
                return@launch
            }
            done()
        }
    }

    fun emptyRecycle(stateID: StateID) {
        viewModelScope.launch {
            try {
                nodeService.delete(stateID)
            } catch (e: SDKException) {
                localDone("#${e.code} - ${e.message}", "Could not empty recycle at $stateID")
                return@launch
            }
            done()
        }
    }

    fun restoreFromTrash(stateID: StateID) {
        viewModelScope.launch {
            try {
                nodeService.restoreNode(stateID)
            } catch (e: SDKException) {
                localDone("#${e.code} - ${e.message}", "Could not restore node at $stateID")
                return@launch
            }
            done()
        }
    }

    fun restoreFromTrash(stateIDs: Set<StateID>) {
        viewModelScope.launch {
            for (stateID in stateIDs) {
                try {
                    nodeService.restoreNode(stateID)
                } catch (e: SDKException) {
                    localDone("#${e.code} - ${e.message}", "Could not restore node at $stateID")
                    return@launch
                }
            }
            done()
        }
    }

    fun download(stateID: StateID, uri: Uri) {
        viewModelScope.launch {
            try {
                transferService.saveToSharedStorage(stateID, uri)
                done()
            } catch (e: SDKException) {
                localDone("#${e.code} - ${e.message}", "Could not save $stateID to share storage")
            }
        }
    }

    fun downloadMultiple(stateIDs: Set<StateID>, uri: Uri) {
        viewModelScope.launch {
            var ok = true
            for (stateID in stateIDs) {
                try {
                    val currUri = uri.buildUpon().appendPath(stateID.fileName).build()
                    transferService.saveToSharedStorage(stateID, currUri)
                } catch (e: SDKException) {
                    localDone(
                        "#${e.code} - ${e.message}",
                        "Could not save $stateID to share storage"
                    )
                    ok = false
                    break
                }
            }
            if (ok) {
                done()
            }
        }
    }

    fun importFiles(stateID: StateID, uris: List<Uri>) {
        viewModelScope.launch {
            try {
                for (uri in uris) {
                    if (this.isActive) {
                        transferService.enqueueUpload(stateID, uri)
                    }
                }
                done()
            } catch (e: SDKException) {
                localDone("#${e.code} - ${e.message}", "Could import files at $stateID")
            }
        }
    }

    private var _targetForPhoto: Pair<StateID, Uri>? = null

    suspend fun preparePhoto(context: Context, parentID: StateID): Uri? =
        withContext(coroutineService.ioDispatcher) {
            val photoFile: File? = try {
                fileService.createImageFile(parentID)
            } catch (ex: IOException) {
                Log.e(logTag, "Cannot create picture file")
                ex.printStackTrace()
                // Error occurred while creating the File
                null
            }

            photoFile?.also { // Continue only if the File was successfully created
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    DEFAULT_FILE_PROVIDER_ID,
                    it
                )
                withContext(Dispatchers.Main) { // We keep the state in the current VM
                    _targetForPhoto = Pair(parentID, uri)
                }
                return@withContext uri
            }
            return@withContext null
        }

    fun uploadPhoto() {
        _targetForPhoto?.let {
            viewModelScope.launch {
                try {
                    transferService.enqueueUpload(it.first, it.second)
                } catch (e: SDKException) {
                    localDone("#${e.code} - ${e.message}", "Could not launch upload to ${it.first}")
                }
            }
        }
    }

    fun cancelPhoto() {
        _targetForPhoto = null
    }

    fun toggleBookmark(stateID: StateID, newState: Boolean) {
        viewModelScope.launch {
            try {
                nodeService.toggleBookmark(stateID, newState)
            } catch (e: Exception) {
                val msg = "Cannot toggle ($newState) bookmark for $stateID, cause: ${e.message}"
                val userMsg = if (newState) "Cannot add bookmark on $stateID"
                else "Cannot remove bookmark on $stateID"
                localDone(msg, userMsg)
                e.printStackTrace()
            }
        }
    }

    fun toggleOffline(stateID: StateID, newState: Boolean) {
        viewModelScope.launch {
            try {
                offlineService.toggleOffline(stateID, newState)
            } catch (e: java.lang.Exception) {
                val msg = "Cannot set offline flag to $newState for $stateID"
                localDone("$msg, cause: ${e.message}", msg)
                e.printStackTrace()
            }
        }
    }

    fun removeBookmark(stateID: StateID) {
        coroutineService.cellsIoScope.launch {
            try {
                nodeService.toggleBookmark(stateID, false)
            } catch (e: Exception) {
                Log.e(logTag, "Cannot delete bookmark for $stateID, cause:  ${e.message}")
                e.printStackTrace()
                done(e)
            }
        }
    }

    suspend fun createShare(stateID: StateID): String? {
        launchProcessing()
        return try {
            nodeService.createShare(stateID)
        } catch (e: SDKException) {
            localDone("#${e.code}: ${e.message}, cause: ${e.cause?.message}", e.message)
            null
        }
    }

    fun removeShare(stateID: StateID) {
        viewModelScope.launch {
            nodeService.removeShare(stateID)
        }
    }

    suspend fun getShareLink(stateID: StateID): String? {
        return nodeService.getNode(stateID)?.getShareAddress()
    }
}
