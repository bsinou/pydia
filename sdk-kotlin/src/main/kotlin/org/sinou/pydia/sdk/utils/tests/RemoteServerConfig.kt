package org.sinou.pydia.sdk.utils.tests

/**
 * Simple object that exposes the basic information to be provided
 * to define a test `Session`
 */
data class RemoteServerConfig(
    val serverURL: String,
    var username: String,
    val skipVerify: Boolean = false,
    val pat: String? = null,
    val pwd: String? = null,
    val defaultWS: String? = null,
)
