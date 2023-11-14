package org.sinou.pydia.client.core.transfer

import android.util.Log
import org.sinou.pydia.client.core.AppNames
import org.sinou.pydia.client.core.services.JobService
import org.sinou.pydia.client.core.services.TransferService
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.transport.StateID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Manage the various jobs and queues to request downloads and then perform then in background
 * while updating the calling job to be able to follow progress.
 */
class FileDownloader(
    private val stateID: StateID,
    private val parentJobID: Long,
) : KoinComponent {

    private val logTag = "FileDownloader"

    private val jobService: JobService by inject()
    private val transferService: TransferService by inject()

    private var dlJob = Job()
    private val dlScope = CoroutineScope(Dispatchers.IO + dlJob)

    // TODO there must be a cleaner way to perform the join.
    private lateinit var doneJob: Job

    private val queue = Channel<String>()
    private val doneChannel = Channel<Boolean>()

    private var totalInBytes = 0L
    private var progressInBytes = 0L

    // Local variables to debounce persistence of progress in the Room DB
    private var lastIncrementalProgress = 0L
    private var lastIncrementalTotal = 0L
    private val totalChannel = Channel<Long>()
    private val progressChannel = Channel<Long>()

    private var isFailed = false

    /** Enqueue a new download */
    suspend fun orderDL(encodedState: String, type: String, sizeInBytes: Long = 0L) {
        Log.d(logTag, "DL $type for $encodedState")
        totalChannel.send(
            when {
                sizeInBytes > 0 -> sizeInBytes
                type == AppNames.LOCAL_FILE_TYPE_THUMB -> TransferService.thumbSize
                type == AppNames.LOCAL_FILE_TYPE_PREVIEW -> TransferService.previewSize
                else -> 0L // This should never happen
            }
        )
        queue.send(encodeModel(encodedState, type))
    }

    /** Inform the downloader that no more downloads are to be done */
    suspend fun walkingDone() {
        queue.send("done")
    }

    /** Tweak to wait that all jobs are terminated */
    suspend fun manualJoin() {
        Log.i(logTag, "Waiting for the doneJob to terminate")
        doneJob.join()
    }

    fun isFailed(): Boolean {
        return isFailed
    }

    init {
        initialize()
    }

    /** Internal code */

    private suspend fun processDownloads() {
        for (msg in queue) { // iterate over incoming messages
            when (msg) {
                "done" -> {
                    Log.e(logTag, "######################################################")
                    Log.e(logTag, "####  DONE message received #######")

                    Log.i(logTag, "Received done msg, finalizing and forwarding to done channel")
//                    finalizeJob()
                    doneChannel.send(true)
                    return
                }

                else -> {
                    if (!isFailed) {
                        Log.d(logTag, "Processing DL for $msg")
                        download(msg)
                    }
                }
            }
        }
    }

    private suspend fun download(encoded: String) {
        val (stateId, type) = decodeModel(encoded)
        try {
            jobService.incrementProgress(parentJobID, 0, stateId.fileName)
            transferService.getFileForDiff(stateId, type, parentJobID, progressChannel)
        } catch (e: SDKException) {
            val errMsg = "could not download $type for $stateId, error #${e.code}: ${e.message}"
            Log.w(logTag, errMsg)
            isFailed = true
            jobService.failed(parentJobID, errMsg)
            jobService.e(logTag, errMsg, "Job #$parentJobID")
        }
    }

    private suspend fun finalizeJob() {
        // We assume all downloads have been done at this time
        lastIncrementalTotal = totalInBytes
        persistTotal(totalInBytes)
        lastIncrementalProgress = progressInBytes
        persistProgress(progressInBytes)
        progressChannel.close()
        totalChannel.close()
    }

    private fun initialize() {
        dlScope.launch { manageTotal() }
        dlScope.launch { manageProgress() }
        doneJob = dlScope.launch { waitForDone() }
        dlScope.launch { processDownloads() }
    }

    private suspend fun manageTotal() {
        for (msg in totalChannel) {
            totalInBytes += msg
            // TODO not very clean, we only notify downstream when the increment is large enough
            if (totalInBytes > lastIncrementalTotal * 1.02) {
                lastIncrementalTotal = totalInBytes
                persistTotal(totalInBytes)
            }
        }
    }

    private suspend fun manageProgress() {
        for (msg in progressChannel) {
            progressInBytes += msg
            // Manual debounce
            if (progressInBytes > lastIncrementalProgress * 1.02) {
                lastIncrementalProgress = progressInBytes
                persistProgress(progressInBytes)
            }
        }
    }

    private suspend fun persistTotal(total: Long) {
        jobService.updateById(parentJobID) { currJob ->
            currJob.total = total
            currJob
        }
    }

    private suspend fun persistProgress(progress: Long) {
        jobService.updateById(parentJobID) { currJob ->
            currJob.progress = progress
            currJob
        }
    }

    private suspend fun waitForDone() {
        for (msg in doneChannel) {
            Log.i(logTag, "Finished Walking the queue, waiting for the download to happen...")
            val waiter = dlScope.launch {
                var running = true
                while (this.isActive && running) {
                    delay(10000L)
                    val rTransfers =
                        transferService.getRunningTransfersForJob(stateID.account(), parentJobID)
                    running = rTransfers.isNotEmpty()
                    Log.d(
                        logTag,
                        "... Still waiting for the DL for JobID #${parentJobID}, we still have ${rTransfers.size} running transfers"
                    )
                }
                Log.i(logTag, "Finished processing the queue, exiting...")
                finalizeJob()
                queue.close()
                doneChannel.close()
            }
            waiter.join()
            break
        }
    }

    private fun encodeModel(encodedState: String, type: String): String {
        return "$type:$encodedState"
    }

    private fun decodeModel(model: String): Pair<StateID, String> {
        val type = model.substring(0, model.indexOf(":"))
        val encoded = model.substring(model.indexOf(":") + 1)
        return StateID.safeFromId(encoded) to type
    }
}
