package org.sinou.pydia.sdk.utils

/**
 * Provides shortcuts for methods that are widely used when manipulating nodes
 * and their path in a Pydio Cells context.
 *
 *
 * Note: we talk here about tree node path: that are typically "common-files/folder/file.jpg"
 * in the below code, we assume path are valid:
 * - they never start with a /
 * - workspace is always provided
 */
object CellsPath {
    fun fullPath(workspace: String?, path: String): String {
        val sb = StringBuilder(workspace)
        return if ("/" == path) {
            sb.toString()
        } else {
            sb.append("/").append(path)
            sb.toString()
        }
    }

    fun getWorkspace(fullPath: String): String {
        val parts = fullPath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return parts[0]
    }

    fun nameFromFullPath(fullPath: String): String {
        val parts = fullPath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (parts.size == 1) {
            "/"
        } else {
            parts[parts.size - 1]
        }
    }
}