package org.sinou.pydia.client.core.ui

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import org.sinou.pydia.client.core.ui.account.AccountListVM
import org.sinou.pydia.client.core.ui.account.AccountsScreen
import org.sinou.pydia.client.core.ui.core.nav.CellsDestinations
import org.sinou.pydia.client.core.ui.login.LoginHelper
import org.sinou.pydia.client.core.ui.login.LoginNavigation
import org.sinou.pydia.client.core.ui.login.loginNavGraph
import org.sinou.pydia.client.core.ui.login.models.LoginVM
import org.sinou.pydia.client.core.ui.models.BrowseRemoteVM
import org.sinou.pydia.client.core.ui.system.systemNavGraph
import org.sinou.pydia.sdk.transport.StateID

@Composable
fun CellsNavGraph(
    startingState: StartingState?,
    ackStartStateProcessing: (String?, StateID) -> Unit,
    isExpandedScreen: Boolean,
    navController: NavHostController,
    navigateTo: (String) -> Unit,
    openDrawer: () -> Unit,
    launchTaskFor: (String, StateID) -> Unit,
    launchIntent: (Intent?, Boolean, Boolean) -> Unit,
    loginVM: LoginVM = koinViewModel(),
    browseRemoteVM: BrowseRemoteVM = koinViewModel()
) {

    val logTag = "CellsNavGraph"

    val loginNavActions = remember(navController) {
        LoginNavigation(navController)
    }

    Log.i(logTag, "### Composing nav graph for ${startingState?.route}")

    startingState?.let {
        LaunchedEffect(key1 = it.route) {
            it.route?.let { dest ->
                Log.e(logTag, "      currRoute: ${navController.currentDestination?.route}")
                Log.e(logTag, "      newRoute: $dest")
                navController.navigate(dest)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = CellsDestinations.Accounts.route,
        route = CellsDestinations.Root.route
    ) {

        composable(CellsDestinations.Accounts.route) {
            val accountListVM: AccountListVM = koinViewModel()
            AccountsScreen(
                isExpandedScreen = isExpandedScreen,
                accountListVM = accountListVM,
                navigateTo = navigateTo,
                openDrawer = openDrawer,
                contentPadding = rememberContentPaddingForScreen(
                    // additionalTop = if (!isExpandedScreen) 0.dp else 8.dp,
                    excludeTop = !isExpandedScreen
                ),
            )

            DisposableEffect(key1 = true) {
                accountListVM.watch()
                onDispose { accountListVM.pause() }
            }
        }

        loginNavGraph(
            loginVM = loginVM,
            helper = LoginHelper(
                navController,
                loginVM,
                navigateTo,
                startingState,
                ackStartStateProcessing
            ),
        )

        systemNavGraph(
            isExpandedScreen = isExpandedScreen,
            navController = navController,
            openDrawer = openDrawer,
            launchIntent = launchIntent,
            back = { navController.popBackStack() },
        )

    }
}
