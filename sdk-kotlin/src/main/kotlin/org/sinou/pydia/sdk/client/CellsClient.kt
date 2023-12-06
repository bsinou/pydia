package org.sinou.pydia.sdk.client

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.sinou.pydia.openapi.api.JobsServiceApi
import org.sinou.pydia.openapi.api.SearchServiceApi
import org.sinou.pydia.openapi.api.ShareServiceApi
import org.sinou.pydia.openapi.api.TreeServiceApi
import org.sinou.pydia.openapi.api.UserMetaServiceApi
import org.sinou.pydia.openapi.api.UserServiceApi
import org.sinou.pydia.openapi.infrastructure.ClientException
import org.sinou.pydia.openapi.infrastructure.ServerException
import org.sinou.pydia.openapi.model.IdmSearchUserMetaRequest
import org.sinou.pydia.openapi.model.IdmUpdateUserMetaRequest
import org.sinou.pydia.openapi.model.IdmUserMeta
import org.sinou.pydia.openapi.model.RestBulkMetaResponse
import org.sinou.pydia.openapi.model.RestCreateNodesRequest
import org.sinou.pydia.openapi.model.RestDeleteNodesRequest
import org.sinou.pydia.openapi.model.RestGetBulkMetaRequest
import org.sinou.pydia.openapi.model.RestPutShareLinkRequest
import org.sinou.pydia.openapi.model.RestRestoreNodesRequest
import org.sinou.pydia.openapi.model.RestSearchResults
import org.sinou.pydia.openapi.model.RestShareLink
import org.sinou.pydia.openapi.model.RestShareLinkAccessType
import org.sinou.pydia.openapi.model.RestUserBookmarksRequest
import org.sinou.pydia.openapi.model.RestUserJobRequest
import org.sinou.pydia.openapi.model.ServiceResourcePolicy
import org.sinou.pydia.openapi.model.ServiceResourcePolicyAction
import org.sinou.pydia.openapi.model.TreeNode
import org.sinou.pydia.openapi.model.TreeNodeType
import org.sinou.pydia.openapi.model.TreeQuery
import org.sinou.pydia.openapi.model.TreeSearchRequest
import org.sinou.pydia.openapi.model.UpdateUserMetaRequestUserMetaOp
import org.sinou.pydia.sdk.api.Client
import org.sinou.pydia.sdk.api.ErrorCodes
import org.sinou.pydia.sdk.api.Registry
import org.sinou.pydia.sdk.api.S3Client
import org.sinou.pydia.sdk.api.S3Names
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.SdkNames
import org.sinou.pydia.sdk.api.Transport
import org.sinou.pydia.sdk.api.ui.PageOptions
import org.sinou.pydia.sdk.api.ui.WorkspaceNode
import org.sinou.pydia.sdk.client.model.DocumentRegistry
import org.sinou.pydia.sdk.client.model.TreeNodeInfo
import org.sinou.pydia.sdk.transport.CellsTransport
import org.sinou.pydia.sdk.transport.StateID
import org.sinou.pydia.sdk.utils.FileNodeUtils
import org.sinou.pydia.sdk.utils.FileNodeUtils.toTreeNodePath
import org.sinou.pydia.sdk.utils.IoHelpers
import org.sinou.pydia.sdk.utils.Log
import org.xml.sax.SAXException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.Properties
import javax.xml.parsers.ParserConfigurationException

class CellsClient(transport: Transport, private val s3Client: S3Client) : Client, SdkNames {

    private val transport: CellsTransport
    private val gson = Gson()

    private fun userServiceApi(): UserServiceApi {
        val (u, c) = transport.apiConf()
        return UserServiceApi(u, c)
    }

    private fun userMetaServiceApi(): UserMetaServiceApi {
        val (u, c) = transport.apiConf()
        return UserMetaServiceApi(u, c)
    }

    private fun treeServiceApi(): TreeServiceApi {
        val (u, c) = transport.apiConf()
        return TreeServiceApi(u, c)
    }

    private fun jobsServiceApi(): JobsServiceApi {
        val (u, c) = transport.apiConf()
        return JobsServiceApi(u, c)
    }

    private fun searchServiceApi(): SearchServiceApi {
        val (u, c) = transport.apiConf()
        return SearchServiceApi(u, c)
    }

    private fun shareServiceApi(): ShareServiceApi {
        val (u, c) = transport.apiConf()
        return ShareServiceApi(u, c)
    }


