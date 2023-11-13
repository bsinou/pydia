package org.sinou.pydia.sdk.client

import org.sinou.pydia.openapi.api.TreeServiceApi
import org.sinou.pydia.openapi.api.UserServiceApi
import org.sinou.pydia.openapi.infrastructure.ApiClient
import org.sinou.pydia.openapi.infrastructure.ServerException
import org.sinou.pydia.openapi.model.RestBulkMetaResponse
import org.sinou.pydia.openapi.model.RestCreateNodesRequest
import org.sinou.pydia.openapi.model.RestDeleteNodesRequest
import org.sinou.pydia.openapi.model.RestGetBulkMetaRequest
import org.sinou.pydia.openapi.model.TreeNode
import org.sinou.pydia.openapi.model.TreeNodeType
import org.sinou.pydia.sdk.api.Client
import org.sinou.pydia.sdk.api.ErrorCodes
import org.sinou.pydia.sdk.api.Registry
import org.sinou.pydia.sdk.api.S3Client
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.SdkNames
import org.sinou.pydia.sdk.api.Transport
import org.sinou.pydia.sdk.api.callbacks.NodeHandler
import org.sinou.pydia.sdk.api.ui.FileNode
import org.sinou.pydia.sdk.api.ui.PageOptions
import org.sinou.pydia.sdk.client.model.DocumentRegistry
import org.sinou.pydia.sdk.client.model.TreeNodeInfo
import org.sinou.pydia.sdk.transport.CellsTransport
import org.sinou.pydia.sdk.transport.StateID
import org.sinou.pydia.sdk.utils.FileNodeUtils
import org.sinou.pydia.sdk.utils.IoHelpers
import org.sinou.pydia.sdk.utils.Log
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import javax.xml.parsers.ParserConfigurationException

class CellsClient(transport: Transport, private val s3Client: S3Client) : Client, SdkNames {
    private val transport: CellsTransport

    init {
        this.transport = transport as CellsTransport
    }

    @Throws(SDKException::class)
    override fun stillAuthenticated(): Boolean {
        return try {
            val api = UserServiceApi(transport.getApiURL(), ApiClient.defaultClient)
            //            IdmUser user =
            api.getUser(
                transport.username, null, null, null, null,
                false, null, -1, true
            )
            true
        } catch (e: SDKException) {
            Log.e(
                logTag, "SDK error #" + e.code
                        + " while checking auth state for " + StateID.fromId(transport.id)
            )
            false
        } catch (e: ServerException) {
            Log.e(logTag, "API error while checking auth state for " + StateID.fromId(transport.id))
            e.printStackTrace()
            if (e.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return false
            }
            throw SDKException.fromServerException(e)
        }
    }

    override fun getDefaultRegistry(): Registry? {
        return null
    }

    override fun getUserRegistry(): Registry? {
        return null
    }

    @Throws(SDKException::class)
    override fun getWorkspaceList(handler: NodeHandler) {
        var con: HttpURLConnection? = null
        var input: InputStream? = null
        val registry: Registry
        try {
            con = transport.openApiConnection("/frontend/state")
            con.requestMethod = "GET"
            input = con.inputStream
            val responseCode = con.responseCode
            if (responseCode != 200) {
                val msg = "could not get registry for " + transport.id + "(" + responseCode + ")"
                throw SDKException.conFailed(msg, IOException(con.responseMessage))
            }
            registry = DocumentRegistry(input)
            if (!registry.isLoggedIn()) {
                // TODO Double check if we are correctly connected
                throw SDKException(
                    ErrorCodes.authentication_required,
                    "not logged in " + transport.id + ", you cannot list workspaces."
                )
            }
            // FIXME do we still need this
//            for (node in registry.workspaces) {
//                if (!Arrays.asList<Any>(defaultExcludedWorkspaces).contains(node.accessType)) {
//                    handler.onNode(node)
//                }
//            }
        } catch (e: ParserConfigurationException) {
            throw SDKException.unexpectedContent(e)
        } catch (e: SAXException) {
            throw SDKException.unexpectedContent(e)
        } catch (e: IOException) {
            throw SDKException.conFailed("could not get registry for " + transport.id, e)
        } finally {
            IoHelpers.closeQuietly(input)
            IoHelpers.closeQuietly(con)
        }
    }

    @Throws(SDKException::class)
    override fun ls(
        slug: String,
        path: String,
        options: PageOptions?,
        handler: NodeHandler
    ): PageOptions {
        val request = RestGetBulkMetaRequest(
            nodePaths = listOf(
                FileNodeUtils.toTreeNodePath(
                    slug,
                    if ("/" == path) "/*" else "$path/*"
                )
            ),
            allMetaProviders = true,
            limit = options?.limit,
            offset = options?.offset,
        )
        val (url, okClient) = transport.apiConf()
        val api = TreeServiceApi(url, okClient)
        val response: RestBulkMetaResponse
        val nextPageOptions = PageOptions()
        try {
            response = api.bulkStatNodes(request)
            response.pagination?.let { pag ->

                pag.limit?.let { nextPageOptions.limit = it }
                nextPageOptions.offset = pag.nextOffset ?: -1
                pag.total?.let { nextPageOptions.total = it }
                pag.currentPage?.let { nextPageOptions.currentPage = it }
                pag.totalPages?.let { nextPageOptions.totalPages = it }

            } ?: run {
                val nodes = response.nodes
                if (nodes != null) {
                    val size = nodes.size
                    nextPageOptions.limit = size
                    nextPageOptions.total = size
                    nextPageOptions.currentPage = 1
                    nextPageOptions.totalPages = 1
                    nextPageOptions.offset = 0
                }
            }
        } catch (e: ServerException) {
            val msg = "Could not list: " + e.message
            throw SDKException(e.statusCode, msg, e)
        }

        response.nodes?.let { nodes ->
            nodes.forEach { handler.onNode(it) }
        }
        return nextPageOptions
    }

