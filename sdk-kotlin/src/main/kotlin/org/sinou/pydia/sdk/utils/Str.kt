package org.sinou.pydia.sdk.utils

/**
 * A few boiler plate helpers for conciseness while dealing with String objects
 */
object Str {
    @JvmStatic
    fun empty(value: String?): Boolean {
        return value == null || "" == value
    }

    @JvmStatic
    fun notEmpty(value: String?): Boolean {
        return value != null && "" != value
    }
}