    init {
        this.transport = transport as CellsTransport
    }

    @Throws(SDKException::class)
    override fun stillAuthenticated(): Boolean {
        return try {
            userServiceApi().getUser(transport.username)
            true
        } catch (e: ClientException) {
            Log.e(
                logTag, "SDK error #" + e.statusCode
                        + " while checking auth state for " + StateID.fromId(transport.id)
            )
            false
        } catch (e: SDKException) {
            Log.w(logTag, "${e.code} (${e.message}) when checking auth for ${transport.stateID}")
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
    override fun getWorkspaceList(handler: (WorkspaceNode) -> Unit) {
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
            for (node in registry.getWorkspaces()) {
                handler(node)
                // TODO check if we have to skip some of the workspaces at this point
//                if (!Arrays.asList<Any>(defaultExcludedWorkspaces).contains(node.accessType)) {
//                    handler.onNode(node)
//                }
            }
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
        handler: (TreeNode) -> Unit
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
        val response: RestBulkMetaResponse
        // var nextPageOptions: PageOptions
        try {
            response = treeServiceApi().bulkStatNodes(request)
            val nextPageOptions = response.pagination?.let { pag ->
                PageOptions(
                    limit = pag.limit ?: 0,
                    offset = pag.nextOffset ?: -1,
                    total = pag.total ?: 0,
                    currentPage = pag.currentPage ?: 0,
                    totalPages = pag.totalPages ?: 0
                )
            } ?: run {
                val size = response.nodes?.size ?: 0
                PageOptions(
                    limit = size,
                    offset = 0,
                    total = size,
                    currentPage = 1,
                    totalPages = 1
                )
            }

            response.nodes?.let { nodes ->
                nodes.forEach { handler(it) }
            }
            return nextPageOptions

        } catch (e: ServerException) {
            val msg = "Could not list: " + e.message
            throw SDKException(e.statusCode, msg, e)
        }

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

        try {
            treeServiceApi().createNodes(request)
        } catch (e: ServerException) {
            e.printStackTrace()
            throw SDKException.fromServerException(e)
        }
    }

    //    @Throws(SDKException::class)
    override fun delete(slug: String, paths: Array<String>, removePermanently: Boolean) {
        val nodes = paths.map { TreeNode(path = FileNodeUtils.toTreeNodePath(slug, it)) }
        val request = RestDeleteNodesRequest(nodes = nodes, removePermanently = removePermanently)
        try {
            treeServiceApi().deleteNodes(request)
        } catch (e: ServerException) {
            e.printStackTrace()
            throw SDKException.fromServerException(e)
        }
    }

    //    @Throws(SDKException::class)
//    override fun statNode(file: String): TreeNode? {
//        try {
//            val response = treeServiceApi().headNode(file)
//            return response.node
//        } catch (e: ServerException) {
//            throw SDKException.fromServerException(e)
//        } catch (e: Exception) {
//            Log.e(logTag, "unexpected error when doing stat node for [$file]")
//            e.printStackTrace()
//            throw SDKException(ErrorCodes.internal_error, "Cannot stat $file", e)
//        }
//    }
    @Throws(SDKException::class)
    override fun statNode(stateID: StateID): TreeNode? {
        val path = if (stateID.slug.isNullOrEmpty()) {
            throw SDKException(ErrorCodes.illegal_argument, "cannot stat at $stateID, define a WS")
        } else if (stateID.file.isNullOrEmpty()) {
            "${stateID.slug}/"
        } else {
            "${stateID.slug}/${stateID.file}"
        }
        try {
            val response = treeServiceApi().headNode(path)
            return response.node
        } catch (e: ServerException) {
            throw SDKException.fromServerException(e)
        } catch (e: Exception) {
            Log.e(logTag, "unexpected error when doing stat node for [$path]")
            e.printStackTrace()
            throw SDKException(ErrorCodes.internal_error, "Cannot stat $path", e)
        }
    }

    @Throws(SDKException::class)
    override fun getThumbnail(
        stateID: StateID,
        uuid: String,
        props: Properties,
        parentFolder: File,
        dim: Int
    ): String? {

        val filename = getThumbFilename(uuid, props, dim)
        if (filename.isNullOrEmpty()) {
            Log.w(logTag, "No thumbnail is defined for $stateID")
            return null
        }
        var out: OutputStream? = null
        try {
            if (!parentFolder.exists()) {
                if (!parentFolder.mkdirs()) {
                    throw SDKException(
                        ErrorCodes.internal_error,
                        "could not create folder for thumbs at " + parentFolder.absolutePath
                    )
                }
            }
            val targetFile = File(parentFolder.absolutePath + File.separator + filename)
            out = FileOutputStream(targetFile)
            // Download API expect a full path starting with a slash (a.k.a a file, not a filename)
            val file = "/$filename"
            download(S3Names.PYDIO_S3_THUMBSTORE_PREFIX, file, out, null)
        } catch (e: IOException) {
            throw SDKException.conReadFailed("could not get thumb for $stateID", e)
        } finally {
            IoHelpers.closeQuietly(out)
        }
        return filename
    }

    /**
     * Warning, this expect a file (with a leading slash), not a file name
     */
    @Throws(SDKException::class)
    override fun download(
        ws: String,
        file: String,
        target: OutputStream,
        onProgress: ((Long) -> String?)?
    ): Long {
        var input: InputStream? = null
        return try {
            val preSignedURL = s3Client.getDownloadPreSignedURL(ws, file)
            val serverUrl = try {
                transport.server.newURL(preSignedURL.path).withQuery(preSignedURL.query)
            } catch (e: MalformedURLException) { // This should never happen with a pre-signed.
                throw SDKException(
                    ErrorCodes.internal_error,
                    "Invalid pre-signed path: " + preSignedURL.path,
                    e
                )
            }
            val con = transport.withUserAgent(serverUrl.openConnection())
            con.connect()
            input = con.inputStream
            onProgress?.let {
                IoHelpers.pipeReadWithIncrementalProgress(input, target, it)
            } ?: run {
                IoHelpers.pipeRead(input, target)
            }
        } catch (e: IOException) {
            if (e.message!!.contains("ENOSPC")) { // no space left on device
                throw SDKException.noSpaceLeft(e)
            }
            throw SDKException.conReadFailed("could not download from $ws$file", e)
        } finally {
            IoHelpers.closeQuietly(input)
        }
    }

    /*
     * If no thumb is defined or if it is currently processing, we return null.
     * If we find only one thumb, we choose this one. Otherwise we return the smaller thumb that has at least required size.
     *
     * Thumb names follow a tacit standard that is: <node_uuid>-<size>.<type>
     * @param currNode
     * @param dim
     * @return
     * @throws SDKException
     */
    @Throws(SDKException::class)
    private fun getThumbFilename(uid: String, meta: Properties, dim: Int): String? {
        var thumbName: String? = null

        val imgThumbsStr = meta[SdkNames.META_KEY_THUMB_PARENT] as? String ?: return null

        // TODO centralize this
        val objType = object : TypeToken<Map<String, Any>>() {}.type
        val thumbData: Map<String, *> = gson.fromJson(imgThumbsStr, objType)
        if (thumbData.containsKey(SdkNames.META_KEY_THUMB_PROCESSING)
            && !(thumbData[SdkNames.META_KEY_THUMB_PROCESSING] as Boolean)
            && thumbData.containsKey(SdkNames.META_KEY_THUMBS)
        ) {
            val thumbs = (thumbData[SdkNames.META_KEY_THUMBS] as? ArrayList<Map<String, Any>>)
                ?: return null
            for (currThumb in thumbs) {
                val size = (currThumb[SdkNames.META_KEY_THUMB_SIZE] as? Double)?.toInt() ?: 0
                val format = currThumb[SdkNames.META_KEY_THUMB_FORMAT] as? String ?: "jpg"
                val currName = "$uid-$size.$format"
                if (thumbName == null) {
                    thumbName = currName
                }
                if (size > 0 && size >= dim) {
                    thumbName = currName
                    break
                }
            }
        }
        return thumbName
    }

    @Throws(SDKException::class)
    override fun getNodeMeta(ws: String, file: String): TreeNode? {
        val request = RestGetBulkMetaRequest(
            allMetaProviders = true,
            nodePaths = listOf(FileNodeUtils.toTreeNodePath(ws, file))
        )
        val response: RestBulkMetaResponse = try {
            treeServiceApi().bulkStatNodes(request)
        } catch (e: ClientException) {
            e.printStackTrace()
            throw SDKException(e)
        }
        if (response.nodes.isNullOrEmpty()) {
            Log.w(logTag, "No node found for " + FileNodeUtils.toTreeNodePath(ws, file))
            return null
        }
        return response.nodes?.let { it[0] }
    }

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
    @Throws(SDKException::class)
    override fun search(ws: String, dir: String, searchedText: String, h: (TreeNode) -> Unit) {
        val query = TreeQuery(
            fileName = searchedText,
            pathPrefix = listOf(ws + dir)
        )
        val request = TreeSearchRequest(
            propertySize = 50,
            query = query
        )
        val results: RestSearchResults = try {
            searchServiceApi().nodes(request)
        } catch (e: ClientException) {
            throw SDKException(e)
        }
        val nodes = results.results
        if (nodes != null) {
            for (treeNode in nodes) {
                h(treeNode)
            }
        }
    }

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
    @Throws(SDKException::class)
    override fun copy(ws: String, files: Array<String>, folder: String) {
        val nodes = mutableListOf<String>()
        for (file in files) {
            // String path = "/" + ws + file;
            val path = ws + file
            nodes.add(path)
        }
        val o = mutableMapOf<String, Any>()
        o["nodes"] = nodes.toTypedArray()
        o["target"] = ws + folder
        o["targetParent"] = true
        val request = RestUserJobRequest(
            jsonParameters = gson.toJson(o)
        )
        // request.setJobName(CellsNames.JOB_ID_COPY)
        try {
            jobsServiceApi().userCreateJob(SdkNames.JOB_ID_COPY, request)
        } catch (e: ClientException) {
            e.printStackTrace()
            throw SDKException(e)
        }
    }

    @Throws(SDKException::class)
    override fun move(ws: String, files: Array<String>, dstFolder: String) {

        val nodes = mutableListOf<String>()
        for (file in files) {
            // String path = "/" + ws + file;
            val path = ws + file
            nodes.add(path)
        }
        val o = mutableMapOf<String, Any>()
        o["nodes"] = nodes.toTypedArray()
        o["target"] = ws + dstFolder
        o["targetParent"] = true

        val request = RestUserJobRequest(
            jsonParameters = gson.toJson(o)
        )

        // request.setJobName(CellsNames.JOB_ID_MOVE)
        try {
            jobsServiceApi().userCreateJob(SdkNames.JOB_ID_MOVE, request)
        } catch (e: ClientException) {
            e.printStackTrace()
            throw SDKException(e)
        }
    }

    @Throws(SDKException::class)
    override fun rename(ws: String, srcFile: String, newName: String) {
        val nodes = listOf(ws + srcFile)

        val parent = File(srcFile).parentFile.path
        val dstFile = if ("/" == parent) {
            parent + newName
        } else {
            "$parent/$newName"
        }
        val targetFile = ws + dstFile
        val o = mutableMapOf<String, Any>()
        o["nodes"] = nodes.toTypedArray()
        o["target"] = targetFile
        o["targetParent"] = false
        val request = RestUserJobRequest(
            jsonParameters = gson.toJson(o)
        )

        try {
            jobsServiceApi().userCreateJob(SdkNames.JOB_ID_MOVE, request)
        } catch (e: ClientException) {
            e.printStackTrace()
            throw SDKException(e)
        }
    }


    /**
     * val nodes: MutableList<TreeNode> = ArrayList()
     *         for (file in files) {
     *             val node: TreeNode = TreeNode(
     *                 uuid = ,
     *                 path = ,
     *             ).uuid(file.id).path(file.path)
     *             nodes.add(node)
     *         }
     */
    @Throws(SDKException::class)
    override fun restore(ws: String, nodes: List<TreeNode>?) {
        val request = RestRestoreNodesRequest(
            nodes = nodes
        )
        try {
            treeServiceApi().restoreNodes(request)
        } catch (e: ClientException) {
            e.printStackTrace()
            throw SDKException(e)
        }
    }

    @Throws(SDKException::class)
    override fun emptyRecycleBin(ws: String) {
        delete(ws, arrayOf("/recycle_bin"), true)
    }
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

    @Throws(SDKException::class)
    override fun getBookmarks(h: (TreeNode) -> Unit) {
        val request = RestUserBookmarksRequest()
        try {
            val (nodes) = userMetaServiceApi().userBookmarks(request)
            if (nodes == null) {
                return
            }
            for (node in nodes) {
                toMultipleNode(h, node)
            }
        } catch (e: ClientException) {
            e.printStackTrace()
            throw SDKException(e)
        }
    }

    //    private fun toMultipleNode(nodes: MutableList<FileNode>, treeNode: TreeNode) {
//        toMultipleNode({ node: Node? ->
//            if (node is FileNode) nodes.add(
//                node
//            )
//        }, treeNode)
//    }
//
    private fun toMultipleNode(h: (TreeNode) -> Unit, node: TreeNode) {
        try {
            val sources = node.appearsIn
            if (sources != null) {
                // A node can appear in various workspaces (typically when referenced in a cell)
                // Yet the server sends back only one node with the specific "appears in" property,
                // We then have to return a node for each bookmark to the local cache
                for (twrp in sources) {
                    var path = twrp.path
                    if (path.isNullOrEmpty()) {
                        Log.i(logTag, "Got an empty path for: " + node.path)
                        path = "/"
                    } else if (!path.startsWith("/")) {
                        path = "/$path"
                    }
// FIXME this has been broken when moving to kotlin
//                        fileNode.setProperty(SdkNames.NODE_PROPERTY_WORKSPACE_SLUG, twrp.wsSlug)
//                        fileNode.setProperty(SdkNames.NODE_PROPERTY_PATH, path)
//                        fileNode.setProperty(
//                            SdkNames.NODE_PROPERTY_FILENAME,
//                            FileNodeUtils.getNameFromPath(path)
//                        )
                    h(node)
                }
            }
        } catch (e: NullPointerException) {
            Log.e(logTag, "###############################################################")
            Log.e(logTag, "Could node create FileNode for " + node.path + ", skipping")
            e.printStackTrace()
        }
    }

    @Throws(SDKException::class)
    override fun bookmark(slug: String, file: String, isBookmarked: Boolean) {
        if (isBookmarked) {
            bookmark(slug, file)
        } else {
            unbookmark(slug, file)
        }
    }

    @Throws(SDKException::class)
    override fun bookmark(ws: String, file: String) {
        bookmark(getNodeUuid(ws, file))
    }

    @Throws(SDKException::class)
    fun bookmark(uuid: String?) {

        val userMeta = IdmUserMeta(
            uuid = uuid,
            namespace = "bookmark",
            jsonValue = "true",
            policies = listOf(
                ServiceResourcePolicy(action = ServiceResourcePolicyAction.rEAD),
                ServiceResourcePolicy(action = ServiceResourcePolicyAction.wRITE),
                ServiceResourcePolicy(action = ServiceResourcePolicyAction.oWNER),
            )
        )

        val request = IdmUpdateUserMetaRequest(
            operation = UpdateUserMetaRequestUserMetaOp.pUT,
            metaDatas = listOf(userMeta)
        )
        try {
            userMetaServiceApi().updateUserMeta(request)
        } catch (e: ClientException) {
            e.printStackTrace()
            throw SDKException(
                ErrorCodes.api_error,
                "could not update bookmark user-meta for uuid $uuid: " + e.message,
                e
            )
        }
    }

    @Throws(SDKException::class)
    override fun unbookmark(ws: String, file: String) {
        try {
            val api = userMetaServiceApi()

            // Retrieve bookmark user meta with node UUID
            val searchRequest = IdmSearchUserMetaRequest(
                namespace = "bookmark",
                nodeUuids = getNodeUuid(ws, file)?.let { listOf(it) }
            )

            val (metadatas) = api.searchUserMeta(searchRequest)

            // Delete corresponding user meta
            val request = IdmUpdateUserMetaRequest(
                operation = UpdateUserMetaRequestUserMetaOp.dELETE,
                metaDatas = metadatas
            )
            api.updateUserMeta(request)
        } catch (e: ClientException) {
            throw SDKException(e)
        }
    }

    //
    @Throws(SDKException::class)
    override fun share(
        workspace: String,
        file: String,
        wsLabel: String,
        isFolder: Boolean,
        wsDescription: String,
        password: String?,
        expiration: Int,
        download: Int,
        canPreview: Boolean,
        canDownload: Boolean
    ): String {
        val uuid = getNodeUuid(workspace, file)

        val n = TreeNode(
            uuid = uuid
        )
        val permissions: MutableList<RestShareLinkAccessType> = ArrayList()
        if (canPreview) {
            permissions.add(RestShareLinkAccessType.preview)
        }
        if (canDownload) {
            permissions.add(RestShareLinkAccessType.download)
        }
        val sl = RestShareLink(
            policiesContextEditable = true,
            permissions = permissions,
            rootNodes = listOf(n),
            description = wsDescription,
            label = wsLabel,
            viewTemplateName = "pydio_unique_strip"
        )

        val request = RestPutShareLinkRequest(
            createPassword = password,
            passwordEnabled = !password.isNullOrEmpty(),
            shareLink = sl,
        )

        return try {
            val restShareLink = shareServiceApi().putShareLink(request)
            transport.server.url() + restShareLink.linkUrl
        } catch (e: ClientException) {
            throw SDKException.fromClientException(e)
        }
    }

    @Throws(SDKException::class)
    override fun share(
        stateID: StateID,
        label: String,
        desc: String,
        password: String?,
        canPreview: Boolean,
        canDownload: Boolean
    ): String {

        val targetRemote = statNode(stateID)
            ?: throw SDKException("Cannot share node $stateID: it has disappeared on remote")
        val meta: Map<String, String> = targetRemote.metaStore ?: HashMap()
        val templateName = if ("true" == meta["is_image"]) {
            SdkNames.SHARE_TEMPLATE_GALLERY
        } else {
            SdkNames.SHARE_TEMPLATE_FOLDER_LIST
        }
        val permissions: MutableList<RestShareLinkAccessType> = ArrayList()
        if (canPreview) {
            permissions.add(RestShareLinkAccessType.preview)
        }
        if (canDownload) {
            permissions.add(RestShareLinkAccessType.download)
        }

        val n = TreeNode(uuid = getNodeUuid(stateID))
        val shareLink = RestShareLink(
            policiesContextEditable = true,
            permissions = permissions,
            rootNodes = listOf(n),
            description = desc,
            label = label,
//            viewTemplateName = "pydio_unique_strip"
            viewTemplateName = templateName
        )

        val hasPwd = !password.isNullOrEmpty()
        val request = RestPutShareLinkRequest(
            createPassword = if (hasPwd) password else null,
            passwordEnabled = hasPwd,
            shareLink = shareLink
        )

        return try {
            val (_, _, _, _, _, _, linkUrl) = shareServiceApi().putShareLink(request)
            transport.server.url() + linkUrl
        } catch (e: ClientException) {
            throw SDKException.fromClientException(e)
        }
    }

    @Throws(SDKException::class)
    override fun unshare(workspace: String, shareUuid: String) {
        try {
            shareServiceApi().deleteShareLink(shareUuid)
        } catch (e: ClientException) {
            throw SDKException.fromClientException(e)
        }
    }

    @Throws(SDKException::class)
    override fun getShareAddress(ws: String, shareID: String): String {
        return try {
            val (_, _, _, _, _, _, linkUrl) = shareServiceApi().getShareLink(shareID)
            getFullLinkAddress(linkUrl, transport.server.url())
        } catch (e: ClientException) {
            throw SDKException.fromClientException(e)
        }
    }

    @Throws(SDKException::class)
    private fun getFullLinkAddress(linkUrl: String?, defaultPrefix: String): String {
        return try {

            // FIXME we should not use java.net package
            val url = URL(linkUrl)
            // Passed URL is valid we directly use this
            url.toString()
        } catch (e: MalformedURLException) {
            if (!linkUrl!!.startsWith("/")) {
                // Log.e(logTag, "Could not parse link URL: [" + linkUrl + "]");
                throw SDKException(
                    ErrorCodes.unexpected_response,
                    "Public link [$linkUrl] is not valid",
                    e
                )
            }
            defaultPrefix + linkUrl
        }
    }
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
    @Throws(SDKException::class)
    private fun getNodeUuid(ws: String, file: String): String? {
        return try {
            treeServiceApi().headNode(FileNodeUtils.toTreeNodePath(ws, file)).node?.uuid
        } catch (e: ClientException) {
            throw SDKException("Cannot get UUID for $ws$file", e)
        }
    }

    @Throws(SDKException::class)
    private fun getNodeUuid(stateID: StateID): String? {
        return try {
            treeServiceApi().headNode(toTreeNodePath(stateID)).node?.uuid
        } catch (e: ClientException) {
            throw SDKException("Cannot get UUID for $stateID", e)
        }
    }

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