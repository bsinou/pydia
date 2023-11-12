package org.sinou.pydia.sdk.transport

import okhttp3.OkHttpClient
import org.sinou.pydia.openapi.api.FrontendServiceApi
import org.sinou.pydia.openapi.infrastructure.ApiClient
import org.sinou.pydia.openapi.model.RestFrontSessionRequest
import org.sinou.pydia.openapi.model.RestFrontSessionResponse
import org.sinou.pydia.sdk.api.CustomEncoder
import org.sinou.pydia.sdk.api.ErrorCodes
import org.sinou.pydia.sdk.api.ICellsTransport
import org.sinou.pydia.sdk.api.PasswordCredentials
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.SdkNames
import org.sinou.pydia.sdk.api.Server
import org.sinou.pydia.sdk.api.ServerURL
import org.sinou.pydia.sdk.api.Transport
import org.sinou.pydia.sdk.transport.auth.CellsOAuthInterceptor
import org.sinou.pydia.sdk.transport.auth.CredentialService
import org.sinou.pydia.sdk.transport.auth.Token
import org.sinou.pydia.sdk.transport.auth.jwt.OAuthConfig
import org.sinou.pydia.sdk.utils.IoHelpers
import org.sinou.pydia.sdk.utils.Log
import org.sinou.pydia.sdk.utils.Str
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URI
import java.nio.charset.StandardCharsets
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

