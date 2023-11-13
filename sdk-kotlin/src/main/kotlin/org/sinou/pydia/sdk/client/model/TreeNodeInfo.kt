package org.sinou.pydia.sdk.client.model

import com.google.gson.Gson
import org.sinou.pydia.sdk.utils.CellsPath

class TreeNodeInfo {
    var eTag: String? = null
    var size: Long = 0
    var lastEdit: Long = 0
    var name: String? = null
        private set
    private var path: String? = null
    var isLeaf = false

    constructor()
    constructor(eTag: String, path: String, isLeaf: Boolean, size: Long, lastEdit: Long) {
        this.eTag = eTag
        this.path = path
        name = CellsPath.nameFromFullPath(path)
        this.isLeaf = isLeaf
        this.size = size
        this.lastEdit = lastEdit
    }

    fun setPath(path: String) {
        this.path = path
        name = CellsPath.nameFromFullPath(path)
    }

    fun getPath(): String? {
        return path
    }

    fun encoded(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun fromEncoded(encoded: String): TreeNodeInfo {
            return Gson().fromJson(encoded, TreeNodeInfo::class.java)
        }
    }
}