package org.sinou.android.pydia.services

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import com.pydio.cells.api.SDKException
import com.pydio.cells.api.SdkNames
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.IoHelpers
import com.pydio.cells.utils.Str
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.db.runtime.RTransfer
import org.sinou.android.pydia.db.runtime.RuntimeDB
import org.sinou.android.pydia.db.runtime.TransferDao
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class TransferService(
    private val accountService: AccountService,
    private val nodeService: NodeService,
    private val fileService: FileService,
    private val runtimeDB: RuntimeDB
) {

    private val logTag = TransferService::class.java.simpleName
    private val mimeMap = MimeTypeMap.getSingleton()

    private val transferServiceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + transferServiceJob)

    val activeTransfers: LiveData<List<RTransfer>?> = runtimeDB.transferDao().getActiveTransfers()

    fun enqueueUpload(parentID: StateID, uri: Uri) {
        val cr = CellsApp.instance.contentResolver
        serviceScope.launch {
            copyAndRegister(cr, uri, parentID)?.let {
                uploadOne(it.id)
            }
        }
    }

    fun getLiveRecord(transferUid: Long): LiveData<RTransfer?> {
        return getTransferDao().getLiveById(transferUid)
    }

    suspend fun clearTerminated() = withContext(Dispatchers.IO) {
        runtimeDB.transferDao().clearTerminatedTransfers()
    }

    suspend fun deleteRecord(transferUid: Long) = withContext(Dispatchers.IO) {
        runtimeDB.transferDao().deleteTransfer(transferUid)
    }

    /** DOWNLOADS **/

    /**
     * Centralize client specific actions that should be done **before** launching
     * the real download.
     */
    suspend fun prepareDownload(state: StateID, type: String): Pair<Long?, String?> =
        withContext(Dispatchers.IO) {

            // Retrieve data and sanity check
            var errorMessage: String?
            val rNode = nodeService.getNode(state)
            if (rNode == null) {
                // No node found, aborting
                errorMessage = "No node found for $state, aborting file DL"
                Log.w(logTag, errorMessage)
                return@withContext Pair(-1, errorMessage)
            }

            val localPath = fileService.getLocalPathFromState(state, type)
            val rec = RTransfer.fromState(
                state.id,
                AppNames.TRANSFER_TYPE_DOWNLOAD,
                localPath,
                rNode.size,
                rNode.mime,
            )
            return@withContext Pair(getTransferDao().insert(rec), null)
        }

    /**
     * Performs the real download for the pre-registered transfer record and update
     * both the RTreeNode and RTransfer records depending on the output status.
     */
    suspend fun downloadFile(transferUid: Long): String? = withContext(Dispatchers.IO) {

        var errorMessage: String? = null

        // Retrieve data and sanity check
        val rTransfer = getTransferDao().getById(transferUid) ?: run {
            val msg = "No record found for $transferUid, aborting file DL"
            Log.w(logTag, msg)
            return@withContext msg
        }

        val state = StateID.fromId(rTransfer.encodedState)
        val rNode = nodeService.getNode(state)
        if (rNode == null) {
            // No node found, aborting
            errorMessage = "No node found for $state, aborting file DL"
            Log.w(logTag, errorMessage)
            return@withContext errorMessage
        }

        var out: FileOutputStream? = null
        try {

            Log.d(logTag, "About to download file from $state")

            // Prepare target file
            val targetFile = File(rTransfer.localPath)
            targetFile.parentFile!!.mkdirs()
            out = FileOutputStream(targetFile)

            // Mark the upload as started
            rTransfer.startTimestamp = Calendar.getInstance().timeInMillis / 1000L
            getTransferDao().update(rTransfer)

            // Real transfer
            accountService.getClient(state)
                .download(state.workspace, state.file, out) { progressL ->
                    rTransfer.progress = progressL
                    getTransferDao().update(rTransfer)
                    false
                }

            // Mark the upload as done
            rTransfer.doneTimestamp = Calendar.getInstance().timeInMillis / 1000L
            rTransfer.error = null
            getTransferDao().update(rTransfer)

            // Also stores the target path in the parent node
            // TODO handle the case where the download duration is long enough to enable
            //   end-user to modify (or delete) the corresponding node before it is downloaded
            rNode.localFilePath = rTransfer.localPath
            nodeService.nodeDB(state).treeNodeDao().update(rNode)

        } catch (se: SDKException) { // Could not retrieve file, failing silently for the end user
            errorMessage = "Could not download file for " + state + ": " + se.message
        } catch (ioe: IOException) {
            // TODO Could not write the file in the local fs, we should notify the user
            errorMessage =
                "Could not write file for DL of $state to the local device: ${ioe.message}"
            ioe.printStackTrace()
        } finally {
            IoHelpers.closeQuietly(out)
        }
        if (Str.notEmpty(errorMessage)) {
            rTransfer.doneTimestamp = Calendar.getInstance().timeInMillis / 1000L
            rTransfer.error = errorMessage
            getTransferDao().update(rTransfer)
            Log.e(logTag, errorMessage!!)
        }

        return@withContext errorMessage
    }

    /** UPLOADS **/

    private fun uploadOne(id: String) {
        val uploadRecord = getTransferDao().getByState(id)
            ?: throw IllegalStateException("No transfer record found for $id, cannot upload")
        doUpload(uploadRecord)
    }

    private fun doUpload(transferRecord: RTransfer) {
        // Real upload in single part
        var inputStream: InputStream? = null
        try {
            // Mark the upload as started
            transferRecord.startTimestamp = Calendar.getInstance().timeInMillis / 1000L
            getTransferDao().update(transferRecord)
            val state = transferRecord.getStateId()
            val srcPath = fileService.getLocalPathFromState(state, AppNames.LOCAL_FILE_TYPE_CACHE)
            inputStream = FileInputStream(File(srcPath))

            Log.d(logTag, "... About to upload file to $state")

            val parent = state.parentFolder()
            accountService.getClient(state).upload(
                inputStream, transferRecord.byteSize,
                transferRecord.mime, parent.workspace, parent.file, state.fileName,
                true
            ) { progressL ->
                transferRecord.progress = progressL
                getTransferDao().update(transferRecord)
                false
            }

            transferRecord.error = null
            transferRecord.doneTimestamp = Calendar.getInstance().timeInMillis / 1000L
            // uploadRecord.progress = 100

        } catch (e: Exception) {
            // TODO manage errors correctly
            transferRecord.error = e.message
            e.printStackTrace()
        } finally {
            IoHelpers.closeQuietly(inputStream)
            transferRecord.doneTimestamp = Calendar.getInstance().timeInMillis / 1000L
            getTransferDao().update(transferRecord)
        }
    }

    suspend fun uploadAllNew() {
        serviceScope.launch {
            val uploads = getTransferDao().getAllNew()
            for (one in uploads) {
                serviceScope.launch {
                    doUpload(one)
                }
            }
        }
    }

    /**
     * Does all the dirty work to copy the file from the device to in-app folder
     * and register a new transfer record in the DB
     */
    private fun copyAndRegister(cr: ContentResolver, uri: Uri, parentID: StateID): StateID? {
        var name: String? = null
        // TODO rather throw an exception 5 lines below if we do not have a valid size
        var size: Long = 1
        cr.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            name = cursor.getString(nameIndex)
            size = cursor.getLong(sizeIndex)
        }
        name = name ?: uri.lastPathSegment!!
        if (Str.empty(name)) {
            return null
        }

        val filename = name!!

        // Mime Type
        val mime = cr.getType(uri) ?: SdkNames.NODE_MIME_DEFAULT
        Log.d(logTag, "Enqueuing upload for $filename, MIME: [$mime], size: $size")

        // TODO should we implement a "clean" of the extensions
        //   the code below wasn't good enough and led to files named e.g. "img.JPG.jpg".
        //   Rather doing nothing.
        /*mimeMap.getExtensionFromMimeType(mime)?.let {
            //   - retrieve file extension
            //   - only append if the extension seems to be invalid
            if (!filename.endsWith(it, true)) {
                name += ".$it"
            }
        }*/

        // TODO to by-pass permission issues, we make a local copy of the file to upload
        //   in Cells app storage
        val fs = fileService
        val targetStateID = createLocalState(parentID, name as String)
        val localPath = fs.getLocalPathFromState(targetStateID, AppNames.LOCAL_FILE_TYPE_CACHE)
        val localFile = File(localPath)
        localFile.parentFile!!.mkdirs()

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = cr.openInputStream(uri)
            outputStream = FileOutputStream(localFile)
            IoHelpers.pipeRead(inputStream, outputStream)
        } catch (ioe: IOException) {
            Log.e(logTag, "could not create local copy of $filename: ${ioe.message}")
            ioe.printStackTrace()
            return null
        } finally {
            IoHelpers.closeQuietly(inputStream)
            IoHelpers.closeQuietly(outputStream)
        }

        val rec = RTransfer.fromState(
            targetStateID.id,
            AppNames.TRANSFER_TYPE_UPLOAD,
            localPath,
            size,
            mime
        )
        runtimeDB.transferDao().insert(rec)
        return targetStateID
    }

    private fun createTargetFile(parentID: StateID, name: String): File {
        val targetStateID = createLocalState(parentID, name)
        val localPath =
            fileService.getLocalPathFromState(targetStateID, AppNames.LOCAL_FILE_TYPE_CACHE)
        val tf = File(localPath)
        tf.parentFile!!.mkdirs()
        return tf
    }

    private fun createLocalState(parentID: StateID, name: String): StateID {
        var parentPath = fileService.getLocalPathFromState(parentID, AppNames.LOCAL_FILE_TYPE_CACHE)
        var targetFile = File(File(parentPath), name)
        while (targetFile.exists()) {
            val newName = bumpFileVersion(targetFile.name)
            targetFile = File(targetFile.parentFile!!, newName)
        }
        return parentID.child(targetFile.name)
    }

    private fun bumpFileVersion(name: String): String {
        val index = name.lastIndexOf(".")
        return if (index == -1) {
            handleWOExt(name)
        } else {
            val newPrefix = handleWOExt(name.substring(0, index))
            newPrefix + name.substring(index, name.length)
        }
    }

    private fun handleWOExt(name: String): String {
        val index = name.lastIndexOf("-")
        return if (index == -1) {
            "${name}-2"
        } else {
            val suffix = name.substring(index + 1, name.length)
            if (isInt(suffix)) {
                val newPrefix = name.substring(0, index)
                val newSuffix = suffix.toInt() + 1
                "${newPrefix}-${newSuffix}"
            } else {
                "${name}-2"
            }
        }
    }

    private fun isInt(value: String): Boolean {
        return try {
            value.toInt()
            true
        } catch (ex: java.lang.Exception) {
            false
        }
    }

    private fun getTransferDao(): TransferDao {
        return runtimeDB.transferDao()
    }
}
