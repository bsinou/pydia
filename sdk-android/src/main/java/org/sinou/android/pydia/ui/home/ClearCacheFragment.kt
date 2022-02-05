package org.sinou.android.pydia.ui.home

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.db.browse.RTreeNode
import org.sinou.android.pydia.utils.showLongMessage

// TODO extend to add more options
//class ClearCacheFragment: Fragment() {
//}

fun clearCache(
    context: Context,
    node: RTreeNode
): Boolean {

    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.confirm_cache_deletion_title)
        .setIcon(R.drawable.ic_baseline_delete_24)
        .setMessage(context.resources.getString(R.string.confirm_cache_deletion_message))
        .setPositiveButton(R.string.button_confirm) { _, _ ->
            doClearCache(context, node)
        }
        .setNegativeButton(R.string.button_cancel, null)
        .show()
    return true
}

private fun doClearCache(context: Context, node: RTreeNode) {
    CellsApp.instance.appScope.launch {
        withContext(Dispatchers.IO) {
            CellsApp.instance.nodeService.clearAccountCache(node.encodedState)
                ?.let {
                    withContext(Dispatchers.Main) {
                        showLongMessage(context, it)
                    }
                }
        }
    }
}