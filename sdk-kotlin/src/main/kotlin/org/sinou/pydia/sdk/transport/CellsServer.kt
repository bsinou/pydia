package org.sinou.pydia.sdk.transport

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.sinou.pydia.sdk.api.ErrorCodes
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.Server
import org.sinou.pydia.sdk.api.ServerURL
import org.sinou.pydia.sdk.transport.auth.jwt.OAuthConfig
import org.sinou.pydia.sdk.utils.IoHelpers
import org.sinou.pydia.sdk.utils.Log
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection

class CellsServer(override val serverURL: ServerURL) : Server {
    override var oAuthConfig: OAuthConfig? = null
        private set
    private var title: String? = null
    override var welcomeMessage: String? = null
        private set

    override var iconURL: String? = null
        private set

    private var version: String? = null
    private var hasLicenceFeatures = false
    override var customPrimaryColor: String? = null
        private set

    @Throws(SDKException::class)
    override fun init(): Server {
        return refresh(true)
    }

    @Throws(SDKException::class)
    override fun refresh(force: Boolean): Server {
        if (force || version == null) {
            downloadBootConf()
            downloadOIDCConfiguration()
        }
        return this
    }

    override val label: String?
        get() = if (title != null && "" != title) {
            title
        } else url()
    override val versionName: String
        get() = version
            ?: throw RuntimeException("Trying to retrieve AJXP Version param before the server has been instantiated")

    override fun hasLicenseFeatures(): Boolean {
        return hasLicenceFeatures
    }

    @Throws(IOException::class)
    fun openAnonConnection(path: String): HttpURLConnection {
        val mUrl = newURL(path)
        return mUrl.openConnection()
    }

    @Throws(SDKException::class)
    private fun downloadBootConf() {
        var con: HttpURLConnection? = null
        var input: InputStream? = null
        try {
            con = openAnonConnection(BOOTCONF_PATH)
            input = con.inputStream
            val jsonString = IoHelpers.readToString(input)

            val type = object : TypeToken<Map<String, Any>>() {}.type
            val map: Map<String, Any> = Gson().fromJson(jsonString, type)

            val customWording = map["customWording"] as? Map<*, *>
            customWording?.let {
                title = it["title"] as? String ?: title
                iconURL = it["icon"] as? String ?: iconURL
                welcomeMessage = it["welcomeMessage"] as? String ?: welcomeMessage
            }

            version = map["ajxpVersion"] as? String ?: version

            // FIXME this is broken. Find where it is used and fix if necessary.
            hasLicenceFeatures = map.containsKey("license_features")

            map["other"]?.let { other ->
                (other as? Map<*, *>)?.get("vanity")?.let { vanity ->
                    (vanity as? Map<*, *>)?.get("palette")?.let { palette ->
                        (palette as? Map<*, *>)?.get("primary1Color")?.let { color ->
                            if (color is String && color.isNotEmpty()) {
                                // Log.d(logTag, "Found a color: $color")
                                customPrimaryColor = color
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw SDKException(
                ErrorCodes.unexpected_content,
                "Could not get boot configuration at " + url(),
                e
            )
        } finally {
            IoHelpers.closeQuietly(con)
            IoHelpers.closeQuietly(input)
        }
    }

    @Throws(SDKException::class)
    private fun downloadOIDCConfiguration() {
        var con: HttpURLConnection? = null
        var input: InputStream? = null
        try {
            val oidcURL = newURL(OAuthConfig.OIDC_WELL_KNOWN_CONFIG_PATH)
            con = oidcURL.openConnection()
            con.requestMethod = "GET"
            input = con.inputStream
            val oidcStr = IoHelpers.readToString(input)
            oAuthConfig = OAuthConfig.fromOIDCResponse(oidcStr)
        } catch (e: FileNotFoundException) {
            Log.e(logTag, "Cannot retrieve OIDC configuration at " + e.message)
            e.printStackTrace()
            throw SDKException(
                ErrorCodes.server_configuration_issue,
                "Cannot retrieve OIDC well known file for " + serverURL.url + ", please check your server config",
                e
            )
        } catch (e: Exception) {
            Log.e(
                logTag, "Unexpected error while retrieving OIDC configuration"
                        + ", cause: " + e.message
            )
            e.printStackTrace()
            throw SDKException.unexpectedContent(e)
        } finally {
            IoHelpers.closeQuietly(con)
            IoHelpers.closeQuietly(input)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return if (other !is CellsServer) false else url() == other.url()
    }

    override fun hashCode(): Int {
        return url().hashCode()
    }

    companion object {
        const val logTag = "CellsServer"
        const val API_PREFIX = "/a"
        const val BOOTCONF_PATH = "$API_PREFIX/frontend/bootconf"
    }
}
