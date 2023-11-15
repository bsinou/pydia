package org.sinou.pydia.sdk.transport.auth.jwt

import com.google.gson.Gson
import org.sinou.pydia.sdk.api.SDKException
import java.lang.IllegalArgumentException

class OAuthConfig private constructor(
    val authorizeEndpoint: String,
    val tokenEndpoint: String,
    val revokeEndpoint: String,
) {

    val redirectURI: String = DEFAULT_REDIRECT_URI
    val scope: String = defaultScope

    var audience: String? = null
    var state: String? = null
    var code: String? = null

    companion object {
        const val OIDC_WELL_KNOWN_CONFIG_PATH = "/oidc/.well-known/openid-configuration"

        private const val DEFAULT_REDIRECT_URI = "cellsauth://callback"
        private const val AUTH_ENDPOINT = "authorization_endpoint"
        private const val TOKEN_ENDPOINT = "token_endpoint"
        private const val REVOCATION_ENDPOINT = "revocation_endpoint"
        private const val defaultScope = "openid email offline profile pydio"

        @Throws(SDKException::class)
        fun fromOIDCResponse(oidcStr: String): OAuthConfig {
            return try {
                fromJsonString(oidcStr)
            } catch (e: Exception) {
                throw SDKException("Could not parse OIDC response", e)
            }
        }

        private fun fromJsonString(oidcStr: String): OAuthConfig {

            val map: Map<String, Any> = (Gson().fromJson(oidcStr, Map::class.java) as? Map<String, Any>
                ?: throw IllegalArgumentException("Could not deserialize OIDC JSON String: $oidcStr"))

            var tmp = map[AUTH_ENDPOINT]
            val authEP = if ( tmp is String && tmp.isNotEmpty()) tmp
            else throw IllegalArgumentException("No authorisation endpoint, cannot register server")

            tmp = map[TOKEN_ENDPOINT]
            val tokenEP = if ( tmp is String && tmp.isNotEmpty()) tmp
            else throw IllegalArgumentException("No token endpoint, cannot register server")

            tmp = map[REVOCATION_ENDPOINT]
            val revocationEP = if ( tmp is String && tmp.isNotEmpty()) tmp
            else throw IllegalArgumentException("No revocation endpoint, cannot register server")

            return OAuthConfig(authEP, tokenEP, revocationEP)
        }
    }
}