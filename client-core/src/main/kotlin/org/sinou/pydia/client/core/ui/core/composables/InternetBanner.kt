package org.sinou.pydia.client.core.ui.core.composables

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import org.sinou.pydia.client.core.LoginStatus
import org.sinou.pydia.client.core.NetworkStatus
import org.sinou.pydia.client.R
import org.sinou.pydia.client.core.services.AuthService
import org.sinou.pydia.client.core.services.ConnectionService
import org.sinou.pydia.client.core.services.ErrorService
import org.sinou.pydia.client.core.services.SessionState
import org.sinou.pydia.client.core.ui.login.LoginDestinations
import org.sinou.pydia.client.core.ui.theme.CellsColor
import org.sinou.pydia.client.core.ui.theme.CellsIcons
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private const val LOG_TAG = "InternetBanner.kt"

@Composable
fun WithInternetBanner(
    contentPadding: PaddingValues,
    connectionService: ConnectionService,
    navigateTo: (String) -> Unit,
    errorService: ErrorService = koinInject(),
    content: @Composable () -> Unit
) {

    val localNavigateTo: (String) -> Unit =
        {    // clean error stack before launching the re-log process
            errorService.clearStack()
            navigateTo(it)
        }

    // TODO add bottom sheet
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        InternetBanner(connectionService, localNavigateTo)
        content()
    }
}

@Composable
private fun InternetBanner(
    // accountService: AccountService,
    connectionService: ConnectionService,
    navigateTo: (String) -> Unit,
) {

    val scope = rememberCoroutineScope()
    val currSession = connectionService.sessionView.collectAsState(initial = null)
    val sessionStatus = connectionService.sessionStateFlow.collectAsState(
        SessionState(NetworkStatus.OK, true, LoginStatus.Connected)
    )
    // val knownSessions = accountService.getLiveSessions().collectAsState(listOf())

    currSession.value?.let {
        val currState = sessionStatus.value

        when {
            currState.isServerReachable && !currState.loginStatus.isConnected()
            -> {
                Log.e(LOG_TAG, "Credentials Expired ")
                CredExpiredStatus(
                    icon = CellsIcons.NoValidCredentials,
                    desc = stringResource(R.string.auth_err_expired),
                    onClick = {
                        scope.launch {
                            currSession.value?.let {
                                val route = if (it.isLegacy) {
                                    Log.i(LOG_TAG, "... Launching re-log on P8 for ${it.accountID}")
                                    LoginDestinations.P8Credentials.createRoute(
                                        it.getStateID(),
                                        it.skipVerify(),
                                        AuthService.LOGIN_CONTEXT_BROWSE
                                    )
                                } else {
                                    Log.i(
                                        LOG_TAG,
                                        "... Launching re-log on Cells for ${it.accountID} from ${it.getStateID()}"
                                    )
                                    LoginDestinations.LaunchAuthProcessing.createRoute(
                                        it.getStateID(),
                                        it.skipVerify(),
                                        AuthService.LOGIN_CONTEXT_BROWSE
                                    )
                                }
                                navigateTo(route)
                            } ?: run {
                                Log.e(LOG_TAG, "... Cannot launch, empty session view")
                            }
                        }
                    }
                )
            }

            else -> {
                // No header
            }
        }
    }


//    if (currSession.value != null) {
    //  SessionStatus.OK != sessionStatus.value && )
//        when {
//            currState.isOK() -> {
//                // No header
//            }
//
//            currState.networkStatus == NetworkStatus.UNAVAILABLE
//            -> ConnectionStatus(
//                icon = CellsIcons.NoInternet,
//                desc = stringResource(R.string.no_internet)
//            )
//
//            currState.networkStatus == NetworkStatus.CAPTIVE
//            -> ConnectionStatus(
//                icon = CellsIcons.CaptivePortal,
//                desc = stringResource(R.string.captive_portal)
//            )
//
//            !currState.isServerReachable -> {
//                if (knownSessions.value.isEmpty()) {
//                    ConnectionStatus(
//                        icon = CellsIcons.ServerUnreachable,
//                        desc = stringResource(R.string.no_account)
//                    )
//                } else {
//                    ConnectionStatus(
//                        icon = CellsIcons.ServerUnreachable,
//                        desc = stringResource(R.string.server_unreachable)
//                    )
//                }
//            }
//
//            currState.isServerReachable && !currState.loginStatus.isConnected()
//            -> {
//                Log.e(LOG_TAG, "Credentials Expired ")
//                CredExpiredStatus(
//                    icon = CellsIcons.NoValidCredentials,
//                    desc = stringResource(R.string.auth_err_expired),
//                    onClick = {
//                        scope.launch {
//                            currSession.value?.let {
//                                val route = if (it.isLegacy) {
//                                    Log.i(LOG_TAG, "... Launching re-log on P8 for ${it.accountID}")
//                                    LoginDestinations.P8Credentials.createRoute(
//                                        it.getStateID(),
//                                        it.skipVerify(),
//                                        AuthService.LOGIN_CONTEXT_BROWSE
//                                    )
//                                } else {
//                                    Log.i(
//                                        LOG_TAG,
//                                        "... Launching re-log on Cells for ${it.accountID} from ${it.getStateID()}"
//                                    )
//                                    LoginDestinations.LaunchAuthProcessing.createRoute(
//                                        it.getStateID(),
//                                        it.skipVerify(),
//                                        AuthService.LOGIN_CONTEXT_BROWSE
//                                    )
//                                }
//                                navigateTo(route)
//                            } ?: run {
//                                Log.e(LOG_TAG, "... Cannot launch, empty session view")
//                            }
//                        }
//                    }
//                )
//            }
//
//            // TODO also handle preferences on limited networks
//            currState.networkStatus == NetworkStatus.METERED
//                    || currState.networkStatus == NetworkStatus.ROAMING
//            -> ConnectionStatus(
//                icon = CellsIcons.Metered,
//                desc = stringResource(R.string.metered_connection),
//                type = ServerConnection.LIMITED
//            )
//
//            else -> {
//                Log.e(LOG_TAG, "Unexpected status: $currState")
//
//                ConnectionStatus(
//                    icon = CellsIcons.NoValidCredentials,
//                    desc = stringResource(id = R.string.auth_err_no_token),
//                    type = ServerConnection.UNREACHABLE
//                )
//            }
//        }
//    }
}


@Composable
private fun CredExpiredStatus(
    icon: ImageVector,
    desc: String,
    onClick: () -> Unit
) {

    val tint = CellsColor.warning
    val bg = CellsColor.warning.copy(alpha = .1f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(
                horizontal = dimensionResource(R.dimen.margin_small),
                vertical = dimensionResource(R.dimen.margin_xxsmall)
            )
    ) {
        Icon(
            tint = tint,
            imageVector = icon,
            contentDescription = desc,
            modifier = Modifier.size(dimensionResource(id = R.dimen.list_trailing_icon_size))
        )
        Spacer(Modifier.size(dimensionResource(R.dimen.list_item_inner_h_padding)))
        Text(
            text = desc,
            color = tint,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onClick) {
            Text(
                text = stringResource(R.string.launch_auth).uppercase(),
                color = tint,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
