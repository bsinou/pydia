package org.sinou.pydia.client.ui.browse.models

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.sinou.pydia.client.core.AppNames
import org.sinou.pydia.client.core.db.nodes.RTransfer
import org.sinou.pydia.client.core.services.TransferService
import org.sinou.pydia.client.ui.core.AbstractCellsVM
import org.sinou.pydia.sdk.transport.StateID

/** Holds a list of recent file transfers for current session */
class TransfersVM(
    private val accountID: StateID,
    private val transferService: TransferService,
) : AbstractCellsVM() {

    private val logTag = "TransfersVM"

    private val transferOrder = prefs.cellsPreferencesFlow.map { cellsPreferences ->
        cellsPreferences.list.transferOrder
    }

    val liveFilter: Flow<String> = prefs.cellsPreferencesFlow.map { cellsPreferences ->
        cellsPreferences.list.transferFilter
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val transfers: Flow<List<RTransfer>> =
        liveFilter.combine(transferOrder) { filter, order -> filter to order }
            .flatMapLatest { pair ->
                transferService.queryTransfersExplicitFilter(
                    accountID,
                    pair.first,
                    pair.second
                )
            }

    suspend fun get(transferID: Long): RTransfer? = transferService.getRecord(accountID, transferID)

    fun pauseOne(transferID: Long) {
        viewModelScope.launch {
            try {
                transferService.pauseTransfer(
                    accountID,
                    transferID,
                    AppNames.JOB_OWNER_USER
                )
            } catch (e: Exception) {
                done(e)
            }
        }
    }

    fun resumeOne(transferID: Long) {
        viewModelScope.launch {
            try {
                transferService.resumeTransfer(accountID, transferID)
            } catch (e: Exception) {
                done(e)
            }
        }
    }

    fun cancelOne(transferID: Long) {
        viewModelScope.launch {
            try {
                transferService.cancelTransfer(
                    accountID,
                    transferID,
                    AppNames.JOB_OWNER_USER
                )

            } catch (e: Exception) {
                done(e)
            }
        }
    }

    fun removeOne(transferID: Long) {
        Log.i(logTag, "About to delete $transferID @ $accountID")
        viewModelScope.launch {
            try {
                transferService.forgetTransfer(accountID, transferID)
            } catch (e: Exception) {
                done(e)
            }
        }
    }

    fun clearTerminated() {
        Log.i(logTag, "About to empty transfer table for $accountID")
        viewModelScope.launch {
            try {
                // TODO better management of terminated transfers
                transferService.clearTerminated(accountID)
            } catch (e: Exception) {
                done(e)
            }
        }
    }

    fun forceRefresh() {
        // DO nothing
    }

    init {
        // We are always "idle" in this view
        done()
    }
}
