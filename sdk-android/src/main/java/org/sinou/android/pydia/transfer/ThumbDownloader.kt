package org.sinou.android.sandbox.kotlin.coroutines

import android.util.Log
import com.google.gson.Gson
import com.pydio.cells.api.Client
import com.pydio.cells.api.ErrorCodes
import com.pydio.cells.api.SDKException
import com.pydio.cells.api.SdkNames
import com.pydio.cells.api.ui.FileNode
import com.pydio.cells.transport.StateID
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.sinou.android.pydia.room.browse.TreeNodeDB
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.coroutines.coroutineContext


class ThumbDownloader(val client: Client, val nodeDB: TreeNodeDB, val filesDir: File) {

    private val tag = "ThumbDownloader"

    private val doneChannel = Channel<Boolean>()
    private val queue = Channel<String>()

    init {
        // filesDir.mkdirs()
    }

    private fun download(encodedState: String) {
        val state = StateID.fromId(encodedState)

        var rNode = nodeDB.treeNodeDao().getNode(encodedState)
        if (rNode == null) {
            // No node found, aborting
            Log.w(tag, "No node found for $state, aborting thumb DL")
            return
        }
        // Prepare a "light" FileNode that is used to get the thumbnail
        // This might be refactored once we have reached MVP
        var node = FileNode()
        node.setProperty(SdkNames.NODE_PROPERTY_WORKSPACE_SLUG, state.workspace)
        node.setProperty(SdkNames.NODE_PROPERTY_PATH, state.file)
        node.setProperty(SdkNames.NODE_PROPERTY_META_JSON_ENCODED, Gson().toJson(rNode.meta))

        val thumbTargetPath = filesDir.absolutePath + File.separator + state.fileName + ".jpg"
        val target = File(thumbTargetPath as String)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(target)
            client.getPreviewData(node, 100, out)
        } catch (se: SDKException) { // Could not retrieve thumb, failing silently for the end user
            Log.e(tag, "Could not retrieve thumb for " + state + ": " + se.message)
        } catch (ioe: IOException) {
            // TODO Could not write the thumb in the local cache, we notify the user
            Log.e(
                tag,
                "could not write newly downloaded thumb to the local device for "
                        + state + ": " + ioe.message
            )
        }
    }

    suspend fun orderThumbDL(url: String) {
        println("DL Thumb for $url")
        queue.send(url)
    }

    suspend fun allDone() {
        doneChannel.send(true)
    }

    suspend fun processThumbDL() {
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

    fun initialize() = runBlocking(block = {
        launch { waitForDone() }
        launch { processThumbDL() }
    })

    suspend fun waitForDone() {
        for (msg in doneChannel) {
            println("Finished processing the queue, exiting...")
            queue.close()
            doneChannel.close()
            break
        }
        coroutineContext.cancelChildren()
    }

}
