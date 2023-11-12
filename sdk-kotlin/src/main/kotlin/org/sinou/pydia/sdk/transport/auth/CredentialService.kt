package org.sinou.pydia.sdk.transport.auth

import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.Store
import org.sinou.pydia.sdk.transport.StateID

/**
 * Wraps a token store to add the feature to refresh tokens from password when
 * necessary
 */
abstract class CredentialService(
    private val tokenStore: Store<Token>,
    private val passwordStore: Store<String>
) : Store<Token> {
    fun putPassword(id: String, password: String) {
        passwordStore.put(id, password)
    }

    fun getPassword(id: String): String? {
        return passwordStore[id]
    }

    fun removePassword(id: String) {
        passwordStore.remove(id)
    }

    fun clearPasswords() {
        passwordStore.clear()
    }

    /**
     * Provides an entry point to cleanly handle refresh token process,
     * typically avoiding launching 2 parallel processes (or more) at the same time
     * that would lead to loosing the credentials.
     */
    @Throws(SDKException::class)
    abstract fun requestRefreshToken(stateID: StateID)

    /* Simply wraps passed tokenStore methods */
    override fun put(id: String, obj: Token) {
        tokenStore.put(id, obj)
    }

    override fun get(id: String): Token? {
        return tokenStore[id]
    }

    override fun getAll(): Map<String, Token> {
        return tokenStore.getAll()
    }

    override fun remove(id: String) {
        tokenStore.remove(id)
    }

    override fun clear() {
        tokenStore.clear()
    }
}
