package org.sinou.android.pydia.transfer

import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.services.FileService
import org.sinou.android.pydia.services.TransferService
import kotlin.coroutines.coroutineContext

class FileDownloader : KoinComponent {

    // private val tag = FileDownloader::class.java.simpleName

    private val fileService: FileService by inject()
    private val transferService: TransferService by inject()

    private val doneChannel = Channel<Boolean>()
    private val queue = Channel<String>()

    private var dlJob = Job()
    private val dlScope = CoroutineScope(Dispatchers.IO + dlJob)

    private suspend fun download(encodedState: String) {

        val state = StateID.fromId(encodedState)
        val result = transferService.prepareDownload(state, AppNames.LOCAL_FILE_TYPE_OFFLINE)

        if (result.first != null && Str.empty(result.second)) {
            transferService.downloadFile(result.first!!)
        }

//        val rNode = nodeDB.treeNodeDao().getNode(encodedState)
//        if (rNode == null) {
//            // No node found, aborting
//            Log.w(tag, "No node found for $state, aborting file DL")
//            return
//        }
//        // Prepare a "light" FileNode that is used to get the thumbnail
//        // This will be refactored once we have reached MVP
//        val node = FileNode()
//        node.setProperty(SdkNames.NODE_PROPERTY_WORKSPACE_SLUG, state.workspace)
//        node.setProperty(SdkNames.NODE_PROPERTY_PATH, state.file)
//
//        var out: FileOutputStream? = null
//        try {
//            val tp = targetPath(state)
//            val targetFile = File(tp)
//            targetFile.parentFile.mkdirs()
//            out = FileOutputStream(targetFile)
//            client.download(state.workspace, state.file, out, null)
//
//            rNode.localFilePath = tp
//            nodeDB.treeNodeDao().update(rNode)
//        } catch (se: SDKException) { // Could not retrieve file, failing silently for the end user
//            Log.e(tag, "Could not retrieve file for " + state + ": " + se.message)
//        } catch (ioe: IOException) {
//            // TODO Could not write the file in the local fs, we should notify the user
//            Log.e(
//                tag,
//                "could not write newly downloaded file to the local device for "
//                        + state + ": " + ioe.message
//            )
//        } finally {
//            IoHelpers.closeQuietly(out)
//        }
    }

    private fun targetPath(state: StateID): String {
        return fileService.getLocalPathFromState(state, AppNames.LOCAL_FILE_TYPE_OFFLINE)
    }

    suspend fun orderFileDL(url: String) {
        println("DL File for $url")
        queue.send(url)
    }

    suspend fun allDone() {
        doneChannel.send(true)
    }

    private suspend fun processFileDL() {
        for (msg in queue) { // iterate over incoming messages
            when (msg) {
                "done" -> {
                    println("Received done message, forwarding to done channel.")
                    doneChannel.send(true)
                    return
                }
                else -> {
                    download(msg)
                }
            }
        }
    }

    init {
        initialize()
    }

    private fun initialize() {
        dlScope.launch { waitForDone() }
        dlScope.launch { processFileDL() }
    }

    private suspend fun waitForDone() {
        for (msg in doneChannel) {
            println("Finished processing the queue, exiting...")
            queue.close()
            doneChannel.close()
            break
        }
        coroutineContext.cancelChildren()
    }
}
