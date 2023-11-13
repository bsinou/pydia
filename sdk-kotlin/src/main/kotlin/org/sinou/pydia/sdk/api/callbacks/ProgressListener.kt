package org.sinou.pydia.sdk.api.callbacks

/**
 * Callback interface to manage long running tasks
 */
interface ProgressListener {
    /**
     * @param processed is the amount of data that has already been processed (usually a number of bytes).
     * @return a message indicating the cause if the client want to force stop the long running task.
     */
    fun onProgress(processed: Long): String?
}