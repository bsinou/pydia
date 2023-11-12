package org.sinou.pydia.sdk.api

interface Store<T> {
    fun put(id: String, obj: T)
    operator fun get(id: String): T?
    fun getAll(): Map<String, T>
    fun remove(id: String)
    fun clear()
}