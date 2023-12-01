package org.sinou.pydia.client.ui.share

import androidx.navigation.NavHostController
import org.sinou.pydia.client.ui.browse.BrowseDestinations
import org.sinou.pydia.sdk.transport.StateID

/** Simply expose navigation actions for the Share subGraph */
class ShareNavigation(private val navController: NavHostController) {

    // private val logTag = "ShareNavigation"

    fun toAccounts() {
        val route = ShareDestinations.ChooseAccount.route
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    fun toFolder(stateID: StateID) {
        val route = ShareDestinations.OpenFolder.createRoute(stateID)
        navController.navigate(route)
    }

    fun toTransfers(stateID: StateID, jobID: Long) {
        val route = ShareDestinations.UploadInProgress.createRoute(stateID, jobID)
        navController.navigate(route) {
            popUpTo(ShareDestinations.ChooseAccount.route) { inclusive = true }
        }
    }

    fun toParentLocation(stateID: StateID) {
        val route = BrowseDestinations.Open.createRoute(stateID)
        navController.navigate(route) {
            popUpTo(ShareDestinations.ChooseAccount.route) { inclusive = true }
        }
    }

    fun back() {
        navController.popBackStack()
    }
}
