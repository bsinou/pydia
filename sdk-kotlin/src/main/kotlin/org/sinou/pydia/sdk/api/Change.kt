package org.sinou.pydia.sdk.api

import org.sinou.pydia.sdk.api.ui.ChangeNode
import java.io.Serializable

class Change : Serializable {
    var seq: Long = 0
    var sourceSide: String? = null
    var targetSide: String? = null
    var type: String? = null
    var source: String? = null
    var target: String? = null
    var nodeId: String? = null
    var node: ChangeNode? = null

    companion object {
        const val TYPE_CREATE = "create"
        const val TYPE_PATH = "path"
        const val TYPE_DELETE = "delete"
        const val TYPE_CONTENT = "content"
    }
}