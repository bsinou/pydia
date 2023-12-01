package org.sinou.pydia.client.ui.browse.screens

import android.content.res.Configuration
import android.text.format.Formatter
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sinou.pydia.client.core.ListContext
import org.sinou.pydia.client.core.ListType
import org.sinou.pydia.client.core.LoadingState
import org.sinou.pydia.client.R
import org.sinou.pydia.client.core.db.nodes.RLiveOfflineRoot
import org.sinou.pydia.client.core.db.runtime.RJob
import org.sinou.pydia.client.core.services.models.ConnectionState
import org.sinou.pydia.client.ui.browse.BrowseHelper
import org.sinou.pydia.client.ui.browse.composables.NodeAction
import org.sinou.pydia.client.ui.browse.composables.NodeMoreMenuData
import org.sinou.pydia.client.ui.browse.composables.NodeMoreMenuType
import org.sinou.pydia.client.ui.browse.composables.OfflineRootItem
import org.sinou.pydia.client.ui.browse.menus.MoreMenuState
import org.sinou.pydia.client.ui.browse.menus.SortByMenu
import org.sinou.pydia.client.ui.browse.models.OfflineVM
import org.sinou.pydia.client.ui.core.ListLayout
import org.sinou.pydia.client.ui.core.composables.TopBarWithMoreMenu
import org.sinou.pydia.client.ui.core.composables.animations.SmoothLinearProgressIndicator
import org.sinou.pydia.client.ui.core.composables.getJobStatus
import org.sinou.pydia.client.ui.core.composables.getNodeTitle
import org.sinou.pydia.client.ui.core.composables.lists.LargeCardWithIcon
import org.sinou.pydia.client.ui.core.composables.lists.LargeCardWithImage
import org.sinou.pydia.client.ui.core.composables.lists.WithLoadingListBackground
import org.sinou.pydia.client.ui.core.composables.menus.CellsModalBottomSheetLayout
import org.sinou.pydia.client.ui.core.composables.modal.ModalBottomSheetValue
import org.sinou.pydia.client.ui.core.composables.modal.rememberModalBottomSheetState
import org.sinou.pydia.client.ui.models.toErrorMessage
import org.sinou.pydia.client.ui.theme.CellsIcons
import org.sinou.pydia.client.ui.theme.UseCellsTheme
import org.sinou.pydia.client.core.util.asAgoString
import org.sinou.pydia.sdk.transport.StateID
import kotlinx.coroutines.launch