class CellsTransport(
    credentialService: CredentialService?,
    username: String,
    server: Server,
    encoder: CustomEncoder
) : ICellsTransport, SdkNames {

    private val encoder: CustomEncoder
    private val timeFormatter: SimpleDateFormat = SimpleDateFormat("HH:mm")
    private val credentialService: CredentialService?

    // TODO rather rely on a state ID
    override val username: String
    override val server: Server
    override var userAgent: String? = null
        get() {
            if (field != null) {
                return field
            }
            field = ClientData.getInstance()!!.userAgent()
            Log.i(logTag, "New Transport instance for $username, user agent is: $field")
            return field
        }
        private set

    init {
        this.credentialService = credentialService
        this.username = username
        this.server = server
        this.encoder = encoder
    }

    override val id: String
        get() = stateID.id
    override val stateID: StateID
        get() = StateID(username, server.serverURL.id)
    override val isOffline: Boolean
        get() {
            Log.w(logTag, "Hard-coded non relevant offline check from ")
            Thread.dumpStack()
            return false
        }

    @Throws(SDKException::class)
    override fun getServerRegistryAsNonAuthenticatedUser(): InputStream? {
        try {
            val con = openAnonymousApiConnection(registryPath)
            con.requestMethod = "GET"
            return con.inputStream
        } catch (e: IOException) {
            throw SDKException.conFailed("cannot get registry as anonymous", e)
        }
    }

    @Throws(SDKException::class)
    override fun getServerRegistryAsAuthenticatedUser(): InputStream? {
        try {
            val con = openApiConnection(registryPath)
            con.requestMethod = "GET"
            return con.inputStream
        } catch (e: IOException) {
            e.printStackTrace()
            throw SDKException.conFailed("cannot get registry as authenticated user", e)
        }
    }


    @get:Throws(SDKException::class)
    val accessToken: String?
        get() {
            val token = checkedToken ?: return null
            return token.value
        }

//    @get:Throws(SDKException::class)
//    val token: Token?
//        get() = credentialService?.get(id)@get:Throws(SDKException::class)

    fun getToken(): Token? {
        return credentialService?.get(id) ?: run { null }
    }

    @Throws(SDKException::class)
    fun requestTokenRefresh() {
        credentialService?.requestRefreshToken(stateID)
    }

    override fun getUserData(binary: String?): InputStream? {
        // FIXME  implement
        return null
    }

    @Throws(SDKException::class)
    override fun withAuth(con: HttpURLConnection): HttpURLConnection {
        con.setRequestProperty("Authorization", "Bearer $accessToken")
        return con
    }

    override fun withUserAgent(con: HttpURLConnection): HttpURLConnection {
        con.setRequestProperty("User-Agent", userAgent)
        return con
    }

    @Throws(SDKException::class, IOException::class)
    override fun openConnection(path: String): HttpURLConnection {
        return withAuth(withUserAgent(server.newURL(path).openConnection()))
    }

    @Throws(IOException::class)
    override fun openAnonConnection(path: String): HttpURLConnection {
        return withUserAgent(server.newURL(path).openConnection())
    }

    @Throws(SDKException::class, IOException::class)
    fun openApiConnection(path: String): HttpURLConnection {
        return withAuth(
            withUserAgent(
                server.newURL(CellsServer.API_PREFIX + path).openConnection()
            )
        )
    }

    @Throws(IOException::class)
    fun openAnonymousApiConnection(path: String): HttpURLConnection {
        return withUserAgent(server.newURL(CellsServer.API_PREFIX + path).openConnection())
    }

    @Throws(SDKException::class)
    fun authenticatedClient(): ApiClient {
        val apiClient: ApiClient = apiClient
        val accessToken = accessToken
        if (accessToken.isNullOrEmpty()) {
            throw SDKException(ErrorCodes.no_token_available)
        }
        // apiClient.addDefaultHeader("Authorization", "Bearer $accessToken")
        return apiClient
    }

    val apiClient: ApiClient
        get() {
//            var apiClient = ApiClient()
//            if (server.isSSLUnverified) {
//                apiClient = apiClient.setVerifyingSsl(false)
//            }
//            apiClient.setBasePath(getApiURL(server.serverURL))
//            apiClient.setUserAgent(userAgent)
//            return apiClient
            // TODO configure default okhttp client

            val builder = OkHttpClient.Builder().addInterceptor(
                CellsOAuthInterceptor { this.getToken()?.idToken })
            // TODO handle skip verify
//            if (server.isSSLUnverified) {
//                apiClient = apiClient.setVerifyingSsl(false)
//            }
            // TODO handle user agent
            // apiClient.setUserAgent(userAgent)

            val apiClient = ApiClient(getApiURL(server.serverURL), builder.build())
            return apiClient
        }

    @Throws(Exception::class)
    fun tryDownloadingBootConf() {
        var con: HttpURLConnection? = null
        var `in`: InputStream? = null
        val out = ByteArrayOutputStream()
        try {
            con = openConnection(CellsServer.BOOTCONF_PATH)
            `in` = con.inputStream
            IoHelpers.pipeRead(`in`, out)
        } finally {
            IoHelpers.closeQuietly(con)
            IoHelpers.closeQuietly(`in`)
            IoHelpers.closeQuietly(out)
        }
    }

    @Throws(SDKException::class)
    override fun getTokenFromLegacyCredentials(credentials: PasswordCredentials): Token {
        val authInfo: MutableMap<String, String> = HashMap()
        authInfo["login"] = credentials.username
        authInfo["password"] = credentials.password
        authInfo["type"] = "credentials"
        val cd = ClientData.getInstance()
        authInfo["client_id"] = cd!!.clientId
        if (Str.notEmpty(cd.clientSecret)) {
            // This additional header is only used for "private" clients and not used with
            // default standard clients that have no client secret
            val authHeader = ("Basic "
                    + encoder.base64Encode(cd.clientId + ":" + cd.clientSecret))
            authInfo["Authorization"] = authHeader
        }
        val request = RestFrontSessionRequest(
            authInfo = authInfo,
            clientTime = System.currentTimeMillis().toInt(),
        )
        val api = FrontendServiceApi()
        val response: RestFrontSessionResponse
        return try {
            response = api.frontSession(request)
            val t = Token()
            t.subject = ServerFactory.accountID(credentials.username, server)
            t.value = response.JWT
            val expireTime: Int? = response.expireTime
            expireTime?.let {
                t.setExpiry(expireTime.toLong())
            }
            t
        } catch (e: Exception) { // TODO was ApiException
            throw SDKException(
                ErrorCodes.no_token_available,
                IOException("login or password incorrect")
            )
        }
    }

    // TODO better management of exceptions.
    @Throws(Exception::class)
    fun getTokenFromCode(code: String, encoder: CustomEncoder?): Token {
        var input: InputStream? = null
        var out: ByteArrayOutputStream? = null
        val cfg: OAuthConfig = server.oAuthConfig
            ?: throw IllegalStateException("No OAuth config is defined for ${server.serverURL.id}")

        return try {
            Log.i(logTag, "Retrieving token from OAuth2 code")
            val endpointURI = URI.create(cfg.tokenEndpoint)
            val con = openAnonConnection(endpointURI.path)

            // Manage Body as URL encoded form
            val authData: MutableMap<String, String> = HashMap()
            authData["grant_type"] = "authorization_code"
            authData["code"] = code
            authData["redirect_uri"] = cfg.redirectURI
            val cd = ClientData.getInstance()
            authData["client_id"] = cd!!.clientId
            if (Str.notEmpty(cd.clientSecret)) {
                authData["client_secret"] = cd.clientSecret
            }
            // String authHeader = "Basic " + encoder.base64Encode(cd.getClientId() + ":" + cd.getClientSecret());
            addPostData(con, authData, null)
            input = try { // Real call
                // TODO double check: do we need to explicitly open the connection before getting the stream ?
                con.connect()
                con.inputStream
            } catch (ioe: IOException) {
                throw IOException("Unable to open connection to $endpointURI", ioe)
            }
            out = ByteArrayOutputStream()
            IoHelpers.pipeRead(input, out)
            val jwtStr = out.toString(StandardCharsets.UTF_8)
            Token.decodeOAuthJWT(jwtStr)
        } finally {
            IoHelpers.closeQuietly(input)
            IoHelpers.closeQuietly(out)
        }
    }

    @Throws(SDKException::class)
    fun getRefreshedOAuthToken(refreshToken: String): Token {
        var input: InputStream? = null
        var out: ByteArrayOutputStream? = null
        val cfg: OAuthConfig = server.oAuthConfig
            ?: throw IllegalStateException("No OAuth config is defined for ${server.serverURL.id}")

        return try {
            Log.d(
                logTag,
                String.format("Launching refresh token flow for %s@%s", username, server.url())
            )
            val endpointURI = URI.create(cfg.tokenEndpoint)
            val con = openAnonConnection(endpointURI.path)
            val authData: MutableMap<String, String> = HashMap()
            authData["grant_type"] = "refresh_token"
            authData["refresh_token"] = refreshToken
            val cd = ClientData.getInstance()
            authData["client_id"] = cd?.clientId ?: ""
            if (Str.notEmpty(cd!!.clientSecret)) {
                authData["client_secret"] = cd.clientSecret
            }
            addPostData(con, authData, null)
            input = con.inputStream
            out = ByteArrayOutputStream()
            IoHelpers.pipeRead(input, out)
            val jwtStr = out.toString(StandardCharsets.UTF_8)
            val newToken = Token.decodeOAuthJWT(jwtStr)
            Log.i(
                logTag, String.format(
                    "Retrieved a refreshed token for %s@%s. New expiration time %s",
                    username,
                    server.url(),
                    timeFormatter.format(Date(newToken.expirationTime * 1000L))
                )
            )
            newToken
        } catch (e: ParseException) {
            Log.e(logTag, "Could not parse refreshed token. " + e.localizedMessage)
            throw SDKException(
                ErrorCodes.no_token_available,
                IOException("could not decode server response")
            )
        } catch (e: IOException) {
            if (e is FileNotFoundException) {
                throw SDKException(
                    ErrorCodes.refresh_token_not_valid,
                    "FNFE while trying to refresh. It usually means that the refresh token has already been consumed",
                    e
                )
            } else {
                throw SDKException.RemoteIOException("Token request failed: " + e.localizedMessage)
            }
        } finally {
            IoHelpers.closeQuietly(input)
            IoHelpers.closeQuietly(out)
        }
    }

    @get:Throws(SDKException::class)
    private val checkedToken: Token?
        get() {
            if (credentialService == null) {
                return null
            }
            val token: Token? = credentialService[id]
            // We check token validity and try to refresh / get it if necessary
            if (token == null) {
                return null
            } else if (token.isExpired) {
                // Initiate a refresh token request if non is already running, but do not retrieve a new valid token here.
                credentialService.requestRefreshToken(stateID)
                throw SDKException(
                    ErrorCodes.token_expired,
                    "Token expired for $stateID, refresh has been requested"
                )
            }
            return token
        }

    /* Sets body parameters as URL encoded form */
    @Throws(SDKException::class)
    private fun addPostData(
        con: HttpURLConnection,
        postData: Map<String, String>,
        authHeader: String?
    ) {
        var postOut: OutputStream? = null
        try {
            con.requestMethod = "POST"
            con.doOutput = true
            con.setRequestProperty(
                "content-type",
                "application/x-www-form-urlencoded; charset=utf-8"
            )
            if (Str.notEmpty(authHeader)) {
                con.setRequestProperty("Authorization", authHeader)
            }
            val builder = StringBuilder()
            for ((key, value) in postData) {
                if (builder.isNotEmpty()) builder.append('&')
                builder.append(encoder.utf8Encode(key))
                builder.append('=')
                builder.append(encoder.utf8Encode(value))
            }
            val postDataBytes: ByteArray = encoder.getUTF8Bytes(builder.toString())
            con.setRequestProperty("content-length", postDataBytes.size.toString())
            postOut = con.outputStream
            postOut.write(postDataBytes)
        } catch (e: IOException) {
            throw SDKException(ErrorCodes.bad_config, e)
        } finally {
            IoHelpers.closeQuietly(postOut)
        }
    }

    fun getApiURL(): String {
        return getApiURL(server.serverURL)
    }

    private fun getApiURL(serverURL: ServerURL): String {
        return try {
            serverURL.withPath(CellsServer.API_PREFIX).url.toString()
        } catch (e: MalformedURLException) {
            throw RuntimeException("Getting API URL for " + serverURL.id, e)
        }
    }

    @Suppress("unused")
    private fun debugConnection(con: HttpURLConnection) {
        println("... Debugging " + con.requestMethod + " connection: ")
        val hfs = con.headerFields
        for (currHeader in hfs.keys) {
            println("--- $currHeader")
            var ii = 0
            for (`val` in hfs[currHeader]!!) {
                println("#" + ++ii + ": " + `val`)
            }
        }
        val headers = con.requestProperties
        for (currHeader in headers.keys) {
            println("--- $currHeader")
            var ii = 0
            for (`val` in headers[currHeader]!!) {
                println("#" + ++ii + ": " + `val`)
            }
        }
    }

    companion object {
        private const val logTag = "CellsTransport"
        private const val registryPath = "/frontend/state"
        fun asAnonymous(server: Server, encoder: CustomEncoder): CellsTransport {
            return CellsTransport(null, Transport.ANONYMOUS_USERNAME, server, encoder)
        }
    }
}