package org.sinou.pydia.sdk.api

/**
 * Credentials based on username and password. Rather use OAuth flows or personal access token.
 */
interface PasswordCredentials : Credentials {
    fun getPassword(): String
}