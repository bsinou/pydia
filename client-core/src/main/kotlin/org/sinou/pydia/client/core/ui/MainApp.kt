package org.sinou.pydia.client.core.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import org.sinou.pydia.sdk.transport.StateID

// private const val logTag = "MainApp"

@Composable
fun MainApp(
    startingState: StartingState?,
    ackStartStateProcessed: (String?, StateID) -> Unit,
    launchIntent: (Intent?, Boolean, Boolean) -> Unit,
    launchTaskFor: (String, StateID) -> Unit,
    widthSizeClass: WindowWidthSizeClass,
) {
    NavHostWithDrawer(
        startingState = startingState,
        ackStartStateProcessed = ackStartStateProcessed,
        launchIntent = launchIntent,
        launchTaskFor = launchTaskFor,
        widthSizeClass = widthSizeClass,
    )
}

class StartingState(var stateID: StateID) {
    var route: String? = null

    // OAuth credential flow call back
    var code: String? = null
    var state: String? = null

    // Share with Pydio
    var uris: MutableList<Uri> = mutableListOf()
}