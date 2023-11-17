package org.sinou.pydia.client.core.ui.share

import android.util.Log
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.sinou.pydia.client.core.AppNames
import org.sinou.pydia.client.core.ui.core.lazyStateID
import org.sinou.pydia.client.core.ui.core.lazyUID
import org.sinou.pydia.client.core.ui.models.BrowseRemoteVM
import org.sinou.pydia.client.core.ui.share.models.MonitorUploadsVM
import org.sinou.pydia.client.core.ui.share.models.ShareVM
import org.sinou.pydia.client.core.ui.share.screens.SelectFolderScreen
import org.sinou.pydia.client.core.ui.share.screens.SelectTargetAccount
import org.sinou.pydia.client.core.ui.share.screens.UploadProgressList
import org.sinou.pydia.sdk.transport.StateID

fun NavGraphBuilder.shareNavGraph(
    isExpandedScreen: Boolean,
    browseRemoteVM: BrowseRemoteVM,
    helper: ShareHelper,
    back: () -> Unit,
) {
    val logTag = "shareNavGraph"

    composable(ShareDestination.ChooseAccount.route) {
        LaunchedEffect(key1 = Unit) {
            Log.i(logTag, "## First composition for: ${ShareDestination.ChooseAccount.route}")
        }
        SelectTargetAccount(helper)
    }

    composable(ShareDestination.OpenFolder.route) { nbsEntry ->
        val stateID = lazyStateID(nbsEntry)
        if (stateID == StateID.NONE) {
            Log.e(logTag, "Cannot open target selection folder page with no ID")
            back()
        } else {
            val shareVM: ShareVM = koinViewModel { parametersOf(stateID) }
            SelectFolderScreen(
                targetAction = AppNames.ACTION_UPLOAD,
                stateID = stateID,
                browseRemoteVM = browseRemoteVM,
                shareVM = shareVM,
                open = helper::open,
                canPost = helper::canPost,
                doAction = { action, currID ->
                    if (AppNames.ACTION_UPLOAD == action) {
                        helper.startUpload(shareVM, currID)
                    } else {
                        helper.launchTaskFor(action, currID)
                    }
                },
            )
            DisposableEffect(key1 = stateID) {
                if (stateID == StateID.NONE) {
                    browseRemoteVM.pause(StateID.NONE)
                } else {
                    browseRemoteVM.watch(stateID, false)
                }
                onDispose {
                    browseRemoteVM.pause(stateID)
                }
            }
        }
    }

    composable(ShareDestination.UploadInProgress.route) { nbsEntry ->
        val stateID = lazyStateID(nbsEntry)
        val jobID = lazyUID(nbsEntry)
        LaunchedEffect(key1 = stateID, key2 = jobID) {
            Log.i(logTag, "## First Comp for: share/in-progress/ #$jobID @ $stateID")
        }
        val monitorUploadsVM: MonitorUploadsVM =
            koinViewModel(parameters = { parametersOf(stateID, jobID) })
        UploadProgressList(
            isExpandedScreen = isExpandedScreen,
            monitorUploadsVM,
            { helper.runInBackground(stateID) },
            { helper.done(stateID) },
            { helper.cancel(stateID) },
            { helper.openParentLocation(stateID) },
        )
    }
}
