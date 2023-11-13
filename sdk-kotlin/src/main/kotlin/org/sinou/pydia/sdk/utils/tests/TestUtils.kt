package org.sinou.pydia.sdk.utils.tests

import org.sinou.pydia.sdk.api.Credentials
import org.sinou.pydia.sdk.api.ErrorCodes
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.ServerURL
import org.sinou.pydia.sdk.api.Transport
import org.sinou.pydia.sdk.transport.ServerFactory
import org.sinou.pydia.sdk.transport.ServerFactory.Companion.accountID
import org.sinou.pydia.sdk.transport.ServerURLImpl.Companion.fromAddress
import org.sinou.pydia.sdk.transport.auth.credentials.LegacyPasswordCredentials
import java.net.MalformedURLException
import java.util.Random

/**
 * Exposes various static methods to ease test implementation
 */
object TestUtils {

    const val parentPath = "sdk-tests"
    const val OS_MAC = "darwin"
    const val OS_LINUX = "linux"
    private const val SEED_CHARS = "abcdef1234567890"
    private val OS = System.getProperty("os.name").lowercase()

    /**
     * Create a new transport that is already logged in and ready to use.
     */
    @Throws(SDKException::class)
    fun getTransport(factory: ServerFactory, conf: RemoteServerConfig): Transport? {
        val sURL: ServerURL
        sURL = try {
            fromAddress(conf.serverURL!!, conf.skipVerify)
        } catch (mue: MalformedURLException) {
            throw SDKException(
                ErrorCodes.configuration_error,
                conf.serverURL + " is not a correct URL",
                mue
            )
        }
        val server = factory.registerServer(sURL)
        val credentials: Credentials = LegacyPasswordCredentials(conf.username!!, conf.pwd!!)
        factory.registerAccountCredentials(sURL, credentials)
        return factory.getTransport(accountID(conf.username, server))
    }

    /* Optimistic helper to get a unique string */
    fun randomString(length: Int): String {
        val sb = StringBuilder()
        val rand = Random()
        for (i in 0 until length) {
            sb.append(SEED_CHARS[rand.nextInt(SEED_CHARS.length)])
        }
        return sb.toString()
    }

    /* Optimistic helper to get a unique string */
    fun uniqueName(prefix: String): String {
        return "$prefix-${randomString(6)}"
    }

    val uniquePath: String
        get() = parentPath + "/" + randomString(6)
    val isWindows: Boolean
        get() = OS.contains("win")
    val isMac: Boolean
        get() = OS.contains("mac")
    val isLinux: Boolean
        get() = OS.contains("nix") || OS.contains("nux") || OS.contains("aix")

    fun getOS(): String {
        return if (isLinux) {
            OS_LINUX
        } else if (isMac) {
            OS_MAC
        } else throw RuntimeException("Unsupported OS")
    }
}