package org.sinou.pydia.sdk.api.ui

class ChangeNode {
    var id: String? = null
    var md5: String? = null
    var size: Long = 0
    private var mTime: Long = 0
    var path: String? = null
    var workspace: String? = null
    fun getmTime(): Long {
        return mTime
    }

    fun setmTime(mTime: Long) {
        this.mTime = mTime
    }
}