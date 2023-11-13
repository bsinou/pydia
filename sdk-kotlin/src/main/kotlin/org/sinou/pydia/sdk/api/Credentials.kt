package org.sinou.pydia.sdk.api

/**
 * Generic interface to manage credentials. We only always have a username
 */
enum class CredentialType {
    PAT, JWT, LEGACY_PASSWORD
}

interface Credentials {

    fun getUsername(): String
    fun getType(): CredentialType
    fun getEncodedValue(): String?

}
