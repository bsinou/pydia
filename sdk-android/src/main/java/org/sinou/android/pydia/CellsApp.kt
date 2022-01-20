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
import org.sinou.android.pydia.room.account.AccountDB
import org.sinou.android.pydia.room.browse.TreeNodeDB
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
        Log.i(tag, "### onCreate")

        super.onCreate()
        instance = this
        delayedInit()
    }

    private fun delayedInit() {
        applicationScope.launch {

            updateClientData()
            sharedPreferences = getSharedPreferences(ClientData.clientID, Context.MODE_PRIVATE)

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
    private fun updateClientData() {

        val packageName: String = this.packageName
        val packageInfo: PackageInfo = try {
            applicationContext.packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            throw SDKException("Could not retrieve PackageInfo for $packageName", e)
        }

        // TODO make this more dynamic
        ClientData.clientID = "cells-mobile"
        ClientData.clientSecret = ""
        ClientData.packageID = packageName
        ClientData.name = "AndroidClient"

        ClientData.buildTimestamp = packageInfo.lastUpdateTime
        ClientData.version = packageInfo.versionName
        ClientData.versionCode = packageInfo.versionCode
        ClientData.platform = getAndroidVersion()
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