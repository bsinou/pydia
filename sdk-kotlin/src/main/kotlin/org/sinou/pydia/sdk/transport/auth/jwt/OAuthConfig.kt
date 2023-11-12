package org.sinou.pydia.sdk.transport.auth.jwt

import com.google.gson.Gson
import org.sinou.pydia.sdk.api.SDKException

class OAuthConfig private constructor() {
    // public final static String SCOPE = "scope";
    var authorizeEndpoint: String? = null
    var redirectURI: String
    var tokenEndpoint: String? = null
    var revokeEndpoint: String? = null

    //     public String refreshEndpoint;
    var audience: String? = null
    var scope: String
    var state: String? = null
    var code: String? = null

    init {
        redirectURI = DEFAULT_REDIRECT_URI
        scope = defaultScope
    } //    private static OAuthConfig fromJSON(JSONObject o) {

    //        OAuthConfig cfg = new OAuthConfig();
    //
    //        cfg.authorizeEndpoint = o.getString(AUTH_ENDPOINT);
    //        cfg.tokenEndpoint = o.getString(TOKEN_ENDPOINT);
    //        cfg.revokeEndpoint = o.getString(REVOCATION_ENDPOINT);
    //
    //        cfg.redirectURI = DEFAULT_REDIRECT_URI;
    //
    //        cfg.scope = defaultScope;
    //        return cfg;
    //    }
    companion object {
        private const val defaultScope = "openid email offline profile pydio"
        const val DEFAULT_REDIRECT_URI = "cellsauth://callback"
        const val OIDC_WELL_KNOWN_CONFIG_PATH = "/oidc/.well-known/openid-configuration"
        const val AUTH_ENDPOINT = "authorization_endpoint"
        const val TOKEN_ENDPOINT = "token_endpoint"
        const val REVOCATION_ENDPOINT = "revocation_endpoint"
        @Throws(SDKException::class)
        fun fromOIDCResponse(oidcStr: String): OAuthConfig {
            return try {
                fromJsonString(oidcStr)
            } catch (e: Exception) {
                throw SDKException("Could not parse OIDC response", e)
            }
        }

        private fun fromJsonString(oidcStr: String): OAuthConfig {
            val cfg = OAuthConfig()
            val gson = Gson()
            val map: Map<String, Any> = gson.fromJson(oidcStr, Map::class.java) as Map<String, Any>
            cfg.authorizeEndpoint = map[AUTH_ENDPOINT] as String?
            cfg.tokenEndpoint = map[TOKEN_ENDPOINT] as String?
            cfg.revokeEndpoint = map[REVOCATION_ENDPOINT] as String?
            return cfg
        }
    }
}