package org.sinou.android.pydia

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.pydio.cells.api.SDKException
import com.pydio.cells.transport.ClientData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.sinou.android.pydia.room.account.AccountDB
import org.sinou.android.pydia.room.browse.TreeNodeDB
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.services.NodeService
import java.io.File

/**
 * Main entry point of the Pydio client application.
 *
 * Does nothing exotic to begin with */
class CellsApp : Application() {

    private val tag = "CellsApp"

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    companion object {
        lateinit var instance: CellsApp
            private set
    }

    lateinit var accountService: AccountService
    lateinit var nodeService: NodeService

    override fun onCreate() {
        super.onCreate()
        instance = this

        delayedInit()
    }

    private fun delayedInit(){
        applicationScope.launch {
            updateClientData()
            initServices()
            // TODO also set-up worker tasks tasks

            Log.i(tag, "Delayed init terminated")
        }
    }

    fun baseDir(): File? {
        val appDir = baseContext.getApplicationInfo().dataDir
        Log.w(tag, "Data dir: $appDir")
        Log.w(tag, "File dir: $filesDir")
        return filesDir
    }

    private fun initServices() {

        accountService = AccountService(
            AccountDB.getDatabase(this.applicationContext),
            baseDir()?.absolutePath
        )

        nodeService = NodeService(
            TreeNodeDB.getDatabase(this.applicationContext) ,
            accountService,
            filesDir,
        )
    }

    @Throws(SDKException::class)
    private fun updateClientData() {

        val packageName: String = this.packageName
        val packageInfo: PackageInfo
        packageInfo = try {
            applicationContext.getPackageManager().getPackageInfo(packageName, 0)
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

    private fun getAndroidVersion(): String? {
        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        return "AndroidSDK" + sdkVersion + "v" + release
    }

}