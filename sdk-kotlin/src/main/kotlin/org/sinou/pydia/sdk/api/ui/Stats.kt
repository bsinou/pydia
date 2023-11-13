package org.sinou.pydia.sdk.api.ui

/**
 * Represents the simplified result of a stat request to the server at a given path,
 * only keeping useful information
 */
class Stats {
    var hash: String? = null
    var size: Long = 0
    private var mTime: Long = 0
    fun getmTime(): Long {
        return mTime
    }

    fun setmTime(mTime: Long) {
        this.mTime = mTime
    }
}