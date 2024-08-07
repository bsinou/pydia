package org.sinou.pydia.client.ui.login

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import org.sinou.pydia.client.ui.core.lazyLoginContext
import org.sinou.pydia.client.ui.core.lazySkipVerify
import org.sinou.pydia.client.ui.core.lazyStateID
import org.sinou.pydia.client.ui.login.models.LoginVM
import org.sinou.pydia.client.ui.login.screens.AskServerUrl
import org.sinou.pydia.client.ui.login.screens.LaunchOAuthFlow
import org.sinou.pydia.client.ui.login.screens.SkipVerify
import org.sinou.pydia.client.ui.login.screens.StartingLoginProcess


private const val logTag = "LoginNavGraphNew"

@Composable
fun LoginNavGraphNew(
    navController: NavHostController,
    navigateTo: (String) -> Unit,
    loginVM: LoginVM = koinViewModel()
) {

    val helper = LoginHelper(
        navController = navController,
        loginVM = loginVM,
        navigateTo = navigateTo
    )

    NavHost(navController, LoginDestinations.AskUrl.route) {
        localLoginGraph(helper, loginVM)
    }
}

fun NavGraphBuilder.localLoginGraph(
    helper: LoginHelper,
    loginVM: LoginVM
) {

    composable(LoginDestinations.Starting.route) { nbsEntry ->
        val stateID = lazyStateID(nbsEntry)
        LaunchedEffect(key1 = stateID) {
            Log.i(logTag, "## 1st compo login/starting/$stateID")
        }
        StartingLoginProcess()
    }

    composable(LoginDestinations.Done.route) { nbsEntry ->
        val stateID = lazyStateID(nbsEntry)
        LaunchedEffect(key1 = stateID) {
            Log.i(logTag, "## 1st compo login/done/$stateID")
        }
        StartingLoginProcess()
    }

    composable(LoginDestinations.AskUrl.route) {
        LaunchedEffect(key1 = Unit) {
            Log.i(logTag, "## 1st compo login/ask-url")
        }
        AskServerUrl(helper = helper, loginVM = loginVM)
    }

    composable(LoginDestinations.SkipVerify.route) { nbsEntry ->
        val stateID = lazyStateID(nbsEntry)
        LaunchedEffect(key1 = stateID) {
            Log.i(logTag, "## 1st compo login/skip-verify/$stateID")
        }
        SkipVerify(stateID, helper = helper, loginVM = loginVM)
    }

    composable(LoginDestinations.LaunchAuthProcessing.route) { nbsEntry ->
        val stateID = lazyStateID(nbsEntry)
        val skipVerify = lazySkipVerify(nbsEntry)
        val loginContext = lazyLoginContext(nbsEntry)
        LaunchOAuthFlow(
            stateID = stateID,
            skipVerify = skipVerify,
            loginContext = loginContext,
            loginVM = loginVM,
            helper = helper,
        )
    }
}
