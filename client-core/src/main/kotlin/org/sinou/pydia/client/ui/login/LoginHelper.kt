package org.sinou.pydia.client.ui.login

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import org.sinou.pydia.client.core.services.AuthService
import org.sinou.pydia.client.ui.login.models.LoginVM
import org.sinou.pydia.sdk.transport.ServerURLImpl
import org.sinou.pydia.sdk.transport.StateID

class LoginHelper(
    private val navController: NavHostController,
    private val loginVM: LoginVM,
    val navigateTo: (String) -> Unit,
//    val startingState: StartingState?,
//    val ackStartStateProcessed: (String?, StateID) -> Unit,
) {
    private val logTag = "LoginHelper"

    fun cancel() {
        navController.popBackStack()
        loginVM.flush()
    }

    suspend fun back() {

        // TODO double check that login navigation is still OK - See below
        loginVM.resetMessages()
        navController.popBackStack()

//        var isFirstLoginPage = true
//        if (bq.size > 1) {
//            val penEntry = bq[bq.size - 2]
//            val penRoute = penEntry.destination.route
//            if (LoginDestinations.isCurrent(penRoute)) {
//                // penultimate route is still in the login subgraph
//                isFirstLoginPage = false
//            }
//        }
//        if (isFirstLoginPage) { // back is then a cancellation of the current login process
//            cancel()
//        } else {
//            loginVM.resetMessages()
//            navController.popBackStack()
//        }
    }

    fun afterPing(res: String) {
        navigateTo(res)
    }

    suspend fun launchAuth(
        context: Context,
        stateID: StateID,
        skipVerify: Boolean = false,
        loginContext: String = AuthService.LOGIN_CONTEXT_CREATE
    ) {
        val intent = loginVM.getSessionView(stateID)?.let { sessionView ->
            // Re-authenticating an existing account
            val url = ServerURLImpl.fromAddress(sessionView.url, sessionView.skipVerify())
            loginVM.newOAuthIntent(url, loginContext)
        } ?: run {
            Log.i(logTag, "Launching OAuth Process for new account $stateID")
            val url = ServerURLImpl.fromAddress(stateID.serverUrl, skipVerify)
            loginVM.newOAuthIntent(url, loginContext)
        }
        intent?.let {
            ContextCompat.startActivity(context, intent, null)
        }
    }

//    suspend fun processAuth(stateID: StateID) {
//
//        if (startingState == null || !LoginDestinations.ProcessAuthCallback.isCurrent(startingState.route)) {
//            Log.e(logTag, "## In processAuth for state: $stateID")
//            Log.e(logTag, "##  invalid starting state or route: ${startingState?.route}")
//            Thread.dumpStack()
//            return
//        }
//
//        Log.i(logTag, "... In processAuth for: $stateID")
//        Log.d(logTag, "     route: ${startingState.route}")
//        Log.d(logTag, "     OAuth state: ${startingState.state}")
//
//        loginVM.handleOAuthResponse(
//            // We assume nullity has already been checked
//            state = startingState.state!!,
//            code = startingState.code!!,
//        )?.let {
//            Log.i(logTag, "    -> OAuth OK, login context: ${it.second}")
//            afterAuth(it.first, it.second)
//        } ?: run {
//            // TODO better error handling
//            ackStartStateProcessed(
//                null,
//                StateID.NONE
//            )
//        }
//    }


//    private fun afterAuth(stateID: StateID, loginContext: String?) {
//        ackStartStateProcessed(null, stateID)
//
//        Log.e(logTag, "... After OAuth: $stateID, context: $loginContext, unstacking destinations:")
//
//        // FIXME remove
//        val bseList = navController.currentBackStack.value
//        Log.e(logTag, "... Looping back stack")
//        var i = 1
//        for (bse in bseList) {
//            Log.e(logTag, " #$i: ${bse.destination.route}")
//            i++
//        }
//        Log.e(logTag, "... Looping done")
//
//        var stillLogin = true
//        while (stillLogin) {
//            val tmp = navController.currentBackStackEntry
//            Log.e(logTag, " - curr dest: ${tmp?.destination?.route}")
//            tmp?.let {
//                if (LoginDestinations.isCurrent(it.destination.route)) {
//                    navController.popBackStack()
//                } else {
//                    stillLogin = false
//                }
//            } ?: run { stillLogin = false }
//        }
//
//        if (loginContext == AuthService.LOGIN_CONTEXT_CREATE) {
//            // New account -> we open it
//            // TODO navigateTo(BrowseDestinations.Open.createRoute(stateID))
//        } else {
//            // We only get rid of login pages.
//        }
//        loginVM.flush()
//    }
}