    @Throws(SDKException::class)
    override fun mkdir(ws: String, parent: String, name: String) {
        val node = TreeNode(
            path = "$ws$parent/$name".replace("//", "/"),
            type = TreeNodeType.COLLECTION
        )

        val request = RestCreateNodesRequest(
            recursive = false,
            nodes = listOf(node)
        )

        val (url, client) = transport.apiConf()
        val api = TreeServiceApi(url, client)
        val response = try {
            api.createNodes(request)
        } catch (e: ServerException) {
            e.printStackTrace()
            throw SDKException.fromServerException(e)
        }
    }

    //    @Throws(SDKException::class)
    override fun delete(slug: String, paths: Array<String>, removePermanently: Boolean) {
        val nodes = paths.map { TreeNode(path = FileNodeUtils.toTreeNodePath(slug, it)) }
        val request = RestDeleteNodesRequest(nodes = nodes, removePermanently = removePermanently)
        val (url, okClient) = transport.apiConf()
        val api = TreeServiceApi(url, okClient)
        try {
            api.deleteNodes(request)
        } catch (e: ServerException) {
            e.printStackTrace()
            throw SDKException.fromServerException(e)
        }
    }

    @Throws(SDKException::class)
    override fun nodeInfo(ws: String, path: String): FileNode? {
        val node = internalStatNode(ws, path)
        return node?.let { FileNodeUtils.toFileNode(it) }
    }

