package org.sinou.pydia.client.core.ui.login

import androidx.navigation.NavHostController
import org.sinou.pydia.sdk.transport.StateID

/** Simply expose navigation actions for the Login subGraph */
class LoginNavigation(private val navController: NavHostController) {

    fun start(stateID: StateID?) {
        val route = LoginDestinations.Starting.createRoute(stateID ?: StateID.NONE)
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    fun done(stateID: StateID?) {
        val route = LoginDestinations.Done.createRoute(stateID ?: StateID.NONE)
        navController.navigate(route)
    }

    fun askUrl() {
        val route = LoginDestinations.AskUrl.createRoute()
        navController.navigate(route)
    }

    fun skipVerify(stateID: StateID?) {
        val route =
            LoginDestinations.SkipVerify.createRoute(stateID ?: StateID.NONE)
        navController.navigate(route)
    }

    fun back() {
        navController.popBackStack()
    }
}
