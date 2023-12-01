package org.sinou.pydia.client.ui.core.composables

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
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.sinou.pydia.client.R
import org.sinou.pydia.client.core.services.AuthService
import org.sinou.pydia.client.core.services.ConnectionService
import org.sinou.pydia.client.core.services.ErrorService
import org.sinou.pydia.client.ui.login.LoginDestinations
import org.sinou.pydia.client.ui.theme.CellsColor
import org.sinou.pydia.client.ui.theme.CellsIcons

private const val LOG_TAG = "InternetBanner.kt"

@Composable
fun WithInternetBanner(
    connectionService: ConnectionService,
    errorService: ErrorService = koinInject(),
    navigateTo: (String) -> Unit,
    contentPadding: PaddingValues,
    content: @Composable () -> Unit
) {

    val localNavigateTo: (String) -> Unit = {
        // clean error stack before launching the re-log process
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
    connectionService: ConnectionService,
    navigateTo: (String) -> Unit,
) {

    val scope = rememberCoroutineScope()
    val currSession = connectionService.sessionView.collectAsState(initial = null)
    val currStatus = connectionService.sessionStateFlow.collectAsState()

    currSession.value?.let {
        val currState = currStatus.value

        when {
            currState.isServerReachable && !currState.loginStatus.isConnected()
            -> {
                CredExpiredStatus(
                    icon = CellsIcons.NoValidCredentials,
                    desc = stringResource(R.string.auth_err_expired),
                    onClick = {
                        scope.launch {
                            currSession.value?.let {
                                val msg = "... Re-logging @${it.accountID} from ${it.getStateID()}"
                                Log.i(LOG_TAG, msg)
                                val route = LoginDestinations.LaunchAuthProcessing.createRoute(
                                    it.getStateID(),
                                    it.skipVerify(),
                                    AuthService.LOGIN_CONTEXT_BROWSE
                                )
                                navigateTo(route)
                            } ?: run {
                                Log.e(LOG_TAG, "Cannot launch, empty session view")
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
}

@Composable
private fun CredExpiredStatus(
    icon: ImageVector,
    desc: String,
    onClick: () -> Unit
) {

    val tint = CellsColor.warning
    val bg = CellsColor.warning.copy(alpha = .1f)
    Log.w(LOG_TAG, "... Server is reachable but we are not connected")

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
