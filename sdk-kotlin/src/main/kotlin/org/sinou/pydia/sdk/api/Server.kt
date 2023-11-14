package org.sinou.pydia.sdk.api

import org.sinou.pydia.sdk.transport.auth.jwt.OAuthConfig
import java.net.MalformedURLException

interface Server {
    /**
     * Forces initialisation of the local Server object to typically refresh boot configuration
     */
    @Throws(SDKException::class)
    fun init(): Server

    /**
     * Re-runs initialisation if necessary or forced
     */
    @Throws(SDKException::class)
    fun refresh(force: Boolean): Server?

    /**
     * Returns the convenient `ServerURL` already configured to communicate with the server
     */
    val serverURL: ServerURL

    /**
     * Returns a ready to use `ServerURL` with the passed trailing path.
     */
    @Throws(MalformedURLException::class)
    fun newURL(path: String): ServerURL {
        return serverURL.withPath(path)
    }

    /**
     * Returns the canonical URL of the server as String for various persistence processes.
     * This should not be used to create another URL object to try to connect to the server
     * or management of self-signed URLs will be skipped.
     */
    fun url(): String {
        return serverURL.id
    }

//    val remoteType: String?
//    val isLegacy: Boolean

    val oAuthConfig: OAuthConfig?

    val isSSLUnverified: Boolean
        get() = serverURL.skipVerify()
    val label: String?
    val welcomeMessage: String?
    val versionName: String?
    fun hasLicenseFeatures(): Boolean
    val customPrimaryColor: String?
    val iconURL: String?

}
