package org.sinou.pydia.client.core.db.accounts

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.sinou.pydia.client.core.db.CellsConverters
import org.sinou.pydia.sdk.api.SdkNames
import org.sinou.pydia.sdk.api.ui.WorkspaceNode
import org.sinou.pydia.sdk.transport.StateID
import java.util.Properties

@Entity(tableName = "workspaces")
@TypeConverters(CellsConverters::class)
data class RWorkspace(

    @PrimaryKey
    @ColumnInfo(name = "encoded_state") val encodedState: String,

    @ColumnInfo(name = "slug") val slug: String,

    @ColumnInfo(name = "type") val type: String,

    @ColumnInfo(name = "label") val label: String? = null,

    @ColumnInfo(name = "description") val description: String? = null,

    @ColumnInfo(name = "remote_mod_ts") var remoteModificationTS: Long,

    @ColumnInfo(name = "last_check_ts") var lastCheckTS: Long = 0L,

    @ColumnInfo(name = "meta") val meta: Properties,

    @ColumnInfo(name = "sort_name") var sortName: String? = null,

    @ColumnInfo(name = "thumb") var thumbFilename: String? = null,
) {

    fun getStateID(): StateID {
        return StateID.safeFromId(encodedState)
    }

    companion object {
        private const val logTag = "RWorkspace"

        fun createChild(parentID: StateID, wsNode: WorkspaceNode): RWorkspace {
            val wsState = parentID.withPath("/${wsNode.slug}")
            return toRWorkspace(wsState, wsNode)
        }

        private fun toRWorkspace(stateID: StateID, wsNode: WorkspaceNode): RWorkspace {
            try {
                val node = RWorkspace(
                    encodedState = stateID.id,
                    slug = wsNode.slug,
                    type = wsNode.type,
                    label = wsNode.label,
                    description = wsNode.description,
                    // TODO can we retrieve remote mod TS?
                    remoteModificationTS = 0L,
                    meta = Properties(), //  wsNode.properties,
                )

                // Add a technical name to easily have a canonical sorting by default,
                // that is: My Files, Normal Wss, Cells.
                // TODO Should we make this case insensitive ?
                node.sortName = when (node.type) {
                    SdkNames.WS_TYPE_PERSONAL -> "2_${node.label}"
                    SdkNames.WS_TYPE_CELL -> "8_${node.label}"
                    else -> "5_${node.label}"
                }
                return node

            } catch (e: java.lang.Exception) {
                Log.w(logTag, "could not create RWorkspace for ${stateID}: ${e.message}")
                throw e
            }
        }
    }
}

//fun List<RWorkspace>.asDomainModel(): List<WorkspaceNode> {
//    return map {
//        WorkspaceNode (
//            // must be done via properties..
//            slug = it.slug,
//            workspaceType = it.type ,
//            label = it.label,
//            description = it.description,
//        )
//
//    }
//}