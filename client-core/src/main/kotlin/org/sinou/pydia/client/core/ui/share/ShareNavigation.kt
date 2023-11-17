package org.sinou.pydia.client.core.ui.share

import androidx.navigation.NavHostController
import org.sinou.pydia.client.core.ui.browse.BrowseDestinations
import org.sinou.pydia.sdk.transport.StateID

/** Simply expose navigation actions for the Share subGraph */
class ShareNavigation(private val navController: NavHostController) {

    // private val logTag = "ShareNavigation"

    fun toAccounts() {
        val route = ShareDestination.ChooseAccount.route
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    fun toFolder(stateID: StateID) {
        val route = ShareDestination.OpenFolder.createRoute(stateID)
        navController.navigate(route)
    }

    fun toTransfers(stateID: StateID, jobID: Long) {
        val route = ShareDestination.UploadInProgress.createRoute(stateID, jobID)
        navController.navigate(route) {
            popUpTo(ShareDestination.ChooseAccount.route) { inclusive = true }
        }
    }

    fun toParentLocation(stateID: StateID) {
        val route = BrowseDestinations.Open.createRoute(stateID)
        navController.navigate(route) {
            popUpTo(ShareDestination.ChooseAccount.route) { inclusive = true }
        }
    }

    fun back() {
        navController.popBackStack()
    }
}
