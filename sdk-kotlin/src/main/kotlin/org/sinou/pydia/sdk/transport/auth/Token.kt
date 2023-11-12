package org.sinou.pydia.sdk.transport.auth

import com.google.gson.Gson
import org.json.JSONObject
import org.sinou.pydia.sdk.utils.Log
import java.text.ParseException

class Token {
    // Set by Cells layers to contain the corresponding encoded accountID
    var subject: String? = null

    // value is the real useful token => access_token in OAuth2
    var value: String? = null

    // idToken contains encoded information about current session, typically the claims
    var idToken: String? = null
    var scope: String? = null
    var tokenType: String? = null
    var refreshToken: String? = null
    var expiresIn: Long = 0
    var expirationTime: Long = 0
    var refreshingSinceTs: Long = 0
    val isExpired: Boolean
        get() {
            if (expirationTime == -1L) {
                return false
            }
            if (value == null) {
                return true
            }
            if ("" == value) {
                return true
            }
            val elapsedTimeSinceExpiry = currentTimeInSeconds() - expirationTime
            val expired = elapsedTimeSinceExpiry > 0
            if (expired) {
                Log.i(
                    logTag,
                    String.format(
                        "Token for %s is expired since %s seconds",
                        subject,
                        elapsedTimeSinceExpiry
                    )
                )
            }
            return expired
        }

    fun setExpiry(expiresIn: Long) {
        if (expiresIn == -1L) {
            expirationTime = -1
            this.expiresIn = -1
        } else {
            expirationTime = currentTimeInSeconds() + expiresIn
            this.expiresIn = expiresIn
        }
    }

    private fun currentTimeInSeconds(): Long {
        return System.currentTimeMillis() / 1000
    }

    companion object {
        private const val logTag = "Token"
        fun encode(t: Token?): String {
            val gson = Gson()
            return gson.toJson(t)
        }

        fun decode(json: String?): Token {
            return Gson().fromJson(json, Token::class.java)
        }

        @Throws(ParseException::class)
        fun decodeOAuthJWT(jwt: String?): Token {
            val t = Token()
            val jo = JSONObject(jwt)
            t.value = jo.getString("access_token")
            t.setExpiry(jo.getInt("expires_in").toLong())
            t.scope = jo.getString("scope")
            t.idToken = jo.getString("id_token")
            t.refreshToken = jo.getString("refresh_token")
            t.tokenType = jo.getString("token_type")
            return t
        }
    }
}