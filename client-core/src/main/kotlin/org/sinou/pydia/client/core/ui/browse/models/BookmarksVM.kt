package org.sinou.pydia.client.core.ui.browse.models

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import org.sinou.pydia.client.core.services.TransferService
import org.sinou.pydia.client.core.ui.core.AbstractCellsVM
import org.sinou.pydia.client.core.ui.models.MultipleItem
import org.sinou.pydia.client.core.ui.models.deduplicateNodes
import org.sinou.pydia.sdk.transport.StateID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Expose methods used by bookmark pages */
class BookmarksVM(
    private val accountID: StateID,
    private val transferService: TransferService,
) : AbstractCellsVM() {

    private val logTag = "BookmarksVM"

    @OptIn(ExperimentalCoroutinesApi::class)
    val bookmarks: Flow<List<MultipleItem>> = defaultOrderPair.flatMapLatest { currPair ->
        nodeService.listBookmarkFlow(accountID, currPair.first, currPair.second).map { nodes ->
            deduplicateNodes(nodeService, nodes)
        }
    }

    fun forceRefresh(stateID: StateID) {
        viewModelScope.launch {
            launchProcessing()
            try {
                nodeService.refreshBookmarks(stateID)
                done()
            } catch (e: Exception) {
                done(e)
            }
        }
    }

    fun removeBookmark(stateID: StateID) {
        viewModelScope.launch {
            try {
                nodeService.toggleBookmark(stateID, false)
            } catch (e: Exception) {
                Log.e(logTag, "Cannot delete bookmark for $stateID, cause:  ${e.message}")
                e.printStackTrace()
                done(e)
            }
        }
    }

    fun download(stateID: StateID, uri: Uri) {
        viewModelScope.launch {
            try {
                transferService.saveToSharedStorage(stateID, uri)
            } catch (e: Exception) {
                done(e)
            }
        }
    }

    /* Helpers */
    init {
        forceRefresh(accountID)
        Log.d(logTag, "... Initialised")
    }

    override fun onCleared() {
        Log.d(logTag, "... Cleared")
    }
}
