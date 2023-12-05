package org.sinou.pydia.client.ui.browse.models

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.sinou.pydia.client.core.db.nodes.RTreeNode
import org.sinou.pydia.client.core.util.isPreViewable
import org.sinou.pydia.client.ui.core.AbstractCellsVM
import org.sinou.pydia.sdk.transport.StateID

/** Hold the state for the carousel component */
class CarouselVM(
    initialStateID: StateID,
//     private val accountService: AccountService,
) : AbstractCellsVM() {

    private val logTag = "CarouselVM"

    // Observe current folder children
    @OptIn(ExperimentalCoroutinesApi::class)
    val allOrdered: Flow<List<RTreeNode>> = defaultOrderPair.flatMapLatest { (orderBy, direction) ->
        try {
            nodeService.listLiveChildren(initialStateID.parent(), "", orderBy, direction)
        } catch (e: Exception) {
            // This should never happen but it has been seen in prod
            // Adding a failsafe to avoid crash
            Log.e(logTag, "Could not list children of $initialStateID: ${e.message}")
            flow { listOf<RTreeNode>() }
        }
    }

    val preViewableItems: Flow<List<RTreeNode>> = allOrdered.map { childList ->
        childList.filter { item ->
            val preViewable = isPreViewable(item)
            preViewable
        }
    }
}
