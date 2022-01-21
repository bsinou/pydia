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
import kotlinx.coroutines.launch
import org.sinou.android.pydia.db.account.AccountDB
import org.sinou.android.pydia.db.browse.TreeNodeDB
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.services.NodeService

/**
 * Main entry point of the Pydio client application.
 *
 * Does nothing exotic to begin with */
class CellsApp : Application() {

    private val tag = "CellsApp"

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    // rather use an Application model that raises a flag when everything is setup.
    var ready = false

    lateinit var sharedPreferences: SharedPreferences
    lateinit var accountService: AccountService
    lateinit var nodeService: NodeService


    companion object {
        lateinit var instance: CellsApp
            private set
    }

    override fun onCreate() {
        Log.i(tag, "###############################")
        Log.i(tag, "### Launching Cells application")
        super.onCreate()
        instance = this
        delayedInit()
    }

    private fun delayedInit() {
        applicationScope.launch {

            val clientID = updateClientData()
            sharedPreferences = getSharedPreferences(clientID, Context.MODE_PRIVATE)

            // delay(1L)
            initServices()

            // TODO also set-up worker tasks
            Log.i(tag, "Delayed init terminated")
            ready = true
        }
    }

    private fun initServices() {

        accountService = AccountService(
            AccountDB.getDatabase(applicationContext),
            filesDir
        )

        nodeService = NodeService(
            TreeNodeDB.getDatabase(applicationContext),
            accountService,
            filesDir,
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
            packageInfo.versionCode as Long
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