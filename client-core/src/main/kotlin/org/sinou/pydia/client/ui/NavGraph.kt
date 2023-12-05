package org.sinou.pydia.client.ui

import android.app.Activity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.sinou.pydia.client.core.services.AuthService
import org.sinou.pydia.client.core.util.rememberContentPaddingForScreen
import org.sinou.pydia.client.ui.account.AccountListVM
import org.sinou.pydia.client.ui.account.AccountsScreen
import org.sinou.pydia.client.ui.browse.BrowseDestinations
import org.sinou.pydia.client.ui.browse.browseNavGraph
import org.sinou.pydia.client.ui.browse.composables.ConfirmDownloadOnLimitedConnection
import org.sinou.pydia.client.ui.browse.composables.Download
import org.sinou.pydia.client.ui.core.lazyQueryContext
import org.sinou.pydia.client.ui.core.lazyStateID
import org.sinou.pydia.client.ui.core.nav.CellsDestinations
import org.sinou.pydia.client.ui.login.LoginHelper
import org.sinou.pydia.client.ui.login.localLoginGraph
import org.sinou.pydia.client.ui.login.models.LoginVM
import org.sinou.pydia.client.ui.models.AppState
import org.sinou.pydia.client.ui.models.BrowseRemoteVM
import org.sinou.pydia.client.ui.models.DownloadVM
import org.sinou.pydia.client.ui.search.Search
import org.sinou.pydia.client.ui.search.SearchHelper
import org.sinou.pydia.client.ui.search.SearchVM
import org.sinou.pydia.client.ui.share.ShareDestinations
import org.sinou.pydia.client.ui.share.ShareHelper
import org.sinou.pydia.client.ui.share.shareNavGraph
import org.sinou.pydia.client.ui.system.systemNavGraph
import org.sinou.pydia.sdk.transport.StateID
import org.sinou.pydia.sdk.utils.Log

@Composable
fun NavGraph(
    initialAppState: AppState,
    isExpandedScreen: Boolean,
    navController: NavHostController,
    navigateTo: (String) -> Unit,
    openDrawer: () -> Unit,
    processSelectedTarget: (StateID?) -> Unit,
    emitActivityResult: (Int) -> Unit,
    loginVM: LoginVM = koinViewModel(),
    browseRemoteVM: BrowseRemoteVM = koinViewModel()
) {

    val logTag = "NavGraph"

    val loginHelper = LoginHelper(
        navController = navController,
        loginVM = loginVM,
        navigateTo = navigateTo
    )

    LaunchedEffect(key1 = initialAppState.intentID, key2 = initialAppState.route) {
        Log.i(logTag, "... new appState: ${initialAppState.route} - ${initialAppState.stateID}")
        initialAppState.context?.let {
            when (it) {
                AuthService.LOGIN_CONTEXT_BROWSE, AuthService.LOGIN_CONTEXT_SHARE -> {
                    // This should terminate the current task and fallback to where we were before re-launching the OAuth process
                    emitActivityResult(Activity.RESULT_OK)
                }

                AuthService.LOGIN_CONTEXT_CREATE -> {
                    navController.navigate(BrowseDestinations.Open.createRoute(initialAppState.stateID))
                }

                AuthService.LOGIN_CONTEXT_ACCOUNTS -> {
                    // Do Nothing
                }
            }
            return@LaunchedEffect
        }

        initialAppState.route?.let { dest ->
            when {
                ShareDestinations.UploadInProgress.isCurrent(dest) -> navController.navigate(dest) {
                    popUpTo(ShareDestinations.ChooseAccount.route) { inclusive = true }
                }

                else -> {
                    Log.e(logTag, "      currRoute: ${navController.currentDestination?.route}")
                    Log.e(logTag, "      newRoute: $dest")
                    navController.navigate(dest)
                }
            }
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

        localLoginGraph(helper = loginHelper, loginVM = loginVM)

        browseNavGraph(
            isExpandedScreen = isExpandedScreen,
            navController = navController,
            openDrawer = openDrawer,
            back = { navController.popBackStack() },
            browseRemoteVM = browseRemoteVM,
        )

        shareNavGraph(
            isExpandedScreen = isExpandedScreen,
            browseRemoteVM = browseRemoteVM,
            helper = ShareHelper(
                navController,
                processSelectedTarget,
                emitActivityResult,
            ),
            back = { navController.popBackStack() },
        )

        systemNavGraph(
            isExpandedScreen = isExpandedScreen,
            navController = navController,
            openDrawer = openDrawer,
            back = { navController.popBackStack() },
        )


        composable(CellsDestinations.Search.route) { entry ->
            val searchVM: SearchVM =
                koinViewModel(parameters = { parametersOf(lazyStateID(entry)) })
            Search(
                isExpandedScreen = isExpandedScreen,
                queryContext = lazyQueryContext(entry),
                stateID = lazyStateID(entry),
                searchVM = searchVM,
                SearchHelper(
                    navController = navController,
                    searchVM = searchVM
                ),
            )
        }

        dialog(CellsDestinations.Download.route) { entry ->
//            val downloadVM: DownloadVM =
//                koinViewModel(parameters = { parametersOf(lazyStateID(entry)) })
//            Download(
//                stateID = lazyStateID(entry),
//                downloadVM = downloadVM
//            ) { navController.popBackStack() }
            val stateID = lazyStateID(entry)
            val canDL = remember {
                mutableStateOf(false)
            }
            val mustConfirm = remember {
                mutableStateOf(false)
            }
            val downloadVM: DownloadVM =
                koinViewModel(parameters = { parametersOf(stateID) })

            LaunchedEffect(key1 = stateID) {
                android.util.Log.i(logTag, "## First Composition for: download/${stateID}")
                if (!downloadVM.mustConfirmDL(stateID)) {
                    canDL.value = true
                } else {
                    mustConfirm.value = true
                }
            }

            if (canDL.value) {
                Download(
                    stateID = stateID,
                    downloadVM = downloadVM
                ) { navController.popBackStack() }
            } else if (mustConfirm.value) {
                ConfirmDownloadOnLimitedConnection(stateID = stateID) {
                    if (it) {
                        canDL.value = true
                    } else {
                        navController.popBackStack()
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }

        }
    }
}
