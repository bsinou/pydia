package org.sinou.pydia.client.ui.share

import android.app.Activity
import android.util.Log
import androidx.navigation.NavHostController
import org.sinou.pydia.client.core.services.AuthService
import org.sinou.pydia.client.ui.login.LoginDestinations
import org.sinou.pydia.client.ui.share.models.ShareVM
import org.sinou.pydia.sdk.transport.StateID
import org.sinou.pydia.sdk.utils.Str

class ShareHelper(
    private val navController: NavHostController,
    private val processSelectedTarget: (StateID?) -> Unit,
    private val emitActivityResult: (Int) -> Unit,
) {
    private val logTag = "ShareHelper"
    private val navigation = ShareNavigation(navController)

    fun login(stateID: StateID, skipVerify: Boolean) {
        val route = LoginDestinations.LaunchAuthProcessing.createRoute(
            stateID.account(),
            skipVerify,
            AuthService.LOGIN_CONTEXT_SHARE,
        )
        Log.i(logTag, "... Launching re-log for ${stateID.account()} from $stateID")
        navController.navigate(route)
    }


    /* Define callbacks */
    fun open(stateID: StateID) {
        Log.d(logTag, "... Calling open for $stateID")
        if (stateID == StateID.NONE) {
            navigation.toAccounts()
        } else {
            navigation.toFolder(stateID)
        }

//        // TODO re-enable Tweak to keep the back stack lean
//        val bq = navController.backQueue
//        var isEffectiveBack = false
//        if (bq.size > 1) {
//            val penultimateID = lazyStateID(bq[bq.size - 2])
//            isEffectiveBack = penultimateID == stateID && StateID.NONE != stateID
//        }
//        if (isEffectiveBack) {
//            Log.d(logTag, "isEffectiveBack: $stateID")
//            navigation.back()
//        } else {
//            if (stateID == StateID.NONE) {
//                navigation.toAccounts()
//            } else {
//                navigation.toFolder(stateID)
//            }
//        }
    }

    fun cancel() {
        emitActivityResult(Activity.RESULT_CANCELED)
    }

    fun done() {
        emitActivityResult(Activity.RESULT_OK)
    }

    fun runInBackground() {
        emitActivityResult(Activity.RESULT_OK)
    }

    fun openParentLocation(stateID: StateID) {
        navigation.toParentLocation(stateID)
    }

    fun startUpload(stateID: StateID) {
        processSelectedTarget(stateID)
    }

    fun canPost(stateID: StateID): Boolean {
        // TODO also check permissions on remote server
        return Str.notEmpty(stateID.slug)
        // true
//        if (action == AppNames.ACTION_UPLOAD) {
//            true
//        } else {
//            // Optimistic check to prevent trying to copy move inside itself
//            // TODO this does not work: we get the parent state as initial input
//            //   (to start from the correct location), we should rather get a list of states
//            //   that are about to copy / move to provide better behaviour in the select target activity
//            !((stateID.id.startsWith(initialStateId) && (stateID.id.length > initialStateId.length)))
//        }
    }
}
