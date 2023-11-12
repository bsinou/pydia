package org.sinou.pydia.sdk.client.model

import org.sinou.pydia.sdk.api.ui.FileNode

class FileNodeList {
    var page = 0
    var pageCount = 0
    var offset = 0
    var nodeCount = 0
    var list: List<FileNode>? = null
}