package org.sinou.android.pydia.services

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import com.pydio.cells.api.SdkNames
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.IoHelpers
import com.pydio.cells.utils.Str
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.db.runtime.RTransfer
import org.sinou.android.pydia.db.runtime.RuntimeDB
import org.sinou.android.pydia.db.runtime.TransferDao
import java.io.*
import java.util.*

class TransferService(
    private val accountService: AccountService,
    private val fileService: FileService,
    private val runtimeDB: RuntimeDB
) {

    private val tag = TransferService::class.java.simpleName
    private val mimeMap = MimeTypeMap.getSingleton()

    private val transferServiceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + transferServiceJob)

    val activeTransfers: LiveData<List<RTransfer>?> = runtimeDB.transferDao().getActiveTransfers()

    fun enqueueUpload(parentID: StateID, uri: Uri) {
        val cr = CellsApp.instance.contentResolver
        serviceScope.launch {
            copyAndRegister(cr, uri, parentID)?.let {
                serviceScope.launch {
                    uploadOne(it.id)
                }
            }
        }
    }

    fun clearTerminated() {
        serviceScope.launch {
            runtimeDB.transferDao().clearTerminatedTransfers()
        }
    }


    private fun uploadOne(id: String) {
        val uploadRecord = getUploadDao().get(id)
            ?: throw java.lang.IllegalStateException("No upload record found for $id")
        doUpload(uploadRecord)
    }

    private fun doUpload(transferRecord: RTransfer) {
        // Real upload in single part
        var inputStream: InputStream? = null
        try {
            // Mark the upload as started
            transferRecord.startTimestamp = Calendar.getInstance().timeInMillis / 1000L
            getUploadDao().update(transferRecord)
            val fs = CellsApp.instance.fileService
            val state = transferRecord.getStateId()
            var srcPath = fs.getLocalPathFromState(state, AppNames.LOCAL_FILE_TYPE_CACHE)
            var srcFile = File(srcPath)
            inputStream = FileInputStream(srcFile)

            val parent = state.parentFolder()
            accountService.getClient(state).upload(
                inputStream, transferRecord.byteSize,
                transferRecord.mime, parent.workspace, parent.file, state.fileName,
                true
            ) { progressL ->
                transferRecord.progress = progressL
                getUploadDao().update(transferRecord)
                true
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
            getUploadDao().update(transferRecord)
        }
    }


    suspend fun uploadAllNew() {
        serviceScope.launch {
            val uploads = getUploadDao().getAllNew()
            for (one in uploads) {
                serviceScope.launch {
                    doUpload(one)
                }
            }
        }
    }

    fun getUploadDao(): TransferDao {
        return runtimeDB.transferDao()
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
        Log.e(tag, "Enqueuing upload for $filename, MIME: [$mime]")
        mimeMap.getExtensionFromMimeType(mime)?.let {
            // TODO make a better check
            //   - retrieve file extension
            //   - only append if the extension seems to be invalid
            if (!filename.endsWith(it, true)) {
                name += ".$it"
            }
        }

        // FIXME to by-pass permission issues, we make a local copy of the file to upload
        //   in Cells app storage
        val fs = fileService
        val targetStateID = createLocalState(parentID, name as String)
        val localPath = fs.getLocalPathFromState(targetStateID, AppNames.LOCAL_FILE_TYPE_CACHE)
        val localFile = File(localPath)
        localFile.parentFile!!.mkdirs()
        //val localFile = createTargetFile(parentID, name as String)

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = cr.openInputStream(uri)
            outputStream = FileOutputStream(localFile)
            IoHelpers.pipeRead(inputStream, outputStream)

            //                val error = uploadAt(
            //                    StateID.fromId(uploadRecord.targetState),
            //                    uploadRecord.name,
            //                    uploadRecord.byteSize,
            //                    uploadRecord.mime,
            //                    inputStream!!
            //                )
        } catch (ioe: IOException) {
            ioe.printStackTrace()
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
        val fs = CellsApp.instance.fileService
        val targetStateID = createLocalState(parentID, name)
        val tf = File(fs.getLocalPathFromState(targetStateID, AppNames.LOCAL_FILE_TYPE_CACHE))
        tf.parentFile!!.mkdirs()
        return tf
    }

    private fun createLocalState(parentID: StateID, name: String): StateID {
        val fs = CellsApp.instance.fileService
        var parentPath = fs.getLocalPathFromState(parentID, AppNames.LOCAL_FILE_TYPE_CACHE)
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
            newPrefix + name.substring(index + 1, name.length)
        }
    }

    private fun handleWOExt(name: String): String {
        val index = name.lastIndexOf("-")
        return if (index == -1) {
            "${name}-2"
        } else {
            val suffix = name.substring(index + 1, name.length)
            if (isInt(suffix)) {
                val newPrefix = handleWOExt(name.substring(0, index))
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

}