private const val LOG_TAG = "OfflineRoots.kt"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineRoots(
    isExpandedScreen: Boolean,
    openDrawer: () -> Unit,
    offlineVM: OfflineVM,
    browseHelper: BrowseHelper,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val listLayout by offlineVM.layout.collectAsState(ListLayout.LIST)
    val loadingState = offlineVM.connectionState.collectAsState()

    val errMsg = offlineVM.errorMessage.collectAsState(null)
    val syncJob = offlineVM.syncJob.collectAsState()
    val roots = offlineVM.offlineRoots.collectAsState()

    LaunchedEffect(key1 = errMsg.value) {
        errMsg.value?.let {
            snackBarHostState.showSnackbar(
                message = toErrorMessage(context, it),
                withDismissAction = false,
                duration = SnackbarDuration.Short
            )
        }
    }

    val localOpen: (StateID) -> Unit = { stateID ->
        scope.launch {
            browseHelper.open(context, stateID, browseHelper.offline)
        }
    }

    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val nodeMoreMenuData: MutableState<Pair<NodeMoreMenuType, StateID>> = remember {
        mutableStateOf(
            Pair(
                NodeMoreMenuType.OFFLINE,
                StateID.NONE
            )
        )
    }
    val openMoreMenu: (NodeMoreMenuType, StateID) -> Unit = { type, stateID ->
        scope.launch {
            nodeMoreMenuData.value = Pair(type, stateID)
            sheetState.expand()
        }
    }

    val moreMenuDone: () -> Unit = {
        scope.launch {
            sheetState.hide()
            nodeMoreMenuData.value = Pair(
                NodeMoreMenuType.BOOKMARK,
                StateID.NONE
            )
        }
    }

    val destinationPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*"),
        onResult = { uri ->
            if (nodeMoreMenuData.value.second != StateID.NONE) {
                uri?.let {
                    offlineVM.download(nodeMoreMenuData.value.second, uri)
                }
            }
            moreMenuDone()
        }
    )

    val launch: (NodeAction, StateID) -> Unit = { action, stateID ->
        when (action) {
            is NodeAction.OpenInApp -> {
                moreMenuDone()
                scope.launch {
                    offlineVM.getNode(stateID)?.let {
                        if (it.isFolder()) {
                            localOpen(stateID)
                        } else {
                            localOpen(stateID.parent())
                        }
                    }
                }
            }

            is NodeAction.ForceResync -> {
                offlineVM.forceSync(stateID)
                moreMenuDone()
            }

            is NodeAction.DownloadToDevice -> {
                destinationPicker.launch(stateID.fileName)
                // Done is called by the destination picker callback
            }

            is NodeAction.ToggleOffline -> {
                offlineVM.removeFromOffline(stateID)
                moreMenuDone()
            }

            is NodeAction.AsGrid -> {
                offlineVM.setListLayout(ListLayout.GRID)
                moreMenuDone()
            }

            is NodeAction.AsList -> {
                offlineVM.setListLayout(ListLayout.LIST)
                moreMenuDone()
            }

            else -> {
                Log.e(LOG_TAG, "Unknown action $action for $stateID")
                moreMenuDone()
            }
        }
    }

    WithScaffold(
        isExpandedScreen = isExpandedScreen,
        connectionState = loadingState.value,
        listLayout = listLayout,
        syncJob = syncJob.value,
        title = stringResource(id = R.string.action_open_offline_roots),
        roots = roots.value,
        openDrawer = openDrawer,
        forceRefresh = offlineVM::forceFullSync,
        open = localOpen,
        launch = launch,
        moreMenuState = MoreMenuState(
            sheetState,
            nodeMoreMenuData.value.first,
            nodeMoreMenuData.value.second,
            openMoreMenu
        ),
        snackBarHostState = snackBarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WithScaffold(
    isExpandedScreen: Boolean,
    connectionState: ConnectionState,
    listLayout: ListLayout,
    syncJob: RJob?,
    title: String,
    roots: List<RLiveOfflineRoot>,
    openDrawer: () -> Unit,
    forceRefresh: () -> Unit,
    open: (StateID) -> Unit,
    launch: (NodeAction, StateID) -> Unit,
    moreMenuState: MoreMenuState,
    snackBarHostState: SnackbarHostState,
) {

    var isShown by remember { mutableStateOf(false) }
    val showMenu: (Boolean) -> Unit = {
        if (it != isShown) {
            isShown = it
        }
    }

    val actionMenuContent: @Composable ColumnScope.() -> Unit = {
        if (listLayout == ListLayout.GRID) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.button_switch_to_list_layout)) },
                onClick = {
                    launch(
                        NodeAction.AsList,
                        StateID.NONE
                    )
                    showMenu(false)
                },
                leadingIcon = {
                    Icon(
                        CellsIcons.AsList,
                        stringResource(R.string.button_switch_to_list_layout)
                    )
                },
            )
        } else {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.button_switch_to_grid_layout)) },
                onClick = {
                    launch(
                        NodeAction.AsGrid,
                        StateID.NONE
                    )
                    showMenu(false)
                },
                leadingIcon = {
                    Icon(
                        CellsIcons.AsGrid,
                        stringResource(R.string.button_switch_to_grid_layout)
                    )
                },
            )
        }

        val label = stringResource(R.string.button_open_sort_by)
        DropdownMenuItem(
            text = { Text(label) },
            onClick = {
                moreMenuState.openMoreMenu(
                    NodeMoreMenuType.SORT_BY,
                    StateID.NONE
                )
                showMenu(false)
            },
            leadingIcon = { Icon(CellsIcons.SortBy, label) },
        )
    }

    Scaffold(
        topBar = {
            TopBarWithMoreMenu(
                title = title,
                isExpandedScreen = isExpandedScreen,
                openDrawer = openDrawer,
                isActionMenuShown = isShown,
                showMenu = showMenu,
                content = actionMenuContent
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { padding ->

        CellsModalBottomSheetLayout(
            isExpandedScreen = isExpandedScreen,
            sheetContent = {
                if (moreMenuState.type == NodeMoreMenuType.SORT_BY) {
                    SortByMenu(
                        type = ListType.DEFAULT,
                        done = { launch(NodeAction.SortBy, StateID.NONE) },
                    )
                } else {
                    NodeMoreMenuData(
                        connectionState = connectionState,
                        type = NodeMoreMenuType.OFFLINE,
                        subjectID = moreMenuState.stateID,
                        launch = launch,
                    )
                }
            },
            sheetState = moreMenuState.sheetState,
        ) {
            OfflineRootsList(
                connectionState = connectionState,
                listLayout = listLayout,
                syncJob = syncJob,
                roots = roots,
                forceRefresh = forceRefresh,
                openMoreMenu = { moreMenuState.openMoreMenu(NodeMoreMenuType.OFFLINE, it) },
                open = open,
                padding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                        .plus(dimensionResource(R.dimen.margin_medium)),
                    start = dimensionResource(R.dimen.list_horizontal_padding),
                    end = dimensionResource(R.dimen.list_horizontal_padding),
                ),
            )
        }
    }
}

@OptIn(
    ExperimentalMaterialApi::class, ExperimentalFoundationApi::class,
    ExperimentalFoundationApi::class
)
@Composable
private fun OfflineRootsList(
    connectionState: ConnectionState,
    listLayout: ListLayout,
    syncJob: RJob?,
    roots: List<RLiveOfflineRoot>,
    forceRefresh: () -> Unit,
    openMoreMenu: (StateID) -> Unit,
    open: (StateID) -> Unit,
    padding: PaddingValues,
) {

    val state = rememberPullRefreshState(
        refreshing = connectionState.loading == LoadingState.PROCESSING,
        onRefresh = {
            Log.e(LOG_TAG, "Force refresh launched")
            forceRefresh()
        },
    )

    WithLoadingListBackground(
        connectionState = connectionState,
        isEmpty = roots.isEmpty(),
        listContext = ListContext.OFFLINE,
        emptyRefreshableDesc = stringResource(id = R.string.no_offline_root_for_account),
        modifier = Modifier.fillMaxSize()
    ) {

        Box(Modifier.pullRefresh(state)) { // .fillMaxSize()) {
            when (listLayout) {
                ListLayout.GRID -> {
                    val listPadding = PaddingValues(
                        top = padding.calculateTopPadding().plus(dimensionResource(R.dimen.margin)),
                        bottom = padding.calculateBottomPadding()
                            .plus(dimensionResource(R.dimen.margin)),
                        start = dimensionResource(id = R.dimen.margin_medium),
                        end = dimensionResource(id = R.dimen.margin_medium),
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = dimensionResource(R.dimen.grid_large_col_min_width)),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.grid_large_padding)),
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.grid_large_padding)),
                        contentPadding = listPadding,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (syncJob != null) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                val progress = if (syncJob.total <= 0) {
                                    -1F
                                } else {
                                    (syncJob.progress).toFloat().div(syncJob.total)
                                }
                                SyncStatus(
                                    desc = getJobStatus(item = syncJob),
                                    progress = progress,
                                    syncJob.doneTimestamp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        items(
                            items = roots,
                            key = { it.encodedState }) { node ->
                            if (node.hasThumb()) {
                                LargeCardWithImage(
                                    stateID = node.getStateID(),
                                    eTag = node.etag,
                                    // TODO fix this: we will miss some changes
                                    metaHash = -1,
                                    mime = node.mime,
                                    title = getNodeTitle(name = node.name, mime = node.mime),
                                    desc = getDesc(node),
                                    openMoreMenu = { openMoreMenu(node.getStateID()) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { open(node.getStateID()) }
                                        .animateItemPlacement(),
                                )
                            } else {
                                LargeCardWithIcon(
                                    sortName = node.sortName,
                                    mime = node.mime,
                                    title = getNodeTitle(name = node.name, mime = node.mime),
                                    desc = getDesc(node),
                                    openMoreMenu = { openMoreMenu(node.getStateID()) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { open(node.getStateID()) }
                                        .animateItemPlacement(),
                                )
                            }
                        }
                    }
                }

                else -> {

                    LazyColumn(
                        contentPadding = padding,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (syncJob != null) {
                            item {
                                val progress = if (syncJob.total <= 0) {
                                    -1F
                                } else {
                                    (syncJob.progress).toFloat().div(syncJob.total)
                                }
                                SyncStatus(
                                    desc = getJobStatus(item = syncJob),
                                    progress = progress,
                                    syncJob.doneTimestamp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        items(roots, key = { it.encodedState }) { offlineRoot ->
                            OfflineRootItem(
                                item = offlineRoot,
                                title = getNodeTitle(
                                    name = offlineRoot.name,
                                    mime = offlineRoot.mime
                                ),
                                desc = getDesc(offlineRoot),
                                more = { openMoreMenu(offlineRoot.getStateID()) },
                                modifier = Modifier
                                    .clickable { open(offlineRoot.getStateID()) }
                                    .animateItemPlacement(),
                            )
                        }
                        item {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dimensionResource(R.dimen.list_bottom_fab_padding))
                            )
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = connectionState.loading == LoadingState.PROCESSING,
                state = state,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun SyncStatus(
    desc: String,
    progress: Float,
    doneTS: Long,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = dimensionResource(R.dimen.list_item_elevation),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = dimensionResource(R.dimen.list_item_inner_padding))
        ) {
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (doneTS <= 0) {
                if (progress == -1f) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = dimensionResource(id = R.dimen.margin_small))
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                } else if (progress in 0.0..1.0) {
                    SmoothLinearProgressIndicator(
                        indicatorProgress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = dimensionResource(id = R.dimen.margin_small))
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun getDesc(item: RLiveOfflineRoot): String {
    val context = LocalContext.current
    val prefix = stringResource(R.string.last_check)
    val mTimeValue = if (item.lastCheckTs > 1) {
        asAgoString(item.lastCheckTs)
    } else {
        stringResource(R.string.last_check_never)
    }
    val sizeValue = Formatter.formatShortFileSize(context, item.size)
    return "$prefix: $mTimeValue • $sizeValue"
}

@Preview(name = "SyncStatus Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "SyncStatus Dark Mode"
)
@Composable
private fun SyncStatusPreview() {
    UseCellsTheme {
        SyncStatus(
            "Pydio Cells server",
            -1f,
            0,
            Modifier
        )
    }
}
