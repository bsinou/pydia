package org.sinou.pydia.sdk.transport

import com.google.gson.Gson
import org.sinou.pydia.sdk.api.ErrorCodes
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.Server
import org.sinou.pydia.sdk.api.ServerURL
import org.sinou.pydia.sdk.transport.auth.jwt.OAuthConfig
import org.sinou.pydia.sdk.utils.IoHelpers
import org.sinou.pydia.sdk.utils.Log
import org.sinou.pydia.sdk.utils.Str
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets

class CellsServer(override val serverURL: ServerURL) : Server {
    override var oAuthConfig: OAuthConfig? = null
        private set
    private var title: String? = null
    override var welcomeMessage: String? = null
        private set

    override var iconURL: String? = null
        private set
        get() = field

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

    //    override val remoteType: String?
//        get() = TODO("Not yet implemented")
//
//    override val isLegacy: Boolean
//        get() = false
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
        return newURL(path).openConnection()
    }

    @Throws(SDKException::class)
    private fun downloadBootConf() {
        var con: HttpURLConnection? = null
        var input: InputStream? = null
        val out = ByteArrayOutputStream()
        try {
            con = openAnonConnection(BOOTCONF_PATH)
            input = con.inputStream
            IoHelpers.pipeRead(input, out)
            val jsonString = out.toString(StandardCharsets.UTF_8)
            val gson = Gson()
            val map: Map<*, *> = gson.fromJson(jsonString, MutableMap::class.java)
            if (map.containsKey("customWording")) {
                val customWordings = map["customWording"] as Map<String, Any>?
                title = customWordings!!["title"] as String?
                iconURL = customWordings["icon"] as String?
                if (customWordings.containsKey("welcomeMessage")) {
                    welcomeMessage = customWordings["welcomeMessage"] as String?
                }
            }
            if (map.containsKey("ajxpVersion")) {
                version = map["ajxpVersion"] as String?
            }

            // FIXME this is broken. Find where it is used and fix if necessary.
            hasLicenceFeatures = map.containsKey("license_features")

            // TODO factorize this and then remove the JSON old library
            if (map.containsKey("other")) {
                var oo = map["other"]
                if (oo is Map<*, *>) {
                    val other = oo as Map<String, Any>
                    if (other.containsKey("vanity")) {
                        oo = other["vanity"]
                        if (oo is Map<*, *>) {
                            val vanity = oo as Map<String, Any>
                            if (vanity.containsKey("palette")) {
                                oo = vanity["palette"]
                                if (oo is Map<*, *>) {
                                    val palette = oo as Map<String, Any>
                                    if (palette.containsKey("primary1Color")) {
                                        oo = palette["primary1Color"]
                                        if (oo is String && Str.notEmpty(oo as String?)) {
                                            // Log.d(logTag, "Found a color: " + oo);
                                            customPrimaryColor = oo
                                        }
                                    }
                                }
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
            IoHelpers.closeQuietly(out)
        }
    }

    @Throws(SDKException::class)
    private fun downloadOIDCConfiguration() {
        var con: HttpURLConnection? = null
        var input: InputStream? = null
        val out = ByteArrayOutputStream()
        try {
            val oidcURL = newURL(OAuthConfig.OIDC_WELL_KNOWN_CONFIG_PATH)
            con = oidcURL.openConnection()
            con.requestMethod = "GET"
            input = con.inputStream
            IoHelpers.pipeRead(input, out)
            val oidcStr = out.toString(StandardCharsets.UTF_8)
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
            IoHelpers.closeQuietly(out)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return if (other !is CellsServer) false else url() == other.url()
    }

    companion object {
        const val logTag = "CellsServer"
        const val API_PREFIX = "/a"
        const val BOOTCONF_PATH = "$API_PREFIX/frontend/bootconf"
    }
}