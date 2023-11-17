package org.sinou.pydia.client.core

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.KoinContext
import org.sinou.pydia.client.core.services.ConnectionService
import org.sinou.pydia.client.core.ui.AppState
import org.sinou.pydia.client.core.ui.MainController
import org.sinou.pydia.client.core.ui.core.screens.WhiteScreen
import org.sinou.pydia.client.core.ui.login.models.OAuthProcessState
import org.sinou.pydia.client.core.ui.login.models.OAuthVM
import org.sinou.pydia.client.core.ui.login.screens.AuthScreen
import org.sinou.pydia.sdk.transport.StateID

/**
 * Main entry point for the Cells Application:
 *
 * - We check if we should forward to the migrate activity
 * - If no migration is necessary, we handle the bundle / intent and then forward everything to Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    private val logTag = "MainActivity"
    private val connectionService: ConnectionService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()
        Log.i(logTag, "... onCreate for main activity, bundle: $savedInstanceState")
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        var appIsReady = false
        val mainActivity = this
        setContent {
            KoinContext {
                AppBinding(
                    activity = mainActivity,
                    sBundle = savedInstanceState,
                    intentID = intentIdentifier(),
                    launchIntent = mainActivity::launchIntent
                ) {
                    appIsReady = true
                }
            }
        }

        // Set up an OnPreDrawListener to the root view.
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (appIsReady) { // Check whether the initial data is ready.
                        // Content is ready: Start drawing.
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                    }
                    Log.d(logTag, "... In onPreDraw(), ready: $appIsReady")
                    return appIsReady
                }
            }
        )
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Composable
    fun AppBinding(
        activity: Activity,
        sBundle: Bundle?,
        intentID: String,
        launchIntent: (Intent?, Boolean, Boolean) -> Unit,
        readyCallback: () -> Unit,
    ) {
        Log.e(logTag, "... Composing AppBinding with\n\tintent[$intentID]: $intent\n\tb: $sBundle ")
        val intentHasBeenProcessed = rememberSaveable { mutableStateOf(false) }
        val appState = remember { mutableStateOf(AppState.NONE) }
        val oauthVM by viewModel<OAuthVM>()
        val processState by oauthVM.processState.collectAsState()

        LaunchedEffect(key1 = intentID) {
            if (intentHasBeenProcessed.value) {
                Log.w(logTag, "intent has already been processed...")
                return@LaunchedEffect
            }
            appState.value = handleIntent(sBundle, oauthVM) ?: AppState(StateID.NONE, null)
            readyCallback()
            intentHasBeenProcessed.value = true
        }

        // Prepare the BaseUiState
        val widthSizeClass = calculateWindowSizeClass(activity).widthSizeClass

        Box {

            when (processState) {
                // FIXME Handle the case where we have to explicitly navigate to a new page (e.G: new account...)
                OAuthProcessState.DONE,
                OAuthProcessState.SKIP ->
                    MainController(
                        appState = appState.value,
                        launchIntent = launchIntent,
                        widthSizeClass = widthSizeClass,
                    )

                OAuthProcessState.PROCESSING -> ProcessAuth(oauthVM)

                OAuthProcessState.NEW -> {
                    Log.d(logTag, "... At Compose root, not yet ready")
                    WhiteScreen()
                }
            }
        }
    }

    @Composable
    fun ProcessAuth(loginVM: OAuthVM) {
        val message = loginVM.message.collectAsState()
        val errMsg = loginVM.errorMessage.collectAsState()
        AuthScreen(
            isProcessing = errMsg.value.isNullOrEmpty(),
            message = message.value,
            errMsg = errMsg.value,
            cancel = { TODO("Reimplement") }
        )
    }

    override fun onPause() {
        connectionService.pauseMonitoring()
        super.onPause()
    }

    override fun onResume() {
        connectionService.relaunchMonitoring()
        super.onResume()
    }

    private fun intentIdentifier(): String {
        var id: String? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            id = intent.identifier
        }
        id = id ?: run {// tmp hack: compute a local ID based on a few variables
            "${intent.categories}/${intent.action}/${intent.component}"
        }
        Log.e(logTag, "#### Got an intent identifier: $id")
        return id
    }

    /**
     * This should not throw any error
     */
    private suspend fun handleIntent(
        sBundle: Bundle?,
        oauthVM: OAuthVM
    ): AppState? {
        try {
            val msg = "... Launching processing for ($intent)\n" +
                    "\t- cmp: ${intent.component}\n\t- action${intent.action}" +
                    "\n\t- categories: ${intent.categories}" //+
            Log.e(logTag, msg)
            if (sBundle != null) {
                TODO("Handle non-null saved bundle state")
            }

            // Handle various supported events
            when (intent.action) {
                Intent.ACTION_MAIN -> {
                    oauthVM.skip()
                }

                Intent.ACTION_VIEW -> {
                    val code = intent.data?.getQueryParameter(AppNames.QUERY_KEY_CODE)
                    val state = intent.data?.getQueryParameter(AppNames.QUERY_KEY_STATE)

                    if (code == null || state == null) {
                        throw IllegalArgumentException("Received an unexpected VIEW intent: $intent")
                    }

                    val (isValid, targetStateID) = oauthVM.isAuthStateValid(state)
                    if (!isValid) {
                        throw IllegalArgumentException("Passed state is wrong or already consumed: $intent")
                    }

                    oauthVM.launchCodeManagement(state, code)
                }

//            Intent.ACTION_SEND == intent.action -> {
//                val clipData = intent.clipData
//                Log.d(logTag, "ACTION_SEND received, clipData: $clipData")
//                clipData?.let {
//                    startingState.route = ShareDestination.ChooseAccount.route
//                    clipData.getItemAt(0).uri?.let {
//                        startingState.uris.add(it)
//                    }
//                }
//            }
//
//            Intent.ACTION_SEND_MULTIPLE == intent.action -> {
//                val tmpClipData = intent.clipData
//                tmpClipData?.let { clipData ->
//                    startingState.route = ShareDestination.ChooseAccount.route
//                    for (i in 0 until clipData.itemCount) {
//                        clipData.getItemAt(i).uri?.let {
//                            startingState.uris.add(it)
//                        }
//                    }
//                }
//            }

                else -> {
                    val action = intent.action
                    var categories = ""
                    intent.categories?.forEach { categories += "$it, " }
                    Log.w(logTag, "... Unexpected intent: $action - $categories")
                }
            }
            // FIXME Handle errors here.
        } catch (e: Exception) {
            Log.e(logTag, "Could not handle intent, doing nothing...")
            e.printStackTrace()
//                if (e is SDKException) {
//                    Log.e(logTag, "After handleIntent, error thrown: ${e.code} - ${e.message}")
//                    if (e.code == ErrorCodes.unexpected_content) { // We should never have received this
//                        Log.e(logTag, "Launch activity with un-valid state, ignoring...")
//                        activity.finishAndRemoveTask()
//                        return@LaunchedEffect
//                    }
//                }
//                Log.e(logTag, "Could not handle intent, aborting....")
//                throw e
        }
        return null
    }

    private fun launchIntent(
        intent: Intent?,
        checkIfKnown: Boolean,
        alsoFinishCurrentActivity: Boolean
    ) {
        if (intent == null) {
            finishAndRemoveTask()
        } else if (checkIfKnown) {
            val resolvedActivity =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val flag = PackageManager.ResolveInfoFlags
                        .of(MATCH_DEFAULT_ONLY.toLong())
                    packageManager.resolveActivity(intent, flag)
                } else {
                    packageManager.resolveActivity(intent, MATCH_DEFAULT_ONLY)
                }
            // TODO better error handling
            if (resolvedActivity == null) {
                Log.e(logTag, "No Matching handler found for $intent")
            }
        } else {
            startActivity(intent)
            if (alsoFinishCurrentActivity) {
                finishAndRemoveTask()
            }
        }
    }
}
