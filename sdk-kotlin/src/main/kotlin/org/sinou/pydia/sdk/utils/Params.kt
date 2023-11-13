package org.sinou.pydia.sdk.utils

/**
 * Class Params implements a wrapper around a simple HashMap to ease implementation of various HTTP requests
 */
class Params private constructor() {
    private val params: MutableMap<String, String>

    init {
        params = HashMap()
    }

    operator fun set(key: String, value: String): Params {
        params[key] = value
        return this
    }

    fun del(key: String): Params {
        params.remove(key)
        return this
    }

    fun get(): Map<String, String> {
        return params
    }

    companion object {
        fun create(key: String, value: String): Params {
            val p = Params()
            p[key] = value
            return p
        }
    }
}