package org.sinou.pydia.client.ui.login.screens

import android.content.res.Configuration
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import org.sinou.pydia.client.ui.core.screens.AuthScreen
import org.sinou.pydia.client.ui.login.LoginHelper
import org.sinou.pydia.client.ui.login.models.LoginVM
import org.sinou.pydia.client.ui.theme.UseCellsTheme
import org.sinou.pydia.sdk.transport.StateID

private const val LOG_TAG = "LaunchOAuthFlow.kt"

@Composable
fun LaunchOAuthFlow(
    stateID: StateID,
    skipVerify: Boolean,
    loginContext: String,
    loginVM: LoginVM,
    helper: LoginHelper,
) {
    val message = loginVM.message.collectAsState()
    val errMsg = loginVM.errorMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = stateID.id) {
            Log.i(LOG_TAG, "... Launching auth process for $stateID, login context: $loginContext")
            helper.launchAuth(context, stateID, skipVerify, loginContext)
    }

    AuthScreen(
        isProcessing = errMsg.value.isNullOrEmpty(),
        message = message.value,
        errMsg = errMsg.value,
        cancel = helper::cancel
    )
}

@Preview(name = "ProcessAuth Light")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "ProcessAuth Dark"
)
@Composable
private fun ProcessAuthPreview() {
    UseCellsTheme {
        AuthScreen(
            isProcessing = true,
            message = "Getting credentials...",
            errMsg = null,
            cancel = {}
        )
    }
}
