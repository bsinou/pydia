package org.sinou.pydia.sdk.transport.auth.credentials

import org.sinou.pydia.sdk.api.Credentials
import org.sinou.pydia.sdk.api.PasswordCredentials

class LegacyPasswordCredentials(private val login: String, private val password: String) :
    PasswordCredentials {
    override fun getUsername(): String {
        return login
    }

    override fun getType(): String {
        return Credentials.TYPE_LEGACY_PASSWORD
    }

    override fun getEncodedValue(): String {
        return getPassword()
    }

    override fun getPassword(): String {
        return password
    }
}