package org.sinou.pydia.sdk.transport.auth

interface TokenStore {
    fun save(key: String, t: Token?)
    operator fun get(key: String): Token?
    fun delete(key: String)

    val all: Map<String, Token?>?
}