package org.sinou.android.pydia.tasks

import android.content.Context
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.db.nodes.RTreeNode
import org.sinou.android.pydia.utils.showMessage

fun moveNode(
    context: Context,
    source: StateID,
    target: StateID,
): Boolean {
    doMoveNodes(context, listOf(source), target)
    return true
}

fun moveNodes(
    context: Context,
    sources: List<StateID>,
    target: StateID,
): Boolean {
    if (sources.isEmpty()) {
        return false
    }
    doMoveNodes(context, sources, target)
    return true
}

private fun doMoveNodes(context: Context, sources: List<StateID>, targetParent: StateID) {
    CellsApp.instance.appScope.launch {

        // TODO what do we store/show?
        //   - source files
        //   - target files
        //   - processing
        CellsApp.instance.nodeService.move(sources, targetParent)
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
