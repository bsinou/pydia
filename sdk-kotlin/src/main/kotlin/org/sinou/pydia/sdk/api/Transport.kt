package org.sinou.pydia.sdk.api

import org.sinou.pydia.sdk.transport.StateID
import org.sinou.pydia.sdk.transport.auth.Token
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection

interface Transport {

    val stateID: StateID
    val id: String
    val username: String?
    val server: Server?
    val userAgent: String?

    /**
     * Tries to login the server with the passed credentials
     * and stores the resulting token in the local token store.
     *
     * @return
     */
    // Token unlock(Credentials c) throws SDKException;
    @Throws(SDKException::class)
    fun getTokenFromLegacyCredentials(credentials: PasswordCredentials): Token

    //
    //    void logout() throws SDKException;
    val isOffline: Boolean

    @Throws(SDKException::class, IOException::class)
    fun openConnection(path: String): HttpURLConnection

    @Throws(SDKException::class, IOException::class)
    fun openAnonConnection(path: String): HttpURLConnection

    @Deprecated("")
    fun withUserAgent(con: HttpURLConnection): HttpURLConnection

    //    JSONObject userInfo() throws SDKException;
    @Throws(SDKException::class)
    fun getUserData(binary: String?): InputStream?

    @Throws(SDKException::class)
    fun getServerRegistryAsNonAuthenticatedUser(): InputStream?

    @Throws(SDKException::class)
    fun getServerRegistryAsAuthenticatedUser(): InputStream? // InputStream getWorkspaceRegistry(String ws) throws SDKException;

    companion object {
        const val ANONYMOUS_USERNAME = "anon"
        const val UNDEFINED_URL = "https://example.com"

        @Deprecated("")
        val UNDEFINED_STATE = "https%3A%2F%2Fexample.com"
    }
}