    //    @Throws(SDKException::class)
//    override fun getThumbnail(
//        stateID: StateID,
//        node: FileNode,
//        parentFolder: File,
//        dim: Int
//    ): String {
//        val filename = getThumbFilename(node, dim)!!
//        if (Str.empty(filename)) {
//            Log.i(logTag, "No thumbnail is defined for $stateID")
//            return null
//        }
//        var out: OutputStream? = null
//        try {
//            if (!parentFolder.exists()) {
//                if (!parentFolder.mkdirs()) {
//                    throw SDKException(
//                        ErrorCodes.internal_error,
//                        "could not create folder for thumbs at " + parentFolder.absolutePath
//                    )
//                }
//            }
//            val targetFile = File(parentFolder.absolutePath + File.separator + filename)
//            out = FileOutputStream(targetFile)
//            // Download API expect a full path starting with a slash (a.k.a a file, not a filename)
//            val file = "/$filename"
//            download(S3Names.PYDIO_S3_THUMBSTORE_PREFIX, file, out, null)
//        } catch (e: IOException) {
//            throw SDKException.conReadFailed("could not get thumb for $stateID", e)
//        } finally {
//            IoHelpers.closeQuietly(out)
//        }
//        return filename
//    }
//
//    /*
//     * If no thumb is defined or if it is currently processing, we return null.
//     * If we find only one thumb, we choose this one. Otherwise we return the smaller thumb that has at least required size.
//     *
//     * @param currNode
//     * @param dim
//     * @return
//     * @throws SDKException
//     */
//    @Throws(SDKException::class)
//    private fun getThumbFilename(currNode: FileNode, dim: Int): String? {
//        var thumbName: String? = null
//        val imgThumbsStr = currNode.meta[SdkNames.META_KEY_IMG_THUMBS] as String?
//        if (Str.empty(imgThumbsStr)) {
//            return null
//        }
//        val thumbData: Map<String, Any>? =
//            Gson().fromJson<Map<*, *>>(imgThumbsStr, MutableMap::class.java)
//        if (thumbData != null && thumbData.containsKey("Processing")
//            && !(thumbData["Processing"] as Boolean)
//            && thumbData.containsKey("thumbnails")
//        ) {
//            val thumbs = thumbData["thumbnails"] as ArrayList<Map<String, Any>>?
//            for (currThumb in thumbs!!) {
//                // Map<String, Object> currThumb = (Map<String, Object>) currThumbObj;
//                val size = java.lang.Double.valueOf(currThumb["size"] as Double).toInt()
//                val format = currThumb["format"] as String?
//                val currName = currNode.id + "-" + size + "." + format
//                if (thumbName == null) {
//                    thumbName = currName
//                }
//                if (size > 0 && size >= dim) {
//                    thumbName = currName
//                    break
//                }
//            }
//        }
//        return thumbName
//    }
//
//    @Throws(SDKException::class)
//    override fun getNodeMeta(ws: String, file: String): FileNode {
//        val request = RestGetBulkMetaRequest()
//        request.addNodePathsItem(FileNodeUtils.toTreeNodePath(ws, file))
//        request.setAllMetaProviders(true)
//        val api = TreeServiceApi(authenticatedClient())
//        val response: RestBulkMetaResponse
//        response = try {
//            api.bulkStatNodes(request)
//        } catch (e: ApiException) {
//            e.printStackTrace()
//            throw SDKException.fromApiException(e)
//        }
//        if (response.nodes == null || response.nodes.isEmpty()) {
//            Log.w(logTag, "No node found for " + PathUtils.getPath(ws, file))
//            return null
//        }
//        return FileNodeUtils.toFileNode(response.nodes[0])
//    }
//
//    @Throws(SDKException::class)
//    override fun stats(ws: String, file: String, withHash: Boolean): Stats {
//        val request = RestGetBulkMetaRequest()
//        request.addNodePathsItem(FileNodeUtils.toTreeNodePath(ws, file))
//        request.setAllMetaProviders(true)
//        val api = TreeServiceApi(authenticatedClient())
//        val response: RestBulkMetaResponse
//        response = try {
//            api.bulkStatNodes(request)
//        } catch (e: ApiException) {
//            e.printStackTrace()
//            throw SDKException.fromApiException(e)
//        }
//        var stats: Stats? = null
//        if (response.nodes != null) {
//            val node = response.nodes[0]
//            stats = Stats()
//            stats.hash = node.etag
//            if (node.getSize() != null) {
//                stats.size = node.getSize().toLong()
//            }
//            if (node.mtime != null) {
//                stats.setmTime(node.mtime.toLong())
//            }
//        }
//        return stats!!
//    }
//
//    @Throws(SDKException::class)
//    override fun search(ws: String, dir: String, searchedText: String, h: NodeHandler) {
//        val query = TreeQuery()
//        query.setFileName(searchedText)
//        val prefix = ws + dir
//        query.addPathPrefixItem(prefix)
//        val request = TreeSearchRequest()
//        request.setSize(50)
//        request.setQuery(query)
//        val api = SearchServiceApi(authenticatedClient())
//        val results: RestSearchResults
//        results = try {
//            api.nodes(request)
//        } catch (e: ApiException) {
//            throw SDKException.fromApiException(e)
//        }
//        val nodes = results.results
//        if (nodes != null) {
//            for (treeNode in nodes) {
//                toMultipleNode(h, treeNode)
//                //                FileNode fileNode;
////                try {
////                    fileNode = toFileNode(treeNode);
////                } catch (NullPointerException ignored) {
////                    continue;
////                }
////
////                if (fileNode != null) {
////                    h.onNode(fileNode);
////                }
//            }
//        }
//    }
//
//    @Throws(SDKException::class)
//    override fun search(parentPath: String, searchedText: String, size: Int): List<FileNode> {
//        return try {
//            Log.d(logTag, " ... About to list nodes for [$searchedText] at $parentPath")
//            val query = TreeQuery()
//            query.setFileName(searchedText)
//            query.addPathPrefixItem(parentPath)
//            val request = TreeSearchRequest()
//            request.setSize(size)
//            request.setQuery(query)
//            val api = SearchServiceApi(authenticatedClient())
//            val (_, treeNodes) = api.nodes(request)
//            val fileNodes: MutableList<FileNode> = ArrayList()
//            if (treeNodes == null) {
//                Log.w(logTag, " .. Found no node for [$searchedText] at $parentPath")
//                return fileNodes
//            }
//            Log.d(
//                logTag, " .. Found " + treeNodes.size
//                        + " nodes for [" + searchedText + "] at " + parentPath
//            )
//            for (node in treeNodes) {
//                // FIXME this won't work, the workspace does not set the appearsIn variable in Search request results
////                toMultipleNode(fileNodes, node);
//                val fileNode = FileNodeUtils.toFileNode(node)
//                if (fileNode != null) {
//                    fileNodes.add(fileNode)
//                }
//            }
//            // Log.d(logTag, " .. After to multiple: " + fileNodes.size());
//            fileNodes
//        } catch (e: ApiException) {
//            throw SDKException.fromApiException(e)
//        }
//    }
//
//    @Throws(SDKException::class)
//    override fun upload(
//        source: InputStream,
//        length: Long,
//        mime: String,
//        ws: String,
//        path: String,
//        name: String,
//        autoRename: Boolean,
//        progressListener: ProgressListener
//    ) {
//        val preSignedURL = s3Client.getUploadPreSignedURL(ws, path, name)
//        val serverUrl: ServerURL
//        serverUrl = try {
//            transport.server.newURL(preSignedURL.path).withQuery(preSignedURL.query)
//        } catch (e: MalformedURLException) { // This should never happen with a pre-signed.
//            throw SDKException(
//                ErrorCodes.internal_error,
//                "Invalid pre-signed path: " + preSignedURL.path,
//                e
//            )
//        }
//        try {
//            val con = transport.withUserAgent(serverUrl.openConnection())
//            con.requestMethod = "PUT"
//            con.doOutput = true
//            con.setRequestProperty("Content-Type", "application/octet-stream")
//            con.setFixedLengthStreamingMode(length)
//            con.outputStream.use { out ->
//                try {
//                    IoHelpers.pipeReadWithIncrementalProgress(source, out, progressListener)
//                } catch (se: SDKException) {
//                    if (SDKException.isCancellation(se)) {
//                        IoHelpers.closeQuietly(out)
//                    }
//                    throw se
//                }
//            }
//            // TODO implement multi part upload
//            Log.d(logTag, "PUT request done with status " + con.responseCode)
//        } catch (e: IOException) {
//            throw SDKException.conWriteFailed("Cannot write to server", e)
//        }
//        // return Message.create(Message.SUCCESS, "SUCCESS");
//    }
//
//    @Throws(SDKException::class)
//    override fun upload(
//        source: File,
//        mime: String,
//        ws: String,
//        path: String,
//        name: String,
//        autoRename: Boolean,
//        progressListener: ProgressListener
//    ) {
//        upload(source, mime, ws, path, name, progressListener)
//    }
//
//    @Deprecated("")
//    override fun uploadURL(ws: String, folder: String, name: String, autoRename: Boolean): String {
//        throw RuntimeException("Unsupported method for cells client")
//    }
//
//    /**
//     * Warning, this expect a file (with a leading slash), not a file name
//     */
//    @Throws(SDKException::class)
//    override fun download(
//        ws: String,
//        file: String,
//        target: OutputStream,
//        progressListener: ProgressListener
//    ): Long {
//        // Log.e(logTag, "about to download [" + file + "]");
//        var `in`: InputStream? = null
//        return try {
//            val preSignedURL = s3Client.getDownloadPreSignedURL(ws, file)
//            val serverUrl: ServerURL
//            serverUrl = try {
//                transport.server.newURL(preSignedURL.path).withQuery(preSignedURL.query)
//            } catch (e: MalformedURLException) { // This should never happen with a pre-signed.
//                throw SDKException(
//                    ErrorCodes.internal_error,
//                    "Invalid pre-signed path: " + preSignedURL.path,
//                    e
//                )
//            }
//            val con = transport.withUserAgent(serverUrl.openConnection())
//            con.connect()
//            `in` = con.inputStream
//            if (progressListener == null) {
//                IoHelpers.pipeRead(`in`, target)
//            } else {
//                IoHelpers.pipeReadWithIncrementalProgress(`in`, target, progressListener)
//            }
//        } catch (e: IOException) {
//            if (e.message!!.contains("ENOSPC")) { // no space left on device
//                throw SDKException.noSpaceLeft(e)
//            }
//            throw SDKException.conReadFailed("could not download from $ws$file", e)
//        } finally {
//            IoHelpers.closeQuietly(`in`)
//        }
//    }
//
//    @Throws(SDKException::class)
//    override fun download(
//        ws: String,
//        file: String,
//        target: File,
//        progressListener: ProgressListener
//    ): Long {
//        var totalRead: Long = -1
//        var dlException: SDKException? = null
//        try {
//            FileOutputStream(target).use { out ->
//                totalRead = download(ws, file, out, progressListener)
//            }
//        } catch (e: FileNotFoundException) {
//            dlException = SDKException.notFound(e)
//        } catch (e: IOException) {
//            dlException = SDKException.conReadFailed("Could not download file $file", e)
//        } catch (e: SDKException) {
//            dlException =
//                SDKException(ErrorCodes.api_error, "Could not download file $file from $ws", e)
//        } finally {
//            if (dlException != null) {
//                // Best effort to download non-complete
//                try {
//                    target.delete()
//                } catch (e: Exception) {
//                    Log.w("Local", "Could not delete file at $target after failed download")
//                }
//                throw dlException
//            }
//        }
//        return totalRead
//    }
//
//    @Throws(SDKException::class)
//    override fun downloadPath(ws: String, file: String): String {
//        return fromURL(s3Client.getDownloadPreSignedURL(ws, file))
//    }
//
//    @Throws(SDKException::class)
//    override fun copy(ws: String, files: Array<String>, folder: String) {
//        val nodes = JSONArray()
//        for (file in files) {
//            // String path = "/" + ws + file;
//            val path = ws + file
//            nodes.put(path)
//        }
//        val o = JSONObject()
//        o.put("nodes", nodes)
//        // o.put("target", "/" + ws + folder);
//        o.put("target", ws + folder)
//        o.put("targetParent", true)
//        val request = RestUserJobRequest()
//        request.setJobName(CellsNames.JOB_ID_COPY)
//        request.setJsonParameters(o.toString())
//        val api = JobsServiceApi(authenticatedClient())
//        try {
//            api.userCreateJob(CellsNames.JOB_ID_COPY, request)
//        } catch (e: ApiException) {
//            e.printStackTrace()
//            throw SDKException.fromApiException(e)
//        }
//    }
//
//    @Throws(SDKException::class)
//    override fun move(ws: String, files: Array<String>, dstFolder: String) {
//        val nodes = JSONArray()
//        for (file in files) {
//            //String path = "/" + ws + file;
//            val path = ws + file
//            nodes.put(path)
//        }
//        val o = JSONObject()
//        o.put("nodes", nodes)
//        // o.put("target", "/" + ws + dstFolder);
//        o.put("target", ws + dstFolder)
//        o.put("targetParent", true)
//        val request = RestUserJobRequest()
//        request.setJobName(CellsNames.JOB_ID_MOVE)
//        request.setJsonParameters(o.toString())
//        val api = JobsServiceApi(authenticatedClient())
//        try {
//            api.userCreateJob(CellsNames.JOB_ID_MOVE, request)
//        } catch (e: ApiException) {
//            e.printStackTrace()
//            throw SDKException.fromApiException(e)
//        }
//    }
//
//    @Throws(SDKException::class)
//    override fun rename(ws: String, srcFile: String, newName: String) {
//        val nodes = JSONArray()
//        // In Cells, paths directly start with the WS slug (**NO** leading slash)
//        // String path = "/" + ws + srcFile;
//        val path = ws + srcFile
//        nodes.put(path)
//        val parent = File(srcFile).parentFile.path
//        val dstFile: String
//        dstFile = if ("/" == parent) {
//            parent + newName
//        } else {
//            "$parent/$newName"
//        }
//        // String targetFile ="/" + ws + dstFile;
//        val targetFile = ws + dstFile
//        val o = JSONObject()
//        o.put("nodes", nodes)
//        o.put("target", targetFile)
//        o.put("targetParent", false)
//        val request = RestUserJobRequest()
//        request.setJobName(CellsNames.JOB_ID_MOVE)
//        request.setJsonParameters(o.toString())
//        val api = JobsServiceApi(authenticatedClient())
//        try {
//            api.userCreateJob(CellsNames.JOB_ID_MOVE, request)
//        } catch (e: ApiException) {
//            e.printStackTrace()
//            throw SDKException.fromApiException(e)
//        }
//    }
//

