/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package org.sinou.pydia.openapi.model

import org.sinou.pydia.openapi.model.ActivityObject
import org.sinou.pydia.openapi.model.IdmACL
import org.sinou.pydia.openapi.model.IdmRole
import org.sinou.pydia.openapi.model.IdmUser
import org.sinou.pydia.openapi.model.IdmWorkspace
import org.sinou.pydia.openapi.model.JobsActionOutput
import org.sinou.pydia.openapi.model.ObjectDataSource
import org.sinou.pydia.openapi.model.ProtobufAny
import org.sinou.pydia.openapi.model.TreeNode

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param acls 
 * @param activities 
 * @param dataSources 
 * @param event 
 * @param nodes 
 * @param outputChain 
 * @param roles 
 * @param users 
 * @param workspaces 
 */


data class JobsActionMessage (

    @Json(name = "Acls")
    val acls: kotlin.collections.List<IdmACL>? = null,

    @Json(name = "Activities")
    val activities: kotlin.collections.List<ActivityObject>? = null,

    @Json(name = "DataSources")
    val dataSources: kotlin.collections.List<ObjectDataSource>? = null,

    @Json(name = "Event")
    val event: ProtobufAny? = null,

    @Json(name = "Nodes")
    val nodes: kotlin.collections.List<TreeNode>? = null,

    @Json(name = "OutputChain")
    val outputChain: kotlin.collections.List<JobsActionOutput>? = null,

    @Json(name = "Roles")
    val roles: kotlin.collections.List<IdmRole>? = null,

    @Json(name = "Users")
    val users: kotlin.collections.List<IdmUser>? = null,

    @Json(name = "Workspaces")
    val workspaces: kotlin.collections.List<IdmWorkspace>? = null

) {


}

