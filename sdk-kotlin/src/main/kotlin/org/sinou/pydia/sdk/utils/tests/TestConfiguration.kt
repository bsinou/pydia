package org.sinou.pydia.sdk.utils.tests

import org.sinou.pydia.sdk.utils.Log
import java.io.File
import java.io.FilenameFilter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.Properties

/**
 * Simply retrieve test sessions from properties files that are in user-defined folders.
 * By default in the resources folder of the calling class.
 */
class TestConfiguration(localURL: URL) {

    private val accounts: MutableMap<String, RemoteServerConfig> = HashMap()

    init {
        try {
            val f = File(localURL.toURI())
            val filter = FilenameFilter { f1: File?, name: String -> name.endsWith(".properties") }
            for (currName in f.list(filter)) {
                loadOne(
                    currName.substring(0, currName.lastIndexOf('.')),
                    "$accountFolder/$currName"
                )
            }
        } catch (e: Exception) {
            Log.e("Initialisation", "Could not load server configuration at $accountFolder")
            e.printStackTrace()
        }
    }

    fun getServer(id: String): RemoteServerConfig? {
        return accounts[id]
    }

    val defaultServer: RemoteServerConfig?
        get() = accounts[defaultServerConfigId]
    val definedServers: Map<String, RemoteServerConfig>
        get() = accounts

    // public Path getWorkingDir() {
    //        return resourceDirPath;
    //    }
    /* Local helpers */
    private fun loadOne(id: String, path: String) {
        try {
            TestConfiguration::class.java.getResourceAsStream(path)
                .use { inputStream -> loadOne(id, inputStream) }
        } catch (e: IOException) {
            Log.e("Initialisation", "Could not retrieve configuration file, cause: ${e.message}")
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun loadOne(id: String, inputStream: InputStream) {
        val p = Properties()
        p.load(InputStreamReader(inputStream))
        if ("true" == p.getProperty("skipServer")) {
            return
        }
        accounts[id] = RemoteServerConfig(
            serverURL = p.getProperty("serverURL"),
            username = p.getProperty("username"),
            pwd = p.getProperty("pwd"),
            pat = p.getProperty("pat"),
            defaultWS = p.getProperty("defaultWorkspace"),
            skipVerify = "true" == p.getProperty("skipVerify"),
        )
    }

    companion object {
        // You must adapt these files to your setup in "src/test/resources"
        // to use the default configuration
        private const val defaultServerConfigId = "default"
        private const val accountFolder = "/accounts"
        val default: TestConfiguration
            get() {
                val url = TestConfiguration::class.java.getResource(accountFolder)
                return TestConfiguration(url)
            }
    }
}
