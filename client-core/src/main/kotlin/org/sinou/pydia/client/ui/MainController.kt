package org.sinou.pydia.client.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.sinou.pydia.client.core.services.ConnectionService
import org.sinou.pydia.client.core.util.rememberContentPaddingForScreen
import org.sinou.pydia.client.ui.browse.BrowseNavigationActions
import org.sinou.pydia.client.ui.core.composables.WithInternetBanner
import org.sinou.pydia.client.ui.core.dumpNavigationStack
import org.sinou.pydia.client.ui.core.lazyStateID
import org.sinou.pydia.client.ui.core.nav.AppDrawer
import org.sinou.pydia.client.ui.core.nav.AppPermanentDrawer
import org.sinou.pydia.client.ui.core.nav.CellsNavigationActions
import org.sinou.pydia.client.ui.models.AppState
import org.sinou.pydia.client.ui.system.SystemNavigationActions
import org.sinou.pydia.client.ui.theme.UseCellsTheme
import org.sinou.pydia.sdk.transport.StateID

private const val LOG_TAG = "MainHost.kt"

@Composable
fun MainApp(
    initialAppState: AppState,
    processSelectedTarget: (StateID?) -> Unit,
    emitActivityResult: (Int) -> Unit,
//    widthSizeClass: WindowWidthSizeClass,
) {
    //Twister()
    MainController(
        initialAppState = initialAppState,
        processSelectedTarget = processSelectedTarget,
        emitActivityResult = emitActivityResult,
//        widthSizeClass = widthSizeClass,
    )
}

@SuppressLint("RestrictedApi")
@Composable
fun MainController(
    initialAppState: AppState,
    processSelectedTarget: (StateID?) -> Unit,
    emitActivityResult: (Int) -> Unit,
//    widthSizeClass: WindowWidthSizeClass,
    connectionService: ConnectionService = koinInject(),
) {
    val scope = rememberCoroutineScope()
    val mainNavController = rememberNavController()

    val cellsNavActions = remember(mainNavController) {
        CellsNavigationActions(mainNavController)
    }
    val browseNavActions = remember(mainNavController) {
        BrowseNavigationActions(mainNavController)
    }
    val systemNavActions = remember(mainNavController) {
        SystemNavigationActions(mainNavController)
    }

    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    // Debug: understand login loop issue
    val lastRoute = rememberSaveable { mutableStateOf("") }

    val navigateTo: (String) -> Unit = { route ->
        if (route == lastRoute.value) {
            Log.w(LOG_TAG, "[WARNING] Same route called twice: $route")
        }
        // FIXME remove
        //  also remove method annotation to suppress the error
        dumpNavigationStack(
            LOG_TAG,
            "Main NavigateTo in WithDrawer",
            mainNavController.currentBackStack.value,
            route
        )
        scope.launch {
            mainNavController.navigate(route)
        }
    }

    val customColor = connectionService.customColor.collectAsState(null)
    // FIXME we do not support detecting if we have touch screen
    // FIXME lost when migrating to version catalogs
    val isExpandedScreen = false
//    val isExpandedScreen = widthSizeClass == WindowWidthSizeClass.Expanded
    val sizeAwareDrawerState = rememberSizeAwareDrawerState(isExpandedScreen)

    UseCellsTheme(
        customColor = customColor.value
    ) {
        ModalNavigationDrawer(
            drawerContent = {
                AppDrawer(
                    currRoute = navBackStackEntry?.destination?.route,
                    currSelectedID = lazyStateID(entry = navBackStackEntry, verbose = false),
                    closeDrawer = { scope.launch { sizeAwareDrawerState.close() } },
                    connectionService = connectionService,
                    cellsNavActions = cellsNavActions,
                    systemNavActions = systemNavActions,
                    browseNavActions = browseNavActions,
                )
            },
            drawerState = sizeAwareDrawerState,
            // Only enable opening the drawer via gestures if the screen is not expanded
            gesturesEnabled = !isExpandedScreen
        ) {
            Row {
                if (isExpandedScreen) { // When we are on a tablet
                    AppPermanentDrawer(
                        currRoute = navBackStackEntry?.destination?.route,
                        currSelectedID = lazyStateID(navBackStackEntry),
                        connectionService = connectionService,
                        cellsNavActions = cellsNavActions,
                        systemNavActions = systemNavActions,
                        browseNavActions = browseNavActions,
                    )
                }
                WithInternetBanner(
                    connectionService = connectionService,
                    navigateTo = navigateTo,
                    contentPadding = rememberContentPaddingForScreen(
                        // additionalTop = if (!isExpandedScreen) 0.dp else 8.dp,
                        excludeTop = !isExpandedScreen
                    )
                ) {
                    NavGraph(
                        initialAppState = initialAppState,
                        isExpandedScreen = isExpandedScreen,
                        navController = mainNavController,
                        navigateTo = navigateTo,
                        processSelectedTarget = processSelectedTarget,
                        emitActivityResult = emitActivityResult,
                        openDrawer = {
                            if (!isExpandedScreen) {
                                scope.launch { sizeAwareDrawerState.open() }
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Determine the drawer state to pass to the modal drawer.
 */
@Composable
private fun rememberSizeAwareDrawerState(isExpandedScreen: Boolean): DrawerState {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    return if (!isExpandedScreen) {
        // If we want to allow showing the drawer, we use a real, remembered drawer
        // state defined above
        drawerState
    } else {
        // If we don't want to allow the drawer to be shown, we provide a drawer state
        // that is locked closed. This is intentionally not remembered, because we
        // don't want to keep track of any changes and always keep it closed
        DrawerState(DrawerValue.Closed)
    }
}

class StartingState(var stateID: StateID) {
    var route: String? = null

    // OAuth credential flow callback
    var code: String? = null
    var state: String? = null

    // Share with Pydio
//     var uris: MutableList<Uri> = mutableListOf()
}