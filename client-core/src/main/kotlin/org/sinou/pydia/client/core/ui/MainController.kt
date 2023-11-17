package org.sinou.pydia.client.core.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.sinou.pydia.client.core.AppKeys
import org.sinou.pydia.client.core.services.ConnectionService
import org.sinou.pydia.client.core.ui.core.composables.WithInternetBanner
import org.sinou.pydia.client.core.ui.core.lazyStateID
import org.sinou.pydia.client.core.ui.core.nav.AppDrawer
import org.sinou.pydia.client.core.ui.core.nav.AppPermanentDrawer
import org.sinou.pydia.client.core.ui.core.nav.CellsNavigationActions
import org.sinou.pydia.client.core.ui.system.SystemNavigationActions
import org.sinou.pydia.client.core.ui.theme.UseCellsTheme
import org.sinou.pydia.client.core.utils.rememberContentPaddingForScreen
import org.sinou.pydia.sdk.transport.StateID

private const val LOG_TAG = "MainHost.kt"

class AppState(
    val stateID: StateID,
    val route: String?
) {
    companion object {
        val NONE = AppState(
            StateID.NONE,
            null
        )
    }
}

@Composable
fun MainController(
    widthSizeClass: WindowWidthSizeClass,
    appState: AppState,
    launchIntent: (Intent?, Boolean, Boolean) -> Unit,
    connectionService: ConnectionService = koinInject(),
) {
    val scope = rememberCoroutineScope()

    val mainNavController = rememberNavController()
    val cellsNavActions = remember(mainNavController) {
        CellsNavigationActions(mainNavController)
    }

    val currAppState = remember {
        mutableStateOf(appState)
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
        val bseList = mainNavController.currentBackStack.value
        Log.e(LOG_TAG, "... Backstack b4 navigation from DRAWER")
        var i = 1
        for (bse in bseList) {
            Log.e(LOG_TAG, " #$i: ${bse.destination.route}")
            i++
        }
        Log.e(LOG_TAG, "... Next destination $route")

        val oldRoute = mainNavController.previousBackStackEntry?.destination?.route
        val oldState: StateID? = oldRoute?.let {
            Log.i(LOG_TAG, "... Got an old route: $oldRoute")
            if (it.endsWith("{${AppKeys.STATE_ID}}")) {
                Log.i(LOG_TAG, "... with state id suffix ")
                lazyStateID(mainNavController.previousBackStackEntry)
            } else {
                null
            }
        }
        val currRoute = mainNavController.currentBackStackEntry?.destination?.route
        val currState: StateID? = currRoute?.let {
            if (it.contains("{${AppKeys.STATE_ID}}")) {
                lazyStateID(entry = mainNavController.currentBackStackEntry)
            } else {
                null
            }
        }

        Log.i(LOG_TAG, "... Navigate to $route")
        Log.d(LOG_TAG, "      - Penultimate Entry route: $oldRoute, stateID: $oldState")
        Log.d(LOG_TAG, "      - Current Entry route: $currRoute, stateID: $currState")
        Log.d(LOG_TAG, "      - Local last route: ${lastRoute.value}")
        lastRoute.value = route
        scope.launch {
            mainNavController.navigate(route)
        }
    }

    val customColor = connectionService.customColor.collectAsState(null)
    val isExpandedScreen = widthSizeClass == WindowWidthSizeClass.Expanded
    val sizeAwareDrawerState = rememberSizeAwareDrawerState(isExpandedScreen)

    UseCellsTheme(
        customColor = customColor.value
    ) {
        ModalNavigationDrawer(
            drawerContent = {
                AppDrawer(
//                    currRoute = navBackStackEntry?.destination?.route,
//                    currSelectedID = lazyStateID(entry = navBackStackEntry, verbose = false),
                    appState = currAppState.value,
                    closeDrawer = { scope.launch { sizeAwareDrawerState.close() } },
                    connectionService = connectionService,
                    cellsNavActions = cellsNavActions,
                    systemNavActions = systemNavActions,
//                    browseNavActions = browseNavActions,
                )
            },
            drawerState = sizeAwareDrawerState,
            // Only enable opening the drawer via gestures if the screen is not expanded
            gesturesEnabled = !isExpandedScreen
        ) {
            Row {
                if (isExpandedScreen) { // When we are on a tablet
                    AppPermanentDrawer(
                        appState = currAppState.value,
//                        currRoute = navBackStackEntry?.destination?.route,
//                        currSelectedID = lazyStateID(navBackStackEntry),
                        connectionService = connectionService,
                        cellsNavActions = cellsNavActions,
                        systemNavActions = systemNavActions,
//                        browseNavActions = browseNavActions,
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
                        isExpandedScreen = isExpandedScreen,
                        appState = currAppState.value,
                        navController = mainNavController,
                        openDrawer = {
                            if (!isExpandedScreen) {
                                scope.launch { sizeAwareDrawerState.open() }
                            }
                        },
                        navigateTo = navigateTo,
                        launchIntent = launchIntent,
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

    // OAuth credential flow call back
    var code: String? = null
    var state: String? = null

    // Share with Pydio
    var uris: MutableList<Uri> = mutableListOf()
}