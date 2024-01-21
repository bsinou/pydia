package org.sinou.pydia.client.core

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinContext
import org.koin.core.parameter.parametersOf
import org.sinou.pydia.client.core.services.ConnectionService
import org.sinou.pydia.client.core.util.currentTimestamp
import org.sinou.pydia.client.ui.MainApp
import org.sinou.pydia.client.ui.core.screens.AuthScreen
import org.sinou.pydia.client.ui.core.screens.WhiteScreen
import org.sinou.pydia.client.ui.system.models.PreLaunchState
import org.sinou.pydia.client.ui.system.models.PreLaunchVM
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

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        var appIsReady = false
        val splashTimeout = currentTimestamp() + 1

        // Keep the splash screen visible while loading
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                // Logic to determine if the splash screen should still be shown
                // Return true to keep it, false to hide it
                currentTimestamp() > splashTimeout
            }
        }

        Log.i(logTag, "... onCreate for main activity, bundle: $savedInstanceState")
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            TODO("Handle non-null saved bundle state")
        }
        WindowCompat.setDecorFitsSystemWindows(window, true)

        val mainActivity = this

        setContent {
            // Prepare the BaseUiState: should probably not be handled here
            val widthSizeClass = calculateWindowSizeClass(mainActivity).widthSizeClass
            KoinContext {
                val showContent = rememberSaveable { mutableStateOf(false) }
                LaunchedEffect(key1 = true) {
                    // Launch your animation
                    Log.e(logTag, "Before sleep")
                    delay(1200L)
                    Log.e(logTag, "After sleep")
                    showContent.value = true
                }
                if (showContent.value) {
                    AppBinding(
                        intentID = intentIdentifier(),
                        widthSizeClass = widthSizeClass,
                    ) {
                        appIsReady = true
                    }
                }
//                val showSplash = rememberSaveable { mutableStateOf(true) }
//                if (showSplash.value){
//                    UseCellsTheme {
//                        AnimatedSplashScreen {
//                            showSplash.value = false
//                        }
//                    }
//                } else {
//                    AppBinding(
//                        intentID = intentIdentifier(),
//                        widthSizeClass = widthSizeClass,
//                    ) {
//                        appIsReady = true
//                    }
//                }
            }
        }

        // Set up an OnPreDrawListener to the root view.
        val timeout = currentTimestamp() + 120
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (appIsReady || currentTimestamp() > timeout) { // Check whether the initial data is ready.
                        // Content is ready: Start drawing.
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                    }
                    return appIsReady
                }
            }
        )
    }

    @Composable
    fun AppBinding(
        intentID: String,
        widthSizeClass: WindowWidthSizeClass,
        readyCallback: () -> Unit,
    ) {

        val scope = rememberCoroutineScope()
        val preLaunchVM: PreLaunchVM = koinViewModel(parameters = { parametersOf(intentID) })

        var intentHasBeenProcessed by rememberSaveable { mutableStateOf(false) }
        val appState by preLaunchVM.appState.collectAsState()
        val processState by preLaunchVM.processState.collectAsState()

        val emitActivityResult: (Int) -> Unit = { res ->
            setResult(res)
            when (res) {
                RESULT_CANCELED -> finishAndRemoveTask()
                RESULT_OK -> finish()
                else -> {} // Do nothing
            }
        }

        val processSelectedTarget: (StateID?) -> Unit = { stateID ->
            scope.launch {
                stateID?.let {
                    // preLaunchVM.shareAt(it)
                }
            }
        }

        LaunchedEffect(key1 = intentID) {
            val msg = "... First composition for AppBinding with:" +
                    "\n\tintent: [$intentID]"
            Log.e(logTag, msg)
            if (intentHasBeenProcessed) {
                Log.w(logTag, "intent has already been processed...")
                preLaunchVM.skip()
            } else {
                handleIntent(preLaunchVM)
            }
            readyCallback()
            intentHasBeenProcessed = true
        }


        Box {
            when (processState) {
                PreLaunchState.TERMINATE -> {
                    LaunchedEffect(Unit) {
                        emitActivityResult(Activity.RESULT_OK)
                    }
                }

                PreLaunchState.DONE,
                PreLaunchState.SKIP ->
                    MainApp(
                        initialAppState = appState,
                        processSelectedTarget = processSelectedTarget,
                        emitActivityResult = emitActivityResult,
                        widthSizeClass = widthSizeClass,
                    )

                PreLaunchState.PROCESSING,
                PreLaunchState.ERROR -> {
                    val message = preLaunchVM.message.collectAsState()
                    val errMsg = preLaunchVM.errorMessage.collectAsState()
                    AuthScreen(
                        isProcessing = errMsg.value.isNullOrEmpty(),
                        message = message.value,
                        errMsg = errMsg.value,
                        cancel = { emitActivityResult(RESULT_CANCELED) }
                    )
                }

                PreLaunchState.NEW -> {
                    Log.d(logTag, "... At Compose root, not yet ready")
                    WhiteScreen()
                }
            }
        }
    }

    override fun onPause() {
        connectionService.pauseMonitoring()
        super.onPause()
    }

    override fun onResume() {
        connectionService.relaunchMonitoring()
        super.onResume()
    }

    private suspend fun handleIntent(preLaunchVM: PreLaunchVM) {
        try {
            val msg = "... Processing intent ($intent):\n" +
                    "\t- cmp: ${intent.component}\n\t- action${intent.action}" +
                    "\n\t- categories: ${intent.categories}" //+
            Log.i(logTag, msg)

            // Handle various supported events
            when (intent.action) {
                Intent.ACTION_MAIN -> {
                    preLaunchVM.launchApp()
                }

                Intent.ACTION_VIEW -> {
                    val code = intent.data?.getQueryParameter(AppKeys.QUERY_KEY_CODE)
                    val state = intent.data?.getQueryParameter(AppKeys.QUERY_KEY_STATE)

                    if (code == null || state == null) {
                        throw IllegalArgumentException("Received an unexpected VIEW intent: $intent")
                    }

                    if (!preLaunchVM.isAuthStateValid(state)) {
                        throw IllegalArgumentException("Passed state is wrong or already consumed: $intent")
                    }

                    preLaunchVM.handleOAuthCode(state, code)
                }

                Intent.ACTION_SEND -> {
                    val clipData = intent.clipData ?: run {
                        throw IllegalArgumentException("Cannot share with no clip data: $intent")
                    }
                    preLaunchVM.handleShare(clipData)
                }

                Intent.ACTION_SEND_MULTIPLE -> {
                    val clipData = intent.clipData ?: run {
                        throw IllegalArgumentException("Cannot share with no clip data: $intent")
                    }
                    preLaunchVM.handleShares(clipData)
                }

                else -> throw IllegalArgumentException("Unexpected intent: $intent")
            }
        } catch (e: IllegalArgumentException) {
            Log.e(logTag, "Misconfigured intent $intent, cannot start the app: ${e.message}")
            e.printStackTrace()
            preLaunchVM.skip()
        } catch (e: Exception) {
            Log.e(logTag, "Unexpected Error while handling main intent: ${e.message}")
            e.printStackTrace()
            preLaunchVM.skip()
        }
    }

    private fun intentIdentifier(): String {
        var id: String? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            id = intent.identifier
        }
        id = id ?: run {// tmp hack: compute a local ID based on a few variables
            "${intent.categories}/${intent.action}/${intent.component}"
        }
        return id
    }
}
