package org.sinou.pydia.client.core

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.sinou.pydia.client.R
import org.sinou.pydia.client.core.di.allModules
import org.sinou.pydia.client.core.services.JobService
import org.sinou.pydia.client.core.services.WorkerService
import org.sinou.pydia.client.core.utils.timestampForLogMessage
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.transport.ClientData
import org.sinou.pydia.sdk.utils.Log

/**
 * Main entry point of the Pydia client application.
 */
class PydiaSimpleApp : Application(), KoinComponent {

    private val logTag = "PydiaSimpleApp"

    companion object {
        lateinit var instance: PydiaSimpleApp
            private set
    }

    override fun onCreate() {

        setLogger()

        // FIXME it seems we have some leaks and some not correctly tagged sockets
        // Enable resource leak tracking during dev when the "A resource failed to call xxx" message shows up
//        if (BuildConfig.DEBUG)
//            StrictMode.enableDefaults()
//        StrictMode.setVmPolicy(
//            StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
//                .detectLeakedClosableObjects()
//                .build()
//        )
        Log.i(logTag, "#################################################################")
        Log.i(logTag, "#########     Launching Pydia Client application      ###########")
        Log.i(logTag, "#################################################################")
        super.onCreate()
        instance = this

        val userAgent = updateClientData()
        Log.i(logTag, "... $userAgent")
        Log.i(logTag, "... Pre-init done - Timestamp: ${timestampForLogMessage()}")

        startKoin {// Launch dependency injection framework
            androidLogger(Level.INFO)
            androidContext(this@PydiaSimpleApp)
            workManagerFactory()
            modules(allModules)
        }
        configureWorkers()
        recordLaunch()
    }

    @Throws(SDKException::class)
    private fun updateClientData(): String {

        val packageInfo = internalGetPackageInfo()
        val instance = ClientData.getInstance()

        instance.packageID = packageName
        instance.name = resources.getString(R.string.app_name)
        instance.clientID = resources.getString(R.string.client_id)
        // this is the date when the app has been updated, not the timestamp of the current release
        instance.lastUpdateTime = packageInfo.lastUpdateTime
        // TODO also add a timestamp when releasing
        instance.version = packageInfo.versionName
        instance.versionCode = compatVersionCode(packageInfo)
        instance.platform = getAndroidVersion()
        ClientData.updateInstance(instance)

        return instance.userAgent()
    }

    private fun configureWorkers() {
        try {
            val workerService: WorkerService by inject()
            Log.i(logTag, "... Initialised workers: $workerService")
        } catch (e: Exception) {
            Log.e(logTag, "Could not configure workerService start: $e")
        }
    }

    private fun recordLaunch() {
        try {
            val jobService: JobService by inject()
            val creationMsg = "### Started ${ClientData.getInstance().userAgent()}"
            jobService.i(logTag, creationMsg, "Cells App")
        } catch (e: Exception) {
            Log.e(logTag, "could not log start: $e")
        }
    }

    private fun internalGetPackageInfo(): PackageInfo {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                applicationContext.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                applicationContext.packageManager.getPackageInfo(packageName, 0)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            throw SDKException("Could not retrieve PackageInfo for $packageName", e)
        }
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


    private fun setLogger() {
        Log.setLogger(CustomLogger())
    }
}

private class CustomLogger : Log.Logger {
    override fun v(tag: String, text: String) {
        android.util.Log.v(tag, text)
    }

    override fun d(tag: String, text: String) {
        android.util.Log.d(tag, text)
    }

    override fun w(tag: String, text: String) {
        android.util.Log.w(tag, text)
    }

    override fun i(tag: String, text: String) {
        android.util.Log.i(tag, text)
    }

    override fun e(tag: String, text: String) {
        android.util.Log.e(tag, text)
    }
}
