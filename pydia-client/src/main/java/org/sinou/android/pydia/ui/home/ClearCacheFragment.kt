package org.sinou.android.pydia.ui.home

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.R
import org.sinou.android.pydia.services.NodeService
import org.sinou.android.pydia.utils.showLongMessage

// TODO extend to add more options
//class ClearCacheFragment: Fragment() {
//}

fun clearCache(
    context: Context,
    encodedState: String,
    nodeService: NodeService,
): Boolean {

    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.confirm_cache_deletion_title)
        .setIcon(R.drawable.ic_baseline_delete_24)
        .setMessage(context.resources.getString(R.string.confirm_cache_deletion_message))
        .setPositiveButton(R.string.button_confirm) { _, _ ->
            doClearCache(context, encodedState, nodeService)
        }
        .setNegativeButton(R.string.button_cancel, null)
        .show()
    return true
}

private fun doClearCache(context: Context, encodedState: String, nodeService: NodeService) {
    CellsApp.instance.appScope.launch {
        withContext(Dispatchers.IO) {
            nodeService.clearAccountCache(encodedState)
                ?.let {
                    withContext(Dispatchers.Main) {
                        showLongMessage(context, it)
                    }
                }
        }
    }
}