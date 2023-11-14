package org.sinou.pydia.client.core.ui.system

import android.content.Intent
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.sinou.pydia.client.core.ui.core.lazyStateID
import org.sinou.pydia.client.core.ui.core.nav.CellsDestinations
import org.sinou.pydia.client.core.ui.system.models.HouseKeepingVM
import org.sinou.pydia.client.core.ui.system.screens.AboutScreen
import org.sinou.pydia.client.core.ui.system.screens.HouseKeeping
import org.sinou.pydia.client.core.ui.system.screens.JobScreen
import org.sinou.pydia.client.core.ui.system.screens.LogScreen
import org.sinou.pydia.client.core.ui.system.screens.SettingsScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * App-wide system and technical pages.
 */
fun NavGraphBuilder.systemNavGraph(
    isExpandedScreen: Boolean,
    navController: NavHostController,
    openDrawer: () -> Unit = {},
    launchIntent: (Intent?, Boolean, Boolean) -> Unit,
    back: () -> Unit,
) {

    // val logTag = "SystemNavGraph"

    composable(SystemDestinations.About.route) {
        AboutScreen(
            isExpandedScreen = isExpandedScreen,
            openDrawer = openDrawer,
            launchIntent = launchIntent,
        )
    }

    composable(SystemDestinations.Jobs.route) {
        JobScreen(openDrawer = openDrawer)
    }

    composable(SystemDestinations.Logs.route) {
        LogScreen(openDrawer = openDrawer)
    }

    composable(SystemDestinations.ClearCache.route) { nbsEntry ->
        val stateID = lazyStateID(nbsEntry)
        val houseKeepingVM: HouseKeepingVM = koinViewModel(parameters = { parametersOf(stateID) })
        HouseKeeping(
            isExpandedScreen = isExpandedScreen,
            houseKeepingVM = houseKeepingVM,
            openDrawer = openDrawer,
            dismiss = {
                if (it) {
                    navController.navigate(CellsDestinations.Accounts.route)
                } else {
                    back()
                }
            },
        )
    }

    composable(SystemDestinations.Settings.route) {
        SettingsScreen(
            isExpandedScreen = isExpandedScreen,
            openDrawer = openDrawer,
        )
    }
}
