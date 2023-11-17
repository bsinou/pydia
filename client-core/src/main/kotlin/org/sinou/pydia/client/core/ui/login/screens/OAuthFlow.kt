package org.sinou.pydia.client.core.ui.login.screens

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sinou.pydia.client.R
import org.sinou.pydia.client.core.ui.core.composables.TitleDescColumnBloc
import org.sinou.pydia.client.core.ui.login.LoginHelper
import org.sinou.pydia.client.core.ui.login.models.LoginVM
import org.sinou.pydia.client.core.ui.theme.UseCellsTheme
import org.sinou.pydia.sdk.transport.StateID


@Composable
fun LaunchAuthProcessing(
    stateID: StateID,
    skipVerify: Boolean,
    loginContext: String,
    loginVM: LoginVM,
    helper: LoginHelper,
) {
    val logTag = "LaunchAuthProcessing"

    val message = loginVM.message.collectAsState()
    val errMsg = loginVM.errorMessage.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(key1 = stateID) {
        Log.i(logTag, "... Launch auth process for $stateID")
        helper.launchAuth(context, stateID, skipVerify, loginContext)
    }

    AuthScreen(
        isProcessing = errMsg.value.isNullOrEmpty(),
        message = message.value,
        errMsg = errMsg.value,
        cancel = helper::cancel
    )
}

//@Composable
//fun ProcessAuth(
//    stateID: StateID,
//    loginVM: LoginVM,
//    helper: LoginHelper,
//) {
//    val logTag = "ProcessAuth"
//    val message = loginVM.message.collectAsState()
//    val errMsg = loginVM.errorMessage.collectAsState()
//
//    LaunchedEffect(key1 = stateID) {
//        Log.d(logTag, "About to Process Auth for ${helper.startingState?.route}")
//        helper.processAuth(stateID)
//    }
//
//    AuthScreen(
//        isProcessing = errMsg.value.isNullOrEmpty(),
//        message = message.value,
//        errMsg = errMsg.value,
//        cancel = helper::cancel
//    )
//}

@Composable
fun AuthScreen(
    isProcessing: Boolean,
    message: String?,
    errMsg: String?,
    cancel: () -> Unit,
) {

    val title = stringResource(R.string.oauth_code_flow_title)
    val desc = stringResource(R.string.oauth_code_flow_desc)

    Surface(modifier = Modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.form_page_padding))
                .wrapContentWidth(Alignment.Start)
        ) {

            TitleDescColumnBloc(title, desc)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .size(240.dp)
            ) {
                Image(
                    painterResource(R.drawable.pydio_logo),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
            if (isProcessing) {
                CircularProgressIndicator(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .alpha(.8f)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            if (errMsg.isNullOrEmpty()) {
                Text(
                    text = errMsg!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.margin_medium))
                        .wrapContentWidth(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.error
                )
            } else if (message.isNullOrEmpty()) {
                Text(
                    text = message!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.margin_medium))
                        .wrapContentWidth(Alignment.CenterHorizontally),
                )
            }

            TextButton(
                onClick = cancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp
                    )
                    .wrapContentWidth(Alignment.End),
            ) {
                Text(
                    text = stringResource(R.string.button_cancel).uppercase(),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(
                modifier = Modifier
                    .weight(0.2f)
                    .padding(bottom = dimensionResource(R.dimen.margin_header))
            )
        }
    }
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
