package org.sinou.android.pydia

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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
import org.sinou.android.pydia.services.TransferService

/**
 * Main entry point of the Pydio client application.
 *
 * Does nothing exotic to begin with */
class CellsApp : Application() {

    private val tag = "CellsApp"

    // Specific scope for the Delayed init. Cancels everything if one of its coroutine fails.
    private val launchScope = CoroutineScope(Dispatchers.Default)

    // Exposed to the whole app for tasks that must survive termination of the calling UI element
    // Typically for actions launched from the "More" menu (copy, move...)
    val appScope = CoroutineScope(SupervisorJob())

    var currentTheme = R.style.Theme_Cells

    // TODO Rather use an Application model that raises a flag when everything is setup.
    var ready = false

    lateinit var sharedPreferences: SharedPreferences
    lateinit var accountService: AccountService
    lateinit var nodeService: NodeService
    lateinit var fileService: FileService
    lateinit var transferService: TransferService

    companion object {
        lateinit var instance: CellsApp
            private set
    }

    override fun onCreate() {
        Log.i(tag, "#################################################################")
        Log.i(tag, "#########  Launching Cells Android Client application  ##########")
        Log.i(tag, "#################################################################")
        super.onCreate()
        instance = this
        delayedInit()
    }

    private fun delayedInit() {
        launchScope.launch {

            val clientID = updateClientData()
            sharedPreferences = getSharedPreferences(clientID, Context.MODE_PRIVATE)

            Log.i(tag, "... Pre-init done")

            // delay(1L)
            initServices()

            // TODO also set-up worker tasks
            ready = true
            Log.i(tag, "Delayed init terminated")
        }
    }

    private fun initServices() {

        // TODO use dependency injection
        accountService = AccountService(
            AccountDB.getDatabase(applicationContext),
            filesDir
        )

        Log.i(tag, "... Account service ready")

        fileService = FileService(
            accountService,
        )

        Log.i(tag, "... File service ready")

        nodeService = NodeService(
            accountService,
            fileService,
        )

        Log.i(tag, "... Node service ready")

        transferService = TransferService(
            accountService,
            nodeService,
            fileService,
            RuntimeDB.getDatabase(applicationContext),
        )

        Log.i(tag, "... Transfer service ready")

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
        instance.name = "AndroidClient"
        // TODO make this more dynamic
        instance.clientID = "cells-mobile"

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