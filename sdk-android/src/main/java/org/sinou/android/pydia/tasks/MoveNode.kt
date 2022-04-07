package org.sinou.android.pydia.tasks

import android.content.Context
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.services.NodeService
import org.sinou.android.pydia.utils.showMessage

fun moveNode(
    context: Context,
    source: StateID,
    target: StateID,
    nodeService: NodeService,
): Boolean {
    doMoveNodes(context, listOf(source), target, nodeService)
    return true
}

fun moveNodes(
    context: Context,
    sources: List<StateID>,
    target: StateID,
    nodeService: NodeService,
): Boolean {
    if (sources.isEmpty()) {
        return false
    }
    doMoveNodes(context, sources, target, nodeService)
    return true
}

private fun doMoveNodes(
    context: Context,
    sources: List<StateID>,
    targetParent: StateID,
    nodeService: NodeService,
) {
    CellsApp.instance.appScope.launch {

        // TODO what do we store/show?
        //   - source files
        //   - target files
        //   - processing
        nodeService.move(sources, targetParent)
            ?.let {
                withContext(Dispatchers.Main) {
                    showMessage(context, it)
                }
            }
            ?: run {
                withContext(Dispatchers.Main) {
                    showMessage(
                        context,
                        "Launched move to $targetParent"
                    )
                }
            }
    }
}
