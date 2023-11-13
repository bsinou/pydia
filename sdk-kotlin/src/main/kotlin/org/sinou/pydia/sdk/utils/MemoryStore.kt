package org.sinou.pydia.sdk.utils

import org.sinou.pydia.sdk.api.Store

class MemoryStore<T> : Store<T> {

    private val objects: MutableMap<String, T> = HashMap()
    override fun put(id: String, obj: T) {
        objects[id] = obj
    }

    override fun get(id: String): T? {
        return objects[id]
    }

    override fun getAll(): Map<String, T> {
        return objects
    }

    override fun remove(id: String) {
        objects.remove(id)
    }

    override fun clear() {
        objects.clear()
    }
}