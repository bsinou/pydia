package org.sinou.pydia.sdk.utils

//object PathUtils {

// Rather use extension functions on the StateID Object
//    fun getWorkspace(path: String?): String? {
//        return if (path == null || "" == path || "/" == path) {
//            null
//        } else path.substring(1).split("/".toRegex()).dropLastWhile { it.isEmpty() }
//            .toTypedArray()[0]
//    }
//
//    fun getFile(path: String?): String? {
//        if (path == null || "" == path) { // || "/".equals(path)) {
//            return null
//        }
//        val prefix = "/" + getWorkspace(path)
//        if (path.length > prefix.length) {
//            return path.substring(prefix.length)
//        } else if (path.length == prefix.length) {
//            // we only have the workspace, so we consider we are at root of the workspace
//            return "/"
//        }
//        return null
//    }
//
//    fun getPath(ws: String, file: String?): String? {
//        var path: String? = "/$ws"
//        if (Str.notEmpty(file)) {
//            path += file
//        }
//        return path
//    }
//}