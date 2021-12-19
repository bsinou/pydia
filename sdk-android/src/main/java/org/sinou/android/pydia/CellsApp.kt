package org.sinou.android.pydia

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.pydio.cells.api.SDKException
import com.pydio.cells.transport.ClientData
import org.sinou.android.pydia.room.account.AccountDB
import org.sinou.android.pydia.services.AccountRepository
import java.io.File

/**
 * Main entry point of the Pydia application.
 *
 * Does nothing exotic to begin with */
class CellsApp : Application() {

    companion object {
        lateinit var instance: CellsApp
            private set
    }

    lateinit var accountRepository: AccountRepository

    override fun onCreate() {
        super.onCreate()
        instance = this
        initServices()
    }

    fun baseDir(): File? {
        return baseContext.filesDir
    }

    // TODO make this in another thread
    private fun initServices() {
        updateClientData()
        accountRepository = AccountRepository(
            AccountDB.getDatabase(this.applicationContext),
            baseDir()?.absolutePath
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