package org.sinou.android.pydia.services

import android.content.Context
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sinou.android.pydia.AppNames
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.db.browse.RTreeNode
import java.io.File

class FileService {

    private val fileServiceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + fileServiceJob)


    suspend fun prepareTree(stateID: StateID) = serviceScope.launch {
        val cacheDir = CellsApp.instance.cacheDir.absolutePath + ps + stateID.accountId
        File(cacheDir).mkdirs()
        File(cacheDir + ps + AppNames.THUMB_PARENT_DIR).mkdir()
        File(cacheDir + ps + AppNames.CACHED_FILE_PARENT_DIR).mkdir()
        val filesDir = CellsApp.instance.filesDir.absolutePath + ps + stateID.accountId
        File(filesDir + ps + AppNames.OFFLINE_FILE_PARENT_DIR).mkdirs()
    }

    suspend fun cleanFileCacheFor(stateID: StateID) = serviceScope.launch {
        val cache = File(CellsApp.instance.cacheDir.absolutePath + ps + stateID.accountId)
        if (cache.exists()) {
            cache.deleteRecursively()
        }
    }

    suspend fun cleanAllLocalFiles(stateID: StateID) = serviceScope.launch {
        cleanFileCacheFor(stateID)
        val files = File(CellsApp.instance.filesDir.absolutePath + ps + stateID.accountId)
        if (files.exists()) {
            files.deleteRecursively()
        }
    }


    companion object {

        val ps = File.separator

        @Volatile
        private var INSTANCE: FileService? = null

        fun getInstance(): FileService {

            INSTANCE?.let { return it }

            synchronized(this) {
                val instance = FileService()
                INSTANCE = instance
                return instance
            }
        }
        
        fun dataDir(stateID: StateID, type: String): File {

            return when (type) {
                AppNames.LOCAL_FILE_TYPE_THUMB -> File(
                    CellsApp.instance.cacheDir.absolutePath
                            + ps + stateID.accountId + ps + AppNames.THUMB_PARENT_DIR
                )
                AppNames.LOCAL_FILE_TYPE_CACHE -> File(
                    CellsApp.instance.cacheDir.absolutePath
                            + ps + stateID.accountId + ps + AppNames.CACHED_FILE_PARENT_DIR
                )
                AppNames.LOCAL_FILE_TYPE_OFFLINE -> File(
                    CellsApp.instance.filesDir.absolutePath
                            + ps + stateID.accountId + ps + AppNames.OFFLINE_FILE_PARENT_DIR
                )
                else -> throw IllegalStateException("Unknown file type: $type")
            }
        }

        @Throws(java.lang.IllegalStateException::class)
        fun getLocalPath(item: RTreeNode, type: String): String? {
            val stat = StateID.fromId(item.encodedState)
            return when (type) {
                AppNames.LOCAL_FILE_TYPE_THUMB
                -> if (Str.empty(item.thumbFilename)) {
                    null
                } else {
                    "${dataDir(stat, type)}${File.separator}${item.thumbFilename}"
                }
                AppNames.LOCAL_FILE_TYPE_CACHE
                -> "${dataDir(stat, type)}${stat.file}"
                // TODO offline files are stored twice
                AppNames.LOCAL_FILE_TYPE_OFFLINE
                -> "${dataDir(stat, type)}${stat.file}"
                else -> throw IllegalStateException("Unable to generate local path for $type file: ${item.encodedState} ")
            }
        }

    }
}