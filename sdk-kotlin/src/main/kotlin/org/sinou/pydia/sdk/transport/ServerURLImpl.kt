package org.sinou.pydia.sdk.transport

import com.google.gson.Gson
import org.sinou.pydia.sdk.api.ErrorCodes
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.ServerURL
import org.sinou.pydia.sdk.client.security.CertificateTrust
import org.sinou.pydia.sdk.client.security.CertificateTrustManager
import org.sinou.pydia.sdk.utils.Log
import org.sinou.pydia.sdk.utils.Str
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.security.KeyManagementException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Arrays
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class ServerURLImpl private constructor(
    override val url: URL, // Self-signed servers management
    private val skipVerify: Boolean
) : ServerURL {

    override val certificateChain: Array<ByteArray>? = null
    private var sslContext: SSLContext? = null
    private var trustHelper: CertificateTrust.Helper? = null
        private get() = if (field == null) {
            object : CertificateTrust.Helper {
                override fun isServerTrusted(chain: Array<X509Certificate>): Boolean {
                    for (c in chain) {
                        certificateChain?.let {
                            for (trusted in it) {
                                try {
                                    c.checkValidity()
                                    val hash = MessageDigest.getInstance("MD5")
                                    val c1 = hash.digest(trusted)
                                    val c2 = hash.digest(c.encoded)
                                    if (Arrays.equals(c1, c2)) {
                                        return true
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                    return false
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }.also { field = it }
        } else field

    private var sslSocketFactory: SSLSocketFactory? = null

    @Throws(IOException::class)
    override fun openConnection(): HttpURLConnection {
        return if ("http" == url.protocol) {
            url.openConnection() as HttpURLConnection
        } else if ("https" == url.protocol) {
            val conn = url.openConnection() as HttpsURLConnection
            if (skipVerify) {
                setAcceptAllVerifier(conn as HttpsURLConnection?)
            }
            conn
        } else throw IllegalStateException("Unsupperted protocol: ${url.protocol}")
    }

    @Throws(MalformedURLException::class)
    override fun withPath(path: String): ServerURL {
        val specBuilder = StringBuilder()
        if (Str.notEmpty(url.path)) {
            specBuilder.append(url.path)
        }
        val verifyPassedURL = URL(url, path)
        specBuilder.append(verifyPassedURL.path)
        if (Str.notEmpty(url.query)) {
            specBuilder.append("?").append(url.query)
        }
        return ServerURLImpl(URL(url, specBuilder.toString()), skipVerify)
    }

    @Throws(MalformedURLException::class)
    override fun withQuery(query: String): ServerURL {
        if (Str.empty(query)) {
            return this
        }
        var spec = "/"
        if (Str.notEmpty(url.path)) {
            spec = url.path
        }
        spec = "$spec?$query"
        return ServerURLImpl(URL(url, spec), skipVerify)
    }

    @Throws(MalformedURLException::class)
    override fun withSpec(spec: String): ServerURL {
        val specBuilder = StringBuilder()
        if (Str.notEmpty(url.path)) {
            specBuilder.append(url.path)
        }
        val verifyPassedURL = URL(url, spec)
        if (Str.notEmpty(verifyPassedURL.path)) {
            specBuilder.append(verifyPassedURL.path)
        }
        if (Str.notEmpty(verifyPassedURL.query)) {
            specBuilder.append("?").append(verifyPassedURL.query)
        }
        return ServerURLImpl(URL(url, specBuilder.toString()), skipVerify)
    }

    override fun toJson(): String? {
        return Gson().toJson(this)
    }

    // TODO finalize self-signed management.
    override fun skipVerify(): Boolean {
        return skipVerify
    }

    /* Manage self signed on a URL by URL Basis.
      Thanks to https://stackoverflow.com/questions/19723415/java-overriding-function-to-disable-ssl-certificate-check */
    @Throws(SDKException::class, IOException::class)
    override fun ping() {
        val connection = openConnection()
        // 10 secs timeout instead of the default unlimited
        connection.connectTimeout = 10000
        try {
            connection.requestMethod = "GET"
            val code = connection.responseCode
            if (code != 200) {
                throw SDKException(
                    code,
                    "Could not reach " + url.host + ": " + connection.responseMessage
                )
            }
        } catch (uhe: UnknownHostException) {
            throw SDKException(ErrorCodes.con_failed, uhe.message, uhe)
        } catch (ste: SocketTimeoutException) {
            throw SDKException(
                ErrorCodes.con_failed,
                url.host + " - server unreachable, timeout: " + ste.message,
                ste
            )
        } catch (pe: ProtocolException) {
            // This might typically be thrown by the underlying OKHttp library when a redirect cycle has been detected
            throw SDKException("Could not reach " + url.host + ": " + pe.message, pe)
            //        } catch (Exception e) {
//            Log.e(logTag, "--- unexpected error while pinging server at " + url.getHost());
//            e.printStackTrace();
//            Log.e(logTag, "--- End of stack trace");
//            throw e;
        } finally {
            try {
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(logTag, "Cannot disconnect connection after ping, swallowed error:")
                e.printStackTrace()
            }
        }
    }

    override val sSLContext: SSLContext?
        get() {
            if (sslContext == null) {
                try {
                    var tmpContext = SSLContext.getInstance("TLS")
                    tmpContext.init(null, arrayOf(trustManager()), null)
                    tmpContext.getSocketFactory()
                    sslContext = tmpContext
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }

//        try {
//            sslContext.getSocketFactory();
//        } catch (Exception e) {
//            try {
//                sslContext = SSLContext.getInstance("TLS");
//                sslContext.init(null, new TrustManager[]{trustManager()}, null);
//            } catch (Exception ex) {
//                e.printStackTrace();
//                return null;
//            }
//        }
            return sslContext
        }

    // TODO tweak until we rework the self signed.
    override fun getSslSocketFactory(): SSLSocketFactory? {
        return try {
            if (sslSocketFactory == null) {
                val sc = SSLContext.getInstance("SSL")
                sc.init(null, SKIP_VERIFY_TRUST_MANAGER, SecureRandom())
                sslSocketFactory = sc.socketFactory
            }
            sslSocketFactory
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Unexpected error while initializing SSL context", e)
        } catch (e: KeyManagementException) {
            throw RuntimeException("Unexpected error while initializing SSL context", e)
        }
    }

    private fun setAcceptAllVerifier(connection: HttpsURLConnection?) {
        try {
            // Create the socket factory.
            // Reusing the same socket factory allows sockets to be reused, supporting persistent connections.
            if (null == sslSocketFactory) {
                val sc = SSLContext.getInstance("SSL")
                sc.init(null, SKIP_VERIFY_TRUST_MANAGER, SecureRandom())
                sslSocketFactory = sc.socketFactory
            }
            connection!!.sslSocketFactory = sslSocketFactory

            // Since we may be using a cert with a different name, we need to ignore the hostname as well.
            connection.hostnameVerifier = SKIP_HOSTNAME_VERIFIER
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Unexpected error while initializing SSL context", e)
        } catch (e: KeyManagementException) {
            throw RuntimeException("Unexpected error while initializing SSL context", e)
        }
    }

    private fun trustManager(): TrustManager {
        return CertificateTrustManager(trustHelper)
    }

    companion object {
        private const val logTag = "ServerURLImpl"
        val SKIP_VERIFY_TRUST_MANAGER = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }

                override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
            }
        )
        private val SKIP_HOSTNAME_VERIFIER =
            HostnameVerifier { hostname: String?, session: SSLSession? -> true }

        @Throws(MalformedURLException::class)
        fun fromAddress(urlString: String, skipVerify: Boolean = false): ServerURL {
            var urlString = urlString
            urlString = urlString.trim { it <= ' ' }.lowercase()
            var url = URL(urlString)
//            when (url.path) {
//                "/", "" -> url = URL(url.protocol + "://" + url.authority)
//                else -> {
//                    // This works for P8 only. We do not support Cells server on a sub-path of a domain.
//                    var path = url.path.trim { it <= ' ' }
//                    if (path.endsWith("/")) {
//                        path = path.substring(0, path.length - 1)
//                    }
//
//                    // Double check the protocol to avoid NPE when handling old servers migration
//                    val protocol = if (url.protocol == null) "https" else url.protocol
//                    val authority = url.authority
//                        ?: throw MalformedURLException("Cannot create a server URL without authority for $urlString")
//                    url = URL(protocol + "://" + url.authority + path)
//                }
//            }
            return ServerURLImpl(url, skipVerify)
        }

        fun fromJson(jsonString: String): ServerURL {
            return try {
                // TODO Dirty tweak until we finalize implementation of self-signed certificates
                // We Assume the passed json is correctly formatted
                // Type type = new TypeToken<Map<String, Object>>() {
                // }.getType();
                // Map<String, Object> props = new Gson().fromJson(jsonString, type);           
                val propType: Map<String, Any> = HashMap()
                val props = Gson().fromJson(jsonString, propType.javaClass)
                fromAddress(
                    props["url"].toString(), java.lang.Boolean.parseBoolean(
                        props["skipVerify"].toString()
                    )
                )
            } catch (e: MalformedURLException) {
                throw RuntimeException("Unable to decode JSON string: $jsonString", e)
            } catch (e: Exception) {
                Log.e(logTag, "Crashing due to proguard with gson..... " + e.message)
                e.printStackTrace()
                throw RuntimeException("Unable to create JSON object: $jsonString", e)
            }
        }
    }
}
