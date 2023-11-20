package org.sinou.pydia.client.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.sinou.pydia.client.core.util.rememberContentPaddingForScreen
import org.sinou.pydia.client.ui.account.AccountListVM
import org.sinou.pydia.client.ui.account.AccountsScreen
import org.sinou.pydia.client.ui.browse.browseNavGraph
import org.sinou.pydia.client.ui.browse.composables.Download
import org.sinou.pydia.client.ui.core.lazyStateID
import org.sinou.pydia.client.ui.core.nav.CellsDestinations
import org.sinou.pydia.client.ui.login.LoginHelper
import org.sinou.pydia.client.ui.login.localLoginGraph
import org.sinou.pydia.client.ui.login.models.LoginVM
import org.sinou.pydia.client.ui.models.BrowseRemoteVM
import org.sinou.pydia.client.ui.models.DownloadVM
import org.sinou.pydia.client.ui.system.systemNavGraph
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

    LaunchedEffect(key1 = appState.route) {
        Log.i(logTag, "### Composing nav graph for ${appState.route}")
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
            LaunchedEffect(Unit) {
                Log.i(logTag, "### Composing Accounts")
            }
            val accountListVM: AccountListVM = koinViewModel()
            AccountsScreen(
                isExpandedScreen = isExpandedScreen,
                accountListVM = accountListVM,
                navigateTo = navigateTo,
                openDrawer = openDrawer,
                contentPadding = rememberContentPaddingForScreen(
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

//        shareNavGraph(
//            isExpandedScreen = isExpandedScreen,
//            browseRemoteVM = browseRemoteVM,
//            helper = ShareHelper(
//                navController,
//                launchTaskFor,
//                startingState,
//                ackStartStateProcessing
//            ),
//            back = { navController.popBackStack() },
//        )

//        composable(CellsDestinations.Search.route) { entry ->
//            val searchVM: SearchVM =
//                koinViewModel(parameters = { parametersOf(lazyStateID(entry)) })
//            Search(
//                isExpandedScreen = isExpandedScreen,
//                queryContext = lazyQueryContext(entry),
//                stateID = lazyStateID(entry),
//                searchVM = searchVM,
//                SearchHelper(
//                    navController = navController,
//                    searchVM = searchVM
//                ),
//            )
//        }

        dialog(CellsDestinations.Download.route) { entry ->
            val downloadVM: DownloadVM =
                koinViewModel(parameters = { parametersOf(lazyStateID(entry)) })
            Download(
                stateID = lazyStateID(entry),
                downloadVM = downloadVM
            ) { navController.popBackStack() }
        }
    }
}
