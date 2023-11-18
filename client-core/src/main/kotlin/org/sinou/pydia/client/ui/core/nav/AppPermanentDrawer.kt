package org.sinou.pydia.client.ui.core.nav

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.sinou.pydia.client.R
import org.sinou.pydia.client.core.services.ConnectionService
import org.sinou.pydia.client.ui.AppState
import org.sinou.pydia.client.ui.core.composables.ConnectionStatus
import org.sinou.pydia.client.ui.core.composables.MenuTitleText
import org.sinou.pydia.client.ui.core.composables.menus.BottomSheetDivider
import org.sinou.pydia.client.ui.system.SystemDestinations
import org.sinou.pydia.client.ui.system.SystemNavigationActions
import org.sinou.pydia.client.ui.system.models.PrefReadOnlyVM
import org.sinou.pydia.client.ui.theme.CellsIcons
import org.sinou.pydia.sdk.transport.StateID

/** AppNavRail provides the main lateral "rail" menu on large screens. */
@Composable
fun AppPermanentDrawer(
    appState: AppState,
//    currRoute: String?,
//    currSelectedID: StateID?,
    prefReadOnlyVM: PrefReadOnlyVM = koinViewModel(),
    connectionService: ConnectionService,
    cellsNavActions: CellsNavigationActions,
    systemNavActions: SystemNavigationActions,
//     browseNavActions: BrowseNavigationActions
) {
    val currRoute = appState.route
    val showDebugTools = prefReadOnlyVM.showDebugTools.collectAsState(initial = false)
    val accountID = connectionService.currAccountID.collectAsState(StateID.NONE)
    val wss = connectionService.wss.collectAsState(listOf())
    val cells = connectionService.cells.collectAsState(listOf())

    val defaultPadding = PaddingValues(horizontal = dimensionResource(R.dimen.horizontal_padding))
    val defaultModifier = Modifier.padding(defaultPadding)
    val defaultTitleModifier = defaultModifier.padding(
        PaddingValues(
            top = 12.dp,
            bottom = 8.dp,
        )
    )

    PermanentDrawerSheet(

        windowInsets = WindowInsets.systemBars
            //.only(if (excludeTop) WindowInsetsSides.Bottom else WindowInsetsSides.Vertical)
            .add(WindowInsets(bottom = 12.dp)),
        modifier = Modifier.width(200.dp),
        //drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        drawerTonalElevation = 12.dp,
    ) {

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            ConnectionStatus()

            AccountRailHeader(
                username = accountID.value?.username ?: stringResource(R.string.ask_url_title),
                address = accountID.value?.serverUrl ?: "",
                openAccounts = { cellsNavActions.navigateToAccounts() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(defaultPadding)
                    .padding(vertical = dimensionResource(id = R.dimen.margin))
            )

            // TOOD this must be injected when everything is modular
//            accountID.value?.let { currAccountID ->
//
//                MyNavigationRailItem(
//                    label = stringResource(R.string.action_open_offline_roots),
//                    icon = CellsIcons.KeepOffline,
//                    selected = BrowseDestinations.OfflineRoots.isCurrent(currRoute),
//                    onClick = { browseNavActions.toOfflineRoots(currAccountID) },
//                )
//                MyNavigationRailItem(
//                    label = stringResource(R.string.action_open_bookmarks),
//                    icon = CellsIcons.Bookmark,
//                    selected = BrowseDestinations.Bookmarks.isCurrent(currRoute),
//                    onClick = { browseNavActions.toBookmarks(currAccountID) },
//                )
//                MyNavigationRailItem(
//                    label = stringResource(R.string.action_open_transfers),
//                    icon = CellsIcons.Transfers,
//                    selected = BrowseDestinations.Transfers.isCurrent(currRoute),
//                    onClick = { browseNavActions.toTransfers(currAccountID) },
//                )
//
//                BottomSheetDivider()
//
//                MenuTitleText(stringResource(R.string.my_workspaces), defaultTitleModifier)
//                wss.value.listIterator().forEach {
//                    val selected = BrowseDestinations.Open.isCurrent(currRoute)
//                            && it.getStateID() == currSelectedID
//                    MyNavigationRailItem(
//                        label = it.label ?: it.slug,
//                        icon = getWsThumbVector(it.sortName ?: ""),
//                        selected = selected,
//                        onClick = { browseNavActions.toBrowse(it.getStateID()) },
//                    )
//                }
//                cells.value.listIterator().forEach {
//                    val selected = BrowseDestinations.Open.isCurrent(currRoute)
//                            && it.getStateID() == currSelectedID
//                    MyNavigationRailItem(
//                        label = it.label ?: it.slug,
//                        icon = getWsThumbVector(it.sortName ?: ""),
//                        selected = selected,
//                        onClick = { browseNavActions.toBrowse(it.getStateID()) },
//                    )
//                }
//            } ?: run { // Temporary fallback when no account is defined
//                // until all routes are hardened for all corner cases
//                MyNavigationRailItem(
//                    label = stringResource(id = R.string.choose_account),
//                    icon = Icons.Filled.Group,
//                    selected = CellsDestinations.Accounts.route == currRoute,
//                    onClick = { cellsNavActions.navigateToAccounts() },
//                )
//            }

            BottomSheetDivider()

            MenuTitleText(stringResource(R.string.my_account), defaultTitleModifier)
            MyNavigationRailItem(
                label = stringResource(R.string.action_settings),
                icon = CellsIcons.Settings,
                selected = SystemDestinations.Settings.route == currRoute,
                onClick = { systemNavActions.navigateToSettings() },
            )
            accountID.value?.let { accID -> // We also temporarily disable this when no account is defined
                // TODO Remove the check once the "clear cache" / housekeeping strategy has been refined
                MyNavigationRailItem(
                    label = stringResource(R.string.action_house_keeping),
                    icon = CellsIcons.EmptyRecycle,
                    selected = SystemDestinations.ClearCache.isCurrent(currRoute),
                    onClick = { systemNavActions.navigateToClearCache(accID) },
                )
            }
            if (showDebugTools.value) {
                MyNavigationRailItem(
                    label = stringResource(R.string.action_open_jobs),
                    icon = CellsIcons.Jobs,
                    selected = SystemDestinations.Jobs.route == currRoute,
                    onClick = { systemNavActions.navigateToJobs() },
                )
                MyNavigationRailItem(
                    label = stringResource(R.string.action_open_logs),
                    icon = CellsIcons.Logs,
                    selected = SystemDestinations.Logs.route == currRoute,
                    onClick = { systemNavActions.navigateToLogs() },
                )
            }
            MyNavigationRailItem(
                label = stringResource(id = R.string.action_open_about),
                icon = CellsIcons.About,
                selected = SystemDestinations.About.route == currRoute,
                onClick = { systemNavActions.navigateToAbout() },
            )
        }
    }
}

@Composable
fun MyNavigationRailItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        icon = { Icon(icon, label) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.height(dimensionResource(R.dimen.menu_item_height)),
        shape = ShapeDefaults.Small,
    )
}
