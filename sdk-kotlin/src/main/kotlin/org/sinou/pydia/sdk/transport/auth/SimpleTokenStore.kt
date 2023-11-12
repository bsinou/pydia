package org.sinou.pydia.sdk.transport.auth

import java.util.concurrent.ConcurrentHashMap

class SimpleTokenStore : TokenStore {
    private val secureTokens: MutableMap<String, Token?> = ConcurrentHashMap()
    override fun save(key: String, t: Token?) {
        secureTokens[key] = t
    }

    override fun get(key: String): Token? {
        return secureTokens[key]
    }

    override fun delete(key: String) {
        secureTokens.remove(key)
    }

    override val all: Map<String, Token?>
        get() = secureTokens
}