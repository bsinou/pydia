package org.sinou.android.pydia.services

import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.db.browse.RTreeNode
import org.sinou.android.pydia.utils.asFormattedString
import org.sinou.android.pydia.utils.getCurrentDateTime
import java.io.File

class FileService(private val accountService: AccountService) {

    private val fileServiceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + fileServiceJob)

    private val appCacheDir = CellsApp.instance.cacheDir.absolutePath
    private val appFileDir = CellsApp.instance.filesDir.absolutePath

    fun prepareTree(stateID: StateID) = serviceScope.launch {
        val dirName = accountService.sessions[stateID.accountId]?.dirName
            ?: throw IllegalStateException("No record found for $stateID")

        val cacheDir = CellsApp.instance.cacheDir.absolutePath + SEP + dirName
        File(cacheDir).mkdirs()
        File(cacheDir + SEP + AppNames.THUMB_PARENT_DIR).mkdir()
        // File(cacheDir + SEP + AppNames.CACHED_FILE_PARENT_DIR).mkdir()
        val filesDir = CellsApp.instance.filesDir.absolutePath + SEP + dirName
        File(filesDir).mkdirs()
        File(filesDir + SEP + AppNames.OFFLINE_FILE_PARENT_DIR).mkdirs()
        File(filesDir + SEP + AppNames.CACHED_FILE_PARENT_DIR).mkdir()
        File(filesDir + SEP + AppNames.TRANSFER_PARENT_DIR).mkdir()
    }

    fun cleanFileCacheFor(stateID: StateID) = serviceScope.launch {
        val dirName = accountService.sessions[stateID.accountId]?.dirName
            ?: throw IllegalStateException("No record found for $stateID")

        val cache = File(CellsApp.instance.cacheDir.absolutePath + SEP + dirName)
        if (cache.exists()) {
            cache.deleteRecursively()
        }

        val tmpCache = File(dataPath(stateID, AppNames.LOCAL_FILE_TYPE_CACHE))
        if (tmpCache.exists()) {
            tmpCache.deleteRecursively()
        }

    }

    fun cleanAllLocalFiles(stateID: StateID) = serviceScope.launch {
        cleanFileCacheFor(stateID)

        val dirName = accountService.sessions[stateID.accountId]?.dirName
            ?: throw IllegalStateException("No record found for $stateID")

        val files = File(CellsApp.instance.filesDir.absolutePath + SEP + dirName)
        if (files.exists()) {
            files.deleteRecursively()
        }
    }

    fun dataDir(stateID: StateID, type: String): File {
        return File(dataPath(stateID, type))
    }

    fun dataPath(stateID: StateID, type: String): String {
        val dirName = accountService.sessions[stateID.accountId]?.dirName
            ?: throw IllegalStateException("No record found for $stateID")
        val middle = SEP + dirName + SEP
        return when (type) {
            AppNames.LOCAL_FILE_TYPE_THUMB ->
                appCacheDir + middle + AppNames.THUMB_PARENT_DIR
            // TODO we cannot put this in the default cache folder for now:
            //   we do not know how to configure the file provider to allow access to this location
            //   for external viewing
            AppNames.LOCAL_FILE_TYPE_CACHE ->
                appFileDir + middle + AppNames.CACHED_FILE_PARENT_DIR
            // Same with the transfer folder, see:
            // https://developer.android.com/training/data-storage/shared/media#open-file-descriptor
            AppNames.LOCAL_FILE_TYPE_TRANSFER ->
                appFileDir + middle + AppNames.TRANSFER_PARENT_DIR
            AppNames.LOCAL_FILE_TYPE_OFFLINE ->
                appFileDir + middle + AppNames.OFFLINE_FILE_PARENT_DIR
            else -> throw IllegalStateException("Unknown file type: $type")
        }
    }

    fun getAccountBasePath(stateID: StateID, type: String): String {
        val dirName = accountService.sessions[stateID.accountId]?.dirName
            ?: throw IllegalStateException("No record found for $stateID")
        val middle = SEP + dirName
        return when (type) {
            AppNames.LOCAL_DIR_TYPE_CACHE ->
                appCacheDir + middle
            AppNames.LOCAL_DIR_TYPE_FILE ->
                appFileDir + middle
            else -> throw IllegalStateException("Unknown base folder type: $type")
        }
    }

    fun getThumbPath(item: RTreeNode): String? {
        val stat = StateID.fromId(item.encodedState)
        return if (Str.empty(item.thumbFilename)) {
            null
        } else {
            "${dataPath(stat, AppNames.LOCAL_FILE_TYPE_THUMB)}${SEP}${item.thumbFilename}"
        }
    }

    /**
     * Get the path to a local resource, if it exists:
     * typically, it returns null for the thumb file if it has not yet been downloaded
     * and persisted from the remote server
     * */
    @Throws(java.lang.IllegalStateException::class)
    fun getLocalPath(item: RTreeNode, type: String): String? {
        val stat = StateID.fromId(item.encodedState)
        return when (type) {
            AppNames.LOCAL_FILE_TYPE_CACHE
            -> "${dataPath(stat, type)}${stat.file}"
            AppNames.LOCAL_FILE_TYPE_TRANSFER
            -> "${dataPath(stat, type)}${stat.file}"
            AppNames.LOCAL_FILE_TYPE_OFFLINE
            -> "${dataPath(stat, type)}${stat.file}"
            AppNames.LOCAL_FILE_TYPE_THUMB
            -> "${dataPath(stat, type)}${stat.file}"
            else -> throw IllegalStateException("Unable to generate local path for $type file: ${item.encodedState} ")
        }
    }

    fun createImageFile(stateID: StateID): File {
        val timestamp = getCurrentDateTime().asFormattedString("yyMMdd_HHmmss")
        val imgPath = dataPath(stateID, AppNames.LOCAL_FILE_TYPE_TRANSFER)
        // TODO do we really want a lazy creation for this base folder? or rather rely on a tree
        //    initialisation when the account is created
        File(imgPath).mkdirs()
        return File("${imgPath}${SEP}IMG_${timestamp}.jpg")

        // Would be safer but with an ugly name :(
        //        return File.createTempFile(
//            "IMG_${timestamp}_", /* prefix */
//            ".jpg", /* suffix */
//            storageDir /* directory */
//        )
    }


    companion object {

        private val SEP: String = File.separator

//        @Volatile
//        private var INSTANCE: FileService? = null
//
//        fun getInstance(): FileService {
//
//            INSTANCE?.let { return it }
//
//            synchronized(this) {
//                val instance = FileService()
//                INSTANCE = instance
//                return instance
//            }
//        }
    }
}
