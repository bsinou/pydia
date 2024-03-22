package org.sinou.pydia.client.ui.login

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
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
        loginContext: String, //  = AuthService.LOGIN_CONTEXT_CREATE
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
            // navController.popBackStack()
        }
    }
}
