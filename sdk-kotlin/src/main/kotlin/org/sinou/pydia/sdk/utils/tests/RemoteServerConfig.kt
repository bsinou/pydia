package org.sinou.pydia.sdk.utils.tests

/**
 * Simple object that exposes the basic information to be provided
 * to define a test `Session`
 */
class RemoteServerConfig {

    /* Main account info */
    @JvmField
    var serverURL: String? = null
    @JvmField
    var skipVerify = false
    @JvmField
    var username: String? = null

    /* Credentials */ // Personal Access Token
    @JvmField
    var pat: String? = null

    // Simple clear text password (legacy, try to not use)
    @JvmField
    var pwd: String? = null

    /* Client preferences */
    @JvmField
    var defaultWS: String? = null
}