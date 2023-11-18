package org.sinou.pydia.client.ui.browse

import androidx.navigation.NavHostController
import org.sinou.pydia.sdk.transport.StateID

/** Simply expose navigation actions for the Browse subGraph */
class BrowseNavigationActions(private val navController: NavHostController) {

    // private val logTag = "BrowseNavigationActions"

    fun toBrowse(stateID: StateID) {
        val route = BrowseDestinations.Open.createRoute(stateID)
        // We don't want the single top flag when browsing otherwise the native back button does not work
        navController.navigate(route)
    }

    fun toOfflineRoots(stateID: StateID) {
        val route = BrowseDestinations.OfflineRoots.createRoute(stateID)
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    fun toBookmarks(stateID: StateID) {
        val route = BrowseDestinations.Bookmarks.createRoute(stateID)
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    fun toTransfers(stateID: StateID) {
        val route = BrowseDestinations.Transfers.createRoute(stateID)
        navController.navigate(route) {
            launchSingleTop = true
        }
    }
}
