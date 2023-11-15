package org.sinou.pydia.client.core.ui.login

import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.sinou.pydia.client.core.ui.core.lazyLoginContext
import org.sinou.pydia.client.core.ui.core.lazySkipVerify
import org.sinou.pydia.client.core.ui.core.lazyStateID
import org.sinou.pydia.client.core.ui.login.models.LoginVM
import org.sinou.pydia.client.core.ui.login.screens.AskServerUrl
import org.sinou.pydia.client.core.ui.login.screens.LaunchAuthProcessing
import org.sinou.pydia.client.core.ui.login.screens.ProcessAuth
import org.sinou.pydia.client.core.ui.login.screens.SkipVerify
import org.sinou.pydia.client.core.ui.login.screens.StartingLoginProcess

fun NavGraphBuilder.loginNavGraph(
    helper: LoginHelper,
    loginVM: LoginVM,
) {
    val logTag = "loginNavGraph"

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
        val lContext = lazyLoginContext(nbsEntry)
        LaunchedEffect(key1 = stateID, key2 = skipVerify) {
            Log.i(logTag, "## 1st compo login/launch-auth/$stateID/$skipVerify/$lContext")
        }
        LaunchAuthProcessing(
            stateID = stateID,
            skipVerify = skipVerify,
            loginContext = lContext,
            loginVM = loginVM,
            helper = helper,
        )
    }

    composable(LoginDestinations.ProcessAuthCallback.route) { nbsEntry ->
        val stateID = lazyStateID(nbsEntry)
        LaunchedEffect(key1 = stateID) {
            Log.i(logTag, "## 1st compo login/process-auth/$stateID")
        }
        ProcessAuth(
            stateID = stateID,
            loginVM = loginVM,
            helper = helper,
        )
    }
}
