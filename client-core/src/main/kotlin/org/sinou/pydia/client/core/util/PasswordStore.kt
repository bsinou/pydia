package org.sinou.pydia.client.core.util

import org.sinou.pydia.sdk.api.Store

class MemoryPasswordStore() : Store<String> {

    private val passwords = mutableMapOf<String, String>()

    override fun put(id: String, obj: String) {
        passwords.put(id, obj)
    }

    override fun get(id: String): String? {
        return passwords.get(id)
    }

    override fun remove(id: String) {
        passwords.remove(id)
    }

    override fun clear() {
        passwords.clear()
    }

    override fun getAll(): Map<String, String> {
        return passwords
    }
}
