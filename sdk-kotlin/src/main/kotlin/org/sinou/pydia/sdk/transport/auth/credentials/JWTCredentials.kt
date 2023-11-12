package org.sinou.pydia.sdk.transport.auth.credentials

import org.sinou.pydia.sdk.api.Credentials
import org.sinou.pydia.sdk.transport.auth.Token

class JWTCredentials(private val username: String, val token: Token) : Credentials {

    override fun getUsername(): String {
        return username
    }

    override fun getType(): String {
        return Credentials.TYPE_JWT
    }

    override fun getEncodedValue(): String {
        return token.toString()
    }
}