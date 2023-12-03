package org.sinou.pydia.sdk.api

import org.sinou.pydia.openapi.model.TreeNode
import org.sinou.pydia.sdk.api.ui.PageOptions
import org.sinou.pydia.sdk.api.ui.WorkspaceNode
import org.sinou.pydia.sdk.transport.StateID
import java.io.File
import java.io.OutputStream
import java.util.Properties

interface Client {

    @Throws(SDKException::class)
    fun getDefaultRegistry(): Registry?

    @Throws(SDKException::class)
    fun getUserRegistry(): Registry?

    @Throws(SDKException::class)
    fun stillAuthenticated(): Boolean

    @Throws(SDKException::class)
    fun getWorkspaceList(handler: (WorkspaceNode) -> Unit)

    @Throws(SDKException::class)
    fun ls(
        slug: String,
        path: String,
        options: PageOptions?,
        handler: (TreeNode) -> Unit
    ): PageOptions

    @Throws(SDKException::class)
    fun mkdir(ws: String, parent: String, name: String)

    @Throws(SDKException::class)
    fun delete(slug: String, paths: Array<String>, removePermanently: Boolean)

    @Throws(SDKException::class)
    fun statNode(file: String): TreeNode?


    /**
     * @param node         The local FileNode with current object meta-data
     * @param parentFolder Must be writable for the current process
     * @param dim          The expected thumbnail dimension
     * @return the filename If a thumbnail has been correctly downloaded or generated or null otherwise
     * @throws SDKException Wraps "known" exception with our error code
     * and a local message to ease downstream management of the error.
     */
    @Throws(SDKException::class)
    fun getThumbnail(
        stateID: StateID,
        uuid: String,
        props: Properties,
        parentFolder: File,
        dim: Int
    ): String?

    @Throws(SDKException::class)
    fun download(
        ws: String,
        file: String,
        target: OutputStream,
        onProgress: ((Long) -> String?)?
    ): Long


    @Throws(SDKException::class)
    fun getNodeMeta(ws: String, file: String): TreeNode?

    @Throws(SDKException::class)
    fun search(ws: String, dir: String, searchedText: String, h: (TreeNode) -> Unit)

    @Throws(SDKException::class)
    fun copy(ws: String, files: Array<String>, folder: String)

    @Throws(SDKException::class)
    fun move(ws: String, files: Array<String>, dstFolder: String)

    @Throws(SDKException::class)
    fun rename(ws: String, srcFile: String, newName: String)

    @Throws(SDKException::class)
    fun restore(ws: String, nodes: List<TreeNode>?)

    @Throws(SDKException::class)
    fun emptyRecycleBin(ws: String)

    @Throws(SDKException::class)
    fun bookmark(slug: String, file: String, isBookmarked: Boolean)

    @Throws(SDKException::class)
    fun bookmark(ws: String, file: String)

    @Throws(SDKException::class)
    fun unbookmark(ws: String, file: String)

    @Throws(SDKException::class)
    fun getBookmarks(h: (TreeNode) -> Unit)

    @Throws(SDKException::class)
    fun share(
        workspace: String,
        file: String,
        wsLabel: String,
        wsDesc: String,
        password: String,
        canPreview: Boolean,
        canDownload: Boolean
    ): String

    @Throws(SDKException::class)
    fun share(
        workspace: String,
        file: String,
        wsLabel: String,
        isFolder: Boolean,
        wsDescription: String,
        password: String,
        expiration: Int,
        download: Int,
        canPreview: Boolean,
        canDownload: Boolean
    ): String


    fun unshare(workspace: String, shareUuid: String)
    fun getShareAddress(ws: String, shareID: String): String


            //    /**
//     * Temporary test before cleaning this part of the code
//     * while testing both P8 and Cells
//     */
//    @Deprecated("")
//    @Throws(SDKException::class)
//    fun getNodeMeta(ws: String, file: String?): FileNode?
//
//    @Throws(SDKException::class)
//    fun stats(ws: String, file: String?, withHash: Boolean): Stats?
//
//
//    @Throws(SDKException::class)
//    fun search(parentPath: String?, searchedText: String?, size: Int): List<FileNode>
//
//    @Deprecated("")
//    @Throws(SDKException::class)
//    fun search(ws: String, dir: String?, searched: String?, h: NodeHandler)
//
//    @Throws(SDKException::class)
//    fun copy(ws: String, files: Array<String>, folder: String)
//
//    @Throws(SDKException::class)
//    fun move(ws: String, files: Array<String>, dstFolder: String)
//
//    @Throws(SDKException::class)
//    fun rename(ws: String, srcFile: String, newName: String)
//
//    @Throws(SDKException::class)
//    fun delete(ws: String, files: Array<String>)
//
//    @Throws(SDKException::class)
//    fun restore(ws: String, files: Array<FileNode>)
//
//    @Throws(SDKException::class)
//    fun emptyRecycleBin(ws: String)
//
//    @Throws(SDKException::class)
//    fun upload(
//        source: InputStream,
//        length: Long,
//        mime: String,
//        ws: String,
//        path: String,
//        name: String,
//        autoRename: Boolean,
//        progressListener: ProgressListener?
//    )
//
//    @Throws(SDKException::class)
//    fun upload(
//        source: File,
//        mime: String,
//        ws: String,
//        path: String,
//        name: String,
//        autoRename: Boolean,
//        progressListener: ProgressListener?
//    )
//
//    @Throws(SDKException::class)
//    fun uploadURL(ws: String, folder: String, name: String, autoRename: Boolean): String
//

//
//    @Throws(SDKException::class)
//    fun download(
//        ws: String,
//        file: String,
//        target: File,
//        progressListener: ProgressListener?
//    ): Long
//
//    @Throws(SDKException::class)
//    fun downloadPath(ws: String, file: String): String
//
//    @Throws(SDKException::class)
//    fun streamingAudioURL(ws: String, file: String): String
//
//    @Throws(SDKException::class)
//    fun streamingVideoURL(ws: String, file: String): String
//
//    @Throws(SDKException::class)
//    fun getBookmarks(h: NodeHandler)
//
//    @Throws(SDKException::class)
//    fun bookmark(workspace: String, file: String, isBookmarked: Boolean)
//
//    // Rather use bookmark(slug, path, isBookmarked)
//    @Deprecated("")
//    @Throws(SDKException::class)
//    fun bookmark(ws: String, file: String)
//
//    // Rather use bookmark(slug, path, isBookmarked)
//    @Deprecated("")
//    @Throws(SDKException::class)
//    fun unbookmark(ws: String, file: String)
//
//    @Throws(SDKException::class)
//    fun share(
//        workspace: String, file: String, linkLabel: String?,
//        linkDescription: String?, password: String?,
//        canPreview: Boolean, canDownload: Boolean
//    ): String
//
//    @Deprecated("")
//    @Throws(SDKException::class)
//    fun share(
//        ws: String,
//        file: String,
//        wsLabel: String,
//        isFolder: Boolean,
//        wsDescription: String?,
//        password: String?,
//        expiration: Int,
//        download: Int,
//        canPreview: Boolean,
//        canDownload: Boolean
//    ): String
//
//    @Throws(SDKException::class)
//    fun unshare(workspace: String, file: String)
//
//    @Throws(SDKException::class)
//    fun getShareAddress(ws: String, file: String): String
}