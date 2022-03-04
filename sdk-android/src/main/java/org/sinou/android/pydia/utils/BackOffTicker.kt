package org.sinou.android.pydia.utils

import kotlin.math.min

class BackOffTicker {
    private val backoffDuration = longArrayOf(
        2, 3, 5, 10, 15, 20,
        30, 30, 30, 30, 30, 30,
        40, 60, 60, 120, 120, 300,
        600, 600, 900, 900, 900, 1800, 3600
    )
    private val lock = 0
    private var currentBackoffIndex = 0

    fun getNextDelay(): Long {
        synchronized(lock) {
            val nextDelay = backoffDuration[currentBackoffIndex]
            currentBackoffIndex = min(currentBackoffIndex + 1, backoffDuration.size - 1)
            return nextDelay
        }
    }

    fun getCurrentIndex():Int {
        synchronized(lock) {
            return currentBackoffIndex
        }
    }

    fun resetIndex() {
        synchronized(lock) {
            currentBackoffIndex = 0
        }
    }
}