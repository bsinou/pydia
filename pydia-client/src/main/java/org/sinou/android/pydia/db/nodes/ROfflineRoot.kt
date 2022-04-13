package org.sinou.android.pydia.db.nodes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pydio.cells.api.SdkNames
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.AppNames

@Entity(tableName = "offline_roots")
data class ROfflineRoot(

    @PrimaryKey
    @ColumnInfo(name = "uuid") val uuid: String,

    @ColumnInfo(name = "encoded_state") val encodedState: String,

//    @ColumnInfo(name = "mime") val mime: String,
//
//    @ColumnInfo(name = "name") val name: String,

    @ColumnInfo(name = "status") var status: String,

    @ColumnInfo(name = "local_mod_ts") var localModificationTS: Long = 0L,

    @ColumnInfo(name = "last_check_ts") var lastCheckTS: Long = 0L,

    @ColumnInfo(name = "message") var message: String?,

    @ColumnInfo(name = "sort_name") var sortName: String? = null,

    // Can be: internal or external, optionally with an index
    @ColumnInfo(name = "storage_key") var localFileType: String = AppNames.LOCAL_FILE_TYPE_NONE,
) {

    fun getStateID(): StateID {
        return StateID.fromId(encodedState)
    }

//    fun isFolder(): Boolean {
//        return mime == SdkNames.NODE_MIME_FOLDER
//                || mime == SdkNames.NODE_MIME_WS_ROOT
//                || mime == SdkNames.NODE_MIME_RECYCLE
//    }

    companion object {
        fun fromTreeNode(treeNode: RTreeNode): ROfflineRoot {
            return ROfflineRoot(
                encodedState = treeNode.encodedState,
                uuid = treeNode.uuid,
//                mime = treeNode.mime,
//                name = treeNode.name,
                status = "new",
                localModificationTS = 0,
                lastCheckTS = 0,
                message = null,
                sortName = treeNode.sortName,
                // TODO: we only support storage in the app files dir for the time being.
                localFileType = "internal",
            )
        }
    }
}