    //
//    @Throws(SDKException::class)
//    override fun restore(ws: String, files: Array<FileNode>) {
//        val nodes: MutableList<TreeNode> = ArrayList()
//        for (file in files) {
//            val node: TreeNode = TreeNode().uuid(file.id).path(file.path)
//            nodes.add(node)
//        }
//        val request = RestRestoreNodesRequest()
//        request.setNodes(nodes)
//        val api = TreeServiceApi(authenticatedClient())
//        try {
//            api.restoreNodes(request)
//        } catch (e: ApiException) {
//            e.printStackTrace()
//            throw SDKException.fromApiException(e)
//        }
//    }
//
//    @Throws(SDKException::class)
//    override fun emptyRecycleBin(ws: String) {
//        delete(ws, arrayOf("/recycle_bin"))
//    }
//
//    @Throws(SDKException::class)
//    override fun streamingAudioURL(slug: String, file: String): String {
//        return fromURL(s3Client.getStreamingPreSignedURL(slug, file, S3Names.S3_CONTENT_TYPE_MP3))
//    }
//
//    @Throws(SDKException::class)
//    override fun streamingVideoURL(slug: String, file: String): String {
//        return fromURL(s3Client.getStreamingPreSignedURL(slug, file, S3Names.S3_CONTENT_TYPE_MP4))
//    }
//
//    @Throws(SDKException::class)
//    override fun getBookmarks(h: NodeHandler) {
//        val request = RestUserBookmarksRequest()
//        val api = UserMetaServiceApi(authenticatedClient())
//        try {
//            val (nodes) = api.userBookmarks(request)
//            if (nodes == null) {
//                return
//            }
//            for (node in nodes) {
//                toMultipleNode(h, node)
//            }
//        } catch (e: ApiException) {
//            e.printStackTrace()
//            throw SDKException.fromApiException(e)
//        }
//    }
//
//    private fun toMultipleNode(nodes: MutableList<FileNode>, treeNode: TreeNode) {
//        toMultipleNode({ node: Node? ->
//            if (node is FileNode) nodes.add(
//                node
//            )
//        }, treeNode)
//    }
//
//    private fun toMultipleNode(h: NodeHandler, node: TreeNode) {
//        try {
//            val fileNode = FileNodeUtils.toFileNode(node)
//            if (fileNode != null) {
//                val sources = node.appearsIn
//                if (sources != null) {
//                    // A node can appear in various workspaces (typically when referenced in a cell)
//                    // Yet the server sends back only one node with the specific "appears in" property,
//                    // We then have to return a node for each bookmark to the local cache
//                    for (twrp in sources) {
//                        var path = twrp.path
//                        if (Str.empty(path)) {
//                            Log.i(logTag, "Got an empty path for: " + fileNode.path)
//                            path = "/"
//                        } else if (!path!!.startsWith("/")) {
//                            path = "/$path"
//                        }
//                        fileNode.setProperty(SdkNames.NODE_PROPERTY_WORKSPACE_SLUG, twrp.wsSlug)
//                        fileNode.setProperty(SdkNames.NODE_PROPERTY_PATH, path)
//                        fileNode.setProperty(
//                            SdkNames.NODE_PROPERTY_FILENAME,
//                            FileNodeUtils.getNameFromPath(path)
//                        )
//                        h.onNode(fileNode)
//                    }
//                }
//            }
//        } catch (e: NullPointerException) {
//            Log.e(logTag, "###############################################################")
//            Log.e(logTag, "###############################################################")
//            Log.e(logTag, "###############################################################")
//            Log.e(logTag, "###############################################################")
//            Log.e(logTag, "Could node create FileNode for " + node.path + ", skipping")
//            e.printStackTrace()
//        }
//    }
//
//    @Throws(SDKException::class)
//    override fun bookmark(slug: String, file: String, isBookmarked: Boolean) {
//        if (isBookmarked) {
//            bookmark(slug, file)
//        } else {
//            unbookmark(slug, file)
//        }
//    }
//
//    @Throws(SDKException::class)
//    override fun bookmark(ws: String, file: String) {
//        bookmark(getNodeUuid(ws, file))
//    }
//
//    @Throws(SDKException::class)
//    fun bookmark(uuid: String?) {
//        val userMeta = IdmUserMeta()
//        userMeta.setNodeUuid(uuid)
//        userMeta.setNamespace("bookmark")
//        userMeta.setJsonValue("true")
//        val ownerPolicy = newPolicy(uuid, ServiceResourcePolicyAction.OWNER)
//        val readPolicy = newPolicy(uuid, ServiceResourcePolicyAction.READ)
//        val writePolicy = newPolicy(uuid, ServiceResourcePolicyAction.WRITE)
//        userMeta.addPoliciesItem(ownerPolicy)
//        userMeta.addPoliciesItem(readPolicy)
//        userMeta.addPoliciesItem(writePolicy)
//        val metas: MutableList<IdmUserMeta> = ArrayList()
//        metas.add(userMeta)
//        val request = IdmUpdateUserMetaRequest()
//        request.setMetaDatas(metas)
//        request.setOperation(UpdateUserMetaRequestUserMetaOp.PUT)
//        val api = UserMetaServiceApi(authenticatedClient())
//        try {
//            api.updateUserMeta(request)
//        } catch (e: ApiException) {
//            e.printStackTrace()
//            throw SDKException(
//                ErrorCodes.api_error,
//                "could not update bookmark user-meta: " + e.message,
//                e
//            )
//        }
//    }
//
//    @Throws(SDKException::class)
//    override fun unbookmark(ws: String, file: String) {
//        try {
//            val api = UserMetaServiceApi(authenticatedClient())
//
//            // Retrieve bookmark user meta with node UUID
//            val searchRequest = IdmSearchUserMetaRequest()
//            searchRequest.setNamespace("bookmark")
//            searchRequest.addNodeUuidsItem(getNodeUuid(ws, file))
//            val (metadatas) = api.searchUserMeta(searchRequest)
//
//            // Delete corresponding user meta
//            val request = IdmUpdateUserMetaRequest()
//            request.setOperation(UpdateUserMetaRequestUserMetaOp.DELETE)
//            request.setMetaDatas(metadatas)
//            api.updateUserMeta(request)
//        } catch (e: ApiException) {
//            throw SDKException.fromApiException(e)
//        }
//    }
//
//    @Throws(SDKException::class)
//    override fun share(
//        workspace: String,
//        file: String,
//        wsLabel: String,
//        isFolder: Boolean,
//        wsDescription: String,
//        password: String,
//        expiration: Int,
//        download: Int,
//        canPreview: Boolean,
//        canDownload: Boolean
//    ): String {
//        val uuid = getNodeUuid(workspace, file)
//        val request = RestPutShareLinkRequest()
//        request.createPassword(password)
//        request.setCreatePassword(password)
//        request.setPasswordEnabled(java.lang.Boolean.parseBoolean(password))
//        val sl = RestShareLink()
//        val n = TreeNode()
//        n.setUuid(uuid)
//        val permissions: MutableList<RestShareLinkAccessType> = ArrayList()
//        if (canPreview) {
//            permissions.add(RestShareLinkAccessType.PREVIEW)
//        }
//        if (canDownload) {
//            permissions.add(RestShareLinkAccessType.DOWNLOAD)
//        }
//        val rootNodes: MutableList<TreeNode> = ArrayList()
//        rootNodes.add(n)
//        sl.setPoliciesContextEditable(true)
//        sl.setPermissions(permissions)
//        sl.setRootNodes(rootNodes)
//        sl.setPoliciesContextEditable(true)
//        sl.setDescription(wsDescription)
//        sl.setLabel(wsLabel)
//        sl.setViewTemplateName("pydio_unique_strip")
//        request.setShareLink(sl)
//        val api = ShareServiceApi(authenticatedClient())
//        return try {
//            val (_, _, _, _, _, _, linkUrl) = api.putShareLink(request)
//            transport.server.url() + linkUrl
//        } catch (e: ApiException) {
//            throw SDKException.fromApiException(e)
//        }
//    }
//
//    @Throws(SDKException::class)
//    override fun share(
//        workspace: String, file: String, ws_label: String, ws_description: String,
//        password: String, canPreview: Boolean, canDownload: Boolean
//    ): String {
//        val targetRemote = internalStatNode(workspace, file)
//            ?: throw SDKException("Cannot share node $workspace/$file: it has disappeared on remote")
//        val request = RestPutShareLinkRequest()
//        val hasPwd = java.lang.Boolean.parseBoolean(password)
//        if (hasPwd) {
//            request.setCreatePassword(password)
//            request.setPasswordEnabled(true)
//        }
//        val shareLink = RestShareLink()
//        shareLink.setLabel(ws_label)
//        shareLink.setDescription(ws_description)
//        shareLink.setPoliciesContextEditable(true)
//        var meta: Map<String?, String>? = targetRemote.metaStore
//        if (meta == null) {
//            meta = HashMap()
//        }
//        if ("true" == meta["is_image"]) {
//            shareLink.setViewTemplateName(CellsNames.SHARE_TEMPLATE_GALLERY)
//        } else {
//            shareLink.setViewTemplateName(CellsNames.SHARE_TEMPLATE_FOLDER_LIST)
//        }
//        val n = TreeNode()
//        n.setUuid(getNodeUuid(workspace, file))
//        val rootNodes: MutableList<TreeNode> = ArrayList()
//        rootNodes.add(n)
//        shareLink.setRootNodes(rootNodes)
//        val permissions: MutableList<RestShareLinkAccessType> = ArrayList()
//        if (canPreview) {
//            permissions.add(RestShareLinkAccessType.PREVIEW)
//        }
//        if (canDownload) {
//            permissions.add(RestShareLinkAccessType.DOWNLOAD)
//        }
//        shareLink.setPermissions(permissions)
//        request.setShareLink(shareLink)
//        val api = ShareServiceApi(authenticatedClient())
//        return try {
//            val (_, _, _, _, _, _, linkUrl) = api.putShareLink(request)
//            transport.server.url() + linkUrl
//        } catch (e: ApiException) {
//            throw SDKException.fromApiException(e)
//        }
//    }
//
//    @Throws(SDKException::class)
//    override fun unshare(workspace: String, shareUuid: String) {
//        val api = ShareServiceApi(authenticatedClient())
//        try {
//            api.deleteShareLink(shareUuid)
//        } catch (e: ApiException) {
//            throw SDKException.fromApiException(e)
//        }
//    }
//
//    @Throws(SDKException::class)
//    override fun getShareAddress(ws: String, shareID: String): String {
//        val api = ShareServiceApi(authenticatedClient())
//        return try {
//            val (_, _, _, _, _, _, linkUrl) = api.getShareLink(shareID)
//            getFullLinkAddress(linkUrl, transport.server.url())
//        } catch (e: ApiException) {
//            throw SDKException.fromApiException(e)
//        }
//    }
//
//    @Throws(SDKException::class)
//    private fun getFullLinkAddress(linkUrl: String?, defaultPrefix: String): String {
//        return try {
//            val url = URL(linkUrl)
//            // Passed URL is valid we directly use this
//            url.toString()
//        } catch (e: MalformedURLException) {
//            if (!linkUrl!!.startsWith("/")) {
//                // Log.e(logTag, "Could not parse link URL: [" + linkUrl + "]");
//                throw SDKException(
//                    ErrorCodes.unexpected_response,
//                    "Public link [$linkUrl] is not valid",
//                    e
//                )
//            }
//            defaultPrefix + linkUrl
//        }
//    }
//
//    override fun isLegacy(): Boolean {
//        return false
//    }
//
//    @Throws(SDKException::class)
//    fun statNode(fullPath: String): TreeNodeInfo? {
//        val node = internalStatNode(fullPath)
//        return if (node != null) toTreeNodeinfo(node) else null
//    }
//
//    @Throws(SDKException::class)
//    fun changes(
//        ws: String?,
//        folder: String?,
//        seq: Int,
//        flatten: Boolean,
//        cp: ChangeHandler?
//    ): Long {
//        // RestChangeRequest request = new RestChangeRequest();
//        // request.setFlatten(flatten);
//        // request.setSeqID(String.valueOf(seq));
//        // request.setFilter("/" + ws + folder);
//
//        // this.getJWT();
//        // ApiClient client = getApiClient();
//        // client.addDefaultHeader("Authorization", "Bearer " + this.bearerValue);
//        // ChangeServiceApi api = new ChangeServiceApi(client);
//        // RestChangeCollection response;
//
//        // try {
//        // response = api.getChanges(String.valueOf(seq), request);
//        // } catch (ApiException e) {
//        // throw SDKException.fromApiException(e);
//        // }
//
//        // for (TreeSyncChange c : response.getChanges()) {
//        // Change change = new Change();
//        // change.setSeq(Long.parseLong(c.getSeq()));
//        // change.setNodeId(c.getNodeId());
//        // change.setType(c.getType().toString());
//        // change.setSource(c.getSource());
//        // change.setTarget(c.getTarget());
//
//        // ChangeNode node = new ChangeNode();
//        // change.setNode(node);
//
//        // node.setSize(Long.parseLong(c.getNode().getBytesize()));
//        // node.setMd5(c.getNode().getMd5());
//        // node.setPath(c.getNode().getNodePath().replaceFirst("/" + ws, ""));
//        // node.setWorkspace(ws);
//        // node.setmTime(Long.parseLong(c.getNode().getMtime()));
//
//        // cp.onChange(change);
//        // }
//        // return Long.parseLong(response.getLastSeqId());
//        throw RuntimeException("This must be reimplemented after API update")
//    }
//
//    /**
//     * Same as statNode() but rather return null than an [SDKException]
//     * in case the node is not found
//     */
//    @Throws(SDKException::class)
//    private fun statOptionalNode(fullPath: String): TreeNodeInfo? {
//        var node: TreeNode? = null
//        try {
//            node = internalStatNode(fullPath)
//        } catch (e: SDKException) {
//            if (e.code != 404) {
//                throw e
//            }
//        }
//        return if (node != null) toTreeNodeinfo(node) else null
//    }
//
    @Throws(SDKException::class)
    private fun internalStatNode(ws: String, path: String): TreeNode? {
// TODO it might be an idea to encode the "tokens" of the path to manage weird folder name (typically with %)
//    check this when we have the new caddy lib on the server side
//        return internalStatNode(FileNodeUtils.toEncodedTreeNodePath(ws, path));
        return internalStatNode(FileNodeUtils.toTreeNodePath(ws, path))
    }

