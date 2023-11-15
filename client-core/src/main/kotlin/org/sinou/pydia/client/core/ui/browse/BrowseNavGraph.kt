package org.sinou.pydia.client.core.ui.browse

import android.util.Log
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
//import org.sinou.pydia.client.core.ui.browse.models.AccountHomeVM
//import org.sinou.pydia.client.core.ui.browse.models.BookmarksVM
//import org.sinou.pydia.client.core.ui.browse.models.CarouselVM
//import org.sinou.pydia.client.core.ui.browse.models.FolderVM
//import org.sinou.pydia.client.core.ui.browse.models.OfflineVM
//import org.sinou.pydia.client.core.ui.browse.models.TransfersVM
//import org.sinou.pydia.client.core.ui.browse.screens.AccountHome
//import org.sinou.pydia.client.core.ui.browse.screens.Bookmarks
//import org.sinou.pydia.client.core.ui.browse.screens.Carousel
//import org.sinou.pydia.client.core.ui.browse.screens.Folder
//import org.sinou.pydia.client.core.ui.browse.screens.NoAccount
//import org.sinou.pydia.client.core.ui.browse.screens.OfflineRoots
//import org.sinou.pydia.client.core.ui.browse.screens.Transfers
import org.sinou.pydia.client.core.ui.core.lazyStateID
import org.sinou.pydia.client.core.ui.core.nav.CellsDestinations
import org.sinou.pydia.client.core.ui.models.BrowseRemoteVM
import org.sinou.pydia.sdk.transport.StateID
import org.sinou.pydia.sdk.utils.Str
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.sinou.pydia.client.core.ui.core.screens.WhiteScreen

fun NavGraphBuilder.browseNavGraph(
    isExpandedScreen: Boolean,
    navController: NavHostController,
    openDrawer: () -> Unit,
    back: () -> Unit,
    // browseRemoteVM: BrowseRemoteVM,
) {

    val logTag = "BrowseNavGraph"

    composable(BrowseDestinations.Open.route) { navBackStackEntry ->
        val stateID = lazyStateID(navBackStackEntry)
        LaunchedEffect(key1 = stateID) {
            Log.i(logTag, "## First Composition for: browse/open/${stateID}")
        }


        WhiteScreen()
//        if (stateID == StateID.NONE) {
//            NoAccount(openDrawer = openDrawer, addAccount = {})
//        } else if (Str.notEmpty(stateID.slug)) {
//            val folderVM: FolderVM = koinViewModel(parameters = { parametersOf(stateID) })
//            Folder(
//                isExpandedScreen = isExpandedScreen,
//                folderID = stateID,
//                openDrawer = openDrawer,
//                openSearch = {
//                    navController.navigate(
//                        CellsDestinations.Search.createRoute("Folder", stateID)
//                    )
//                },
//                browseRemoteVM = browseRemoteVM,
//                folderVM = folderVM,
//                browseHelper = BrowseHelper(navController, folderVM),
//            )
//        } else {
//
//            val accountHomeVM: AccountHomeVM = koinViewModel(parameters = { parametersOf(stateID) })
//            val helper = BrowseHelper(navController, accountHomeVM)
//
//            AccountHome(
//                isExpandedScreen = isExpandedScreen,
//                accountID = stateID,
//                openDrawer = openDrawer,
//                openSearch = {
//                    navController.navigate(
//                        CellsDestinations.Search.createRoute(
//                            "AccountHome",
//                            stateID
//                        )
//                    )
//                },
//                browseRemoteVM = browseRemoteVM,
//                accountHomeVM = accountHomeVM,
//                browseHelper = helper,
//            )
//        }
//
//        DisposableEffect(key1 = stateID) {
//            if (stateID == StateID.NONE) {
//                browseRemoteVM.pause(StateID.NONE)
//            } else {
//                browseRemoteVM.watch(stateID, false)
//            }
//            onDispose {
//                Log.d(logTag, "onDispose for browse/open/$stateID, pause browseRemoteVM")
//                browseRemoteVM.pause(stateID)
//            }
//        }
    }

//    composable(BrowseDestinations.OpenCarousel.route) { navBackStackEntry ->
//        val stateID = lazyStateID(navBackStackEntry)
//        val carouselVM: CarouselVM = koinViewModel(parameters = { parametersOf(stateID) })
//        Carousel(
//            stateID,
//            // back = back,
//            carouselVM,
//        )
//    }
//
//    composable(BrowseDestinations.OfflineRoots.route) { navBackStackEntry ->
//        val stateID = lazyStateID(navBackStackEntry)
//        LaunchedEffect(key1 = stateID) {
//            Log.i(logTag, "## First Composition for: browse/offline/$stateID")
//        }
//        if (stateID == StateID.NONE) {
//            Log.e(logTag, "Cannot open OfflineRoots with no ID")
//            back()
//        } else {
//            val offlineVM: OfflineVM = koinViewModel(parameters = { parametersOf(stateID) })
//            val helper = BrowseHelper(navController, offlineVM)
//            OfflineRoots(
//                isExpandedScreen = isExpandedScreen,
//                openDrawer = openDrawer,
//                offlineVM = offlineVM,
//                browseHelper = helper,
//            )
//        }
//    }
//
//    composable(BrowseDestinations.Bookmarks.route) { navBackStackEntry ->
//        val stateID = lazyStateID(navBackStackEntry)
//        LaunchedEffect(key1 = stateID) {
//            Log.i(logTag, "## First Composition for: browse/bookmarks/$stateID")
//        }
//        if (stateID == StateID.NONE) {
//            Log.e(logTag, "Cannot open bookmarks with no ID")
//            back()
//        } else {
//            val bookmarksVM: BookmarksVM = koinViewModel(parameters = { parametersOf(stateID) })
//            val helper = BrowseHelper(navController, bookmarksVM)
//            Bookmarks(
//                isExpandedScreen = isExpandedScreen,
//                stateID,
//                openDrawer = openDrawer,
//                browseHelper = helper,
//                bookmarksVM = bookmarksVM,
//            )
//        }
//    }
//
//    composable(BrowseDestinations.Transfers.route) { navBackStackEntry ->
//        val stateID = lazyStateID(navBackStackEntry)
//        if (stateID == StateID.NONE) {
//            Log.e(logTag, "Cannot open Transfers with no ID")
//            back()
//        } else {
//            val transfersVM: TransfersVM =
//                koinViewModel(parameters = { parametersOf(browseRemoteVM.isLegacy, stateID) })
//            val helper = BrowseHelper(navController, transfersVM)
//            Transfers(
//                isExpandedScreen = isExpandedScreen,
//                openDrawer = openDrawer,
//                accountID = stateID.account(),
//                transfersVM = transfersVM,
//                browseHelper = helper,
//            )
//        }
//    }
}
