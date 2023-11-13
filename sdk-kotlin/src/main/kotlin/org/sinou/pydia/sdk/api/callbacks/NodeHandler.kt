package org.sinou.pydia.sdk.api.callbacks

import org.sinou.pydia.openapi.model.TreeNode

interface NodeHandler {
    fun onNode(node: TreeNode)
}