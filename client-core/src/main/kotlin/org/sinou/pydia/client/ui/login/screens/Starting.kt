package org.sinou.pydia.client.ui.login.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.sinou.pydia.client.ui.theme.UseCellsTheme

@Composable
fun StartingLoginProcess() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        CircularProgressIndicator()
    }
}


@Preview(name = "Starting Login process Light")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Starting Login process Dark"
)
@Composable
private fun ProcessAuthPreview() {
    UseCellsTheme {
        StartingLoginProcess()
    }
}
