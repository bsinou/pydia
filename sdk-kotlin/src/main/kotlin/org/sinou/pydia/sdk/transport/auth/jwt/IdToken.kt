package org.sinou.pydia.sdk.transport.auth.jwt

import com.google.gson.Gson
import org.sinou.pydia.sdk.api.CustomEncoder
import java.text.ParseException

class IdToken {
    var header: Header? = null
    var claims: Claims? = null
    var signature: String? = null

    companion object {
        @Throws(ParseException::class)
        fun parse(decoder: CustomEncoder, strJwt: String): IdToken {
            val parts = strJwt.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (parts.size != 3) {
                throw ParseException("IdToken string cannot be parsed in 3 parts as expected", -1)
            }
            val idToken = IdToken()
            val headerStr = decoder.base64Decode(parts[0])
            idToken.header = Gson().fromJson(headerStr, Header::class.java)
            val claimsStr = decoder.base64Decode(parts[1])
            idToken.claims = Gson().fromJson(claimsStr, Claims::class.java)
            idToken.signature = parts[2]
            return idToken
        }
    }
}