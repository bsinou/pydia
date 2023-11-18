package org.sinou.pydia.client.core.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import org.sinou.pydia.client.core.ui.account.AccountListVM
import org.sinou.pydia.client.core.ui.account.AccountsScreen
import org.sinou.pydia.client.core.ui.browse.browseNavGraph
import org.sinou.pydia.client.core.ui.core.nav.CellsDestinations
import org.sinou.pydia.client.core.ui.login.LoginHelper
import org.sinou.pydia.client.core.ui.login.localLoginGraph
import org.sinou.pydia.client.core.ui.login.models.LoginVM
import org.sinou.pydia.client.core.ui.models.BrowseRemoteVM
import org.sinou.pydia.client.core.ui.system.systemNavGraph
import org.sinou.pydia.client.core.util.rememberContentPaddingForScreen
import org.sinou.pydia.sdk.utils.Log

@Composable
fun NavGraph(
    isExpandedScreen: Boolean,
    appState: AppState,
    navController: NavHostController,
    openDrawer: () -> Unit,
    navigateTo: (String) -> Unit,
    launchIntent: (Intent?, Boolean, Boolean) -> Unit,
    browseRemoteVM: BrowseRemoteVM = koinViewModel(),
    loginVM: LoginVM = koinViewModel(),
) {


    val loginHelper = LoginHelper(
        navController = navController,
        loginVM = loginVM,
        navigateTo = navigateTo
    )

    val logTag = "NavGraph"

    Log.i(logTag, "### Composing nav graph for ${appState.route}")
    LaunchedEffect(key1 = appState.route) {
        appState.route?.let { dest ->
            Log.e(logTag, "      currRoute: ${navController.currentDestination?.route}")
            Log.e(logTag, "      newRoute: $dest")
            navController.navigate(dest)
        } ?: run {
            Log.i(logTag, "### Forcing navigation to accounts")
            navController.navigate(CellsDestinations.Accounts.route)
        }
    }

    NavHost(
        navController = navController,
        startDestination = CellsDestinations.Accounts.route,
        route = CellsDestinations.Root.route
    ) {

        composable(CellsDestinations.Accounts.route) {
            Log.i(logTag, "### Composing Accounts")
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

        localLoginGraph(loginHelper, loginVM)

        browseNavGraph(
            isExpandedScreen = isExpandedScreen,
            navController = navController,
            openDrawer = openDrawer,
            back = { navController.popBackStack() },
            browseRemoteVM = browseRemoteVM,
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
