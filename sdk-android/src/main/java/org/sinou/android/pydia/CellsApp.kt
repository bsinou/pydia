package org.sinou.android.pydia

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pydio.cells.api.SDKException
import com.pydio.cells.transport.ClientData
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.sinou.android.pydia.db.accounts.AccountDB
import org.sinou.android.pydia.db.runtime.RuntimeDB
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.services.FileService
import org.sinou.android.pydia.services.NodeService
import org.sinou.android.pydia.services.OfflineSyncWorker
import org.sinou.android.pydia.services.TransferService
import java.util.concurrent.TimeUnit

/**
 * Main entry point of the Pydio client application.
 *
 * Does nothing exotic to begin with */
class CellsApp : Application() {

    private val logTag = CellsApp::class.simpleName
    private lateinit var sharedPreferences: SharedPreferences

    // Specific scope for the Delayed init. Cancels everything if one of its coroutine fails.
    private val launchScope = CoroutineScope(Dispatchers.Default)

    // Exposed to the whole app for tasks that must survive termination of the calling UI element
    // Typically for actions launched from the "More" menu (copy, move...)
    val appScope = CoroutineScope(SupervisorJob())

    // var currentTheme = R.style.Theme_Cells

    // TODO Rather use an Application model that raises a flag when everything is setup.
    var ready = false

    lateinit var accountService: AccountService
    lateinit var nodeService: NodeService
    lateinit var fileService: FileService
    lateinit var transferService: TransferService

    companion object {
        lateinit var instance: CellsApp
            private set
    }

    override fun onCreate() {
        Log.i(logTag, "#################################################################")
        Log.i(logTag, "#########  Launching Cells Android Client application  ##########")
        Log.i(logTag, "#################################################################")
        super.onCreate()
        instance = this
        delayedInit()
    }

    private fun delayedInit() {
        launchScope.launch {

            val clientID = updateClientData()
            // sharedPreferences = getSharedPreferences(clientID, Context.MODE_PRIVATE)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            Log.i(logTag, "... Pre-init done")

            initServices()
            ready = true
            Log.i(logTag, "... Service initialized, configuring workers")

            setupOfflineWorker()
            Log.i(logTag, "... Delayed init terminated")
        }
    }

    private fun initServices() {

        // TODO use dependency injection
        accountService = AccountService(
            AccountDB.getDatabase(applicationContext),
            filesDir
        )

        Log.i(logTag, "... Account service ready")

        fileService = FileService(
            accountService,
        )

        Log.i(logTag, "... File service ready")

        nodeService = NodeService(
            accountService,
            fileService,
        )

        Log.i(logTag, "... Node service ready")

        transferService = TransferService(
            accountService,
            nodeService,
            fileService,
            RuntimeDB.getDatabase(applicationContext),
        )

        Log.i(logTag, "... Transfer service ready")

    }

    private fun setupOfflineWorker() {

        // TODO make this configurable

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
// Could be even more defensive:
//            .setRequiresCharging(true)
//            .apply {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    setRequiresDeviceIdle(true)
//                }
//            }
            .build()

        // Dev constraints. Do **not** use this in production
//        val repeatingRequest = PeriodicWorkRequestBuilder<OfflineSyncWorker>(
//            16,
//            TimeUnit.MINUTES
//        ).setConstraints(constraints)
//            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<OfflineSyncWorker>(
            1,
            TimeUnit.DAYS
        ).setConstraints(constraints)
            .build()

        WorkManager.getInstance(instance.applicationContext).enqueueUniquePeriodicWork(
            OfflineSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }

    @Throws(SDKException::class)
    private fun updateClientData(): String {

        val packageInfo: PackageInfo = try {
            applicationContext.packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            throw SDKException("Could not retrieve PackageInfo for $packageName", e)
        }

        val instance = ClientData.getInstance()
        instance.packageID = packageName
        instance.name = resources.getString(R.string.app_name)
        instance.clientID = resources.getString(R.string.client_id)
        instance.buildTimestamp = packageInfo.lastUpdateTime
        instance.version = packageInfo.versionName
        instance.versionCode = compatVersionCode(packageInfo)
        instance.platform = getAndroidVersion()

        ClientData.updateInstance(instance)
        return instance.clientID
    }

    @Suppress("DEPRECATION")
    private fun compatVersionCode(packageInfo: PackageInfo): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }
    }

    private fun getAndroidVersion(): String {
        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        return "AndroidSDK" + sdkVersion + "v" + release
    }

    fun getPreference(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun setPreference(key: String, value: String) {
        with(sharedPreferences.edit()) {
            putString(key, value)
            apply()
        }
    }

    fun getCurrentState(): StateID? {
        return getPreference(AppNames.PREF_KEY_CURRENT_STATE)?.let { StateID.fromId(it) }
    }

    fun setCurrentState(state: StateID) {
        setPreference(AppNames.PREF_KEY_CURRENT_STATE, state.id)
    }

    // TODO implement background cleaning, typically:
    //  - states
    //  - upload & downloads
}