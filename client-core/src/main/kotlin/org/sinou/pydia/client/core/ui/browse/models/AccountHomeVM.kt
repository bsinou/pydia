package org.sinou.pydia.client.core.ui.browse.models

import android.util.Log
import kotlinx.coroutines.flow.Flow
import org.sinou.pydia.client.core.db.accounts.RSessionView
import org.sinou.pydia.client.core.db.accounts.RWorkspace
import org.sinou.pydia.client.core.services.AccountService
import org.sinou.pydia.client.core.ui.core.AbstractCellsVM
import org.sinou.pydia.sdk.api.SdkNames
import org.sinou.pydia.sdk.transport.StateID

/**
 * Exposes the repository while showing an account Home.
 */
class AccountHomeVM(
    val accountID: StateID,
    accountService: AccountService,
) : AbstractCellsVM() {

    private val logTag = "AccountHomeVM"

    val currSession: Flow<RSessionView?> = accountService.getLiveSession(accountID)

    val wss: Flow<List<RWorkspace>> =
        accountService.getWsByTypeFlow(SdkNames.WS_TYPE_DEFAULT, accountID.id)

    val cells: Flow<List<RWorkspace>> =
        accountService.getWsByTypeFlow(SdkNames.WS_TYPE_CELL, accountID.id)

    init {
        Log.i(logTag, "Created")
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(logTag, "Cleared")
    }
}