    @Throws(SDKException::class)
    private fun internalStatNode(fullPath: String): TreeNode? {
        val api =
            TreeServiceApi(transport.getApiURL(), ApiClient.defaultClient)// authenticatedClient())
        // Log.d(logTag, "############# ");
        // Log.d(logTag, "############# internal stat for [" + fullPath + "]");
        // Log.d(logTag, "############# ");
        return try {
            api.headNode(fullPath).node
        } catch (e: ServerException) {
            throw SDKException.fromServerException(e)
        } catch (e: Exception) {
            Log.e(logTag, "unexpected error when doing stat node for $fullPath")
            e.printStackTrace()
            throw SDKException(
                ErrorCodes.internal_error,
                "unexpected error when doing stat node for $fullPath",
                e
            )
        }
    }

    //
//    /**
//     * List children of the node at `fullPath`. Note that it does nothing if
//     * no node is found at `fullPath`.
//     */
//    @Throws(SDKException::class)
//    fun listChildren(fullPath: String, handler: TreeNodeHandler) {
//        val request = RestGetBulkMetaRequest()
//        request.addNodePathsItem("$fullPath/*")
//        request.setAllMetaProviders(true)
//        val api = TreeServiceApi(authenticatedClient())
//        var response: RestBulkMetaResponse? = null
//        try {
//            response = api.bulkStatNodes(request)
//        } catch (e: ApiException) {
//            if (e.code != 404) {
//                throw SDKException.fromApiException(e)
//            }
//        }
//        if (response != null && response.nodes != null) {
//            val nodes = response.nodes!!.iterator()
//            while (nodes.hasNext()) {
//                handler.onNode(nodes.next())
//            }
//        }
//    }
//
//    @Throws(SDKException::class)
//    fun mkfile(ws: String, name: String, folder: String) {
//        val node = TreeNode()
//        node.setPath("/$ws$folder/$name")
//        node.setType(TreeNodeType.LEAF)
//        val request = RestCreateNodesRequest()
//        request.recursive(false)
//        request.addNodesItem(node)
//        val api = TreeServiceApi(authenticatedClient())
//        val response: RestNodesCollection
//        response = try {
//            api.createNodes(request)
//        } catch (e: ApiException) {
//            e.printStackTrace()
//            throw SDKException.fromApiException(e)
//        }
//    }
//
//    private fun newPolicy(
//        nodeId: String?,
//        action: ServiceResourcePolicyAction
//    ): ServiceResourcePolicy {
//        val policy = ServiceResourcePolicy()
//        policy.setSubject("user:" + transport.username)
//        policy.setResource(nodeId)
//        policy.setEffect(ServiceResourcePolicyPolicyEffect.ALLOW)
//        policy.setAction(action)
//        return policy
//    }
//
//    operator fun get(transport: Transport): CellsClient {
//        return CellsClient(transport, s3Client)
//    }
//
//    /* Transfer methods that use S3 */
//    @Deprecated("")
//    private fun fromURL(url: URL): String {
//        return url.toString().replace(" ", "%20")
//    }
//
//    @Throws(SDKException::class)
//    private fun upload(
//        f: File,
//        mime: String,
//        ws: String,
//        path: String,
//        name: String,
//        tpl: ProgressListener
//    ) {
//        try {
//            FileInputStream(f).use { `in` ->
//                upload(
//                    `in`,
//                    f.length(),
//                    mime,
//                    ws,
//                    path,
//                    name,
//                    true,
//                    tpl
//                )
//            }
//        } catch (e: FileNotFoundException) {
//            throw SDKException.notFound(e)
//        } catch (e: IOException) {
//            val msg = "Could not upload to $ws$path/$name"
//            throw SDKException(ErrorCodes.con_write_failed, msg, e)
//        }
//    }
//
//    @Throws(SDKException::class)
//    private fun getNodeUuid(ws: String, file: String): String? {
//        val api = TreeServiceApi(authenticatedClient())
//        return try {
//            val (node) = api.headNode(FileNodeUtils.toTreeNodePath(ws, file))
//            node!!.uuid
//        } catch (e: ApiException) {
//            throw SDKException.fromApiException(e)
//        }
//    }
//
//    @Throws(SDKException::class)
//    private fun authenticatedClient(): ApiClient {
//        return transport.authenticatedClient()
//    }
//
//    /**
//     * This is necessary until min version is 24: we cannot use the consumer pattern:
//     * public void listChildren(String fullPath, Consumer&lt;TreeNode&gt; consumer) throws SDKException {
//     * ... consumer.onNode(nodes.next());
//     */
//    interface TreeNodeHandler {
//        fun onNode(node: TreeNode?)
//    }

    companion object {
        private const val logTag = "CellsClient"
        fun toTreeNodeInfo(node: TreeNode): TreeNodeInfo {
            val isLeaf = node.type === TreeNodeType.LEAF
            val size: Long = node.propertySize?.toLong() ?: -1
            val lastEdit = node.mtime!!.toLong()
            return TreeNodeInfo(node.etag!!, node.path!!, isLeaf, size, lastEdit)
        }
    }
}