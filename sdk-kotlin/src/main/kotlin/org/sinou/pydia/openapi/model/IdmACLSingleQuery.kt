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

import org.sinou.pydia.openapi.model.IdmACLAction

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param actions 
 * @param nodeIDs 
 * @param roleIDs 
 * @param workspaceIDs 
 * @param not 
 */


data class IdmACLSingleQuery (

    @Json(name = "Actions")
    val actions: kotlin.collections.List<IdmACLAction>? = null,

    @Json(name = "NodeIDs")
    val nodeIDs: kotlin.collections.List<kotlin.String>? = null,

    @Json(name = "RoleIDs")
    val roleIDs: kotlin.collections.List<kotlin.String>? = null,

    @Json(name = "WorkspaceIDs")
    val workspaceIDs: kotlin.collections.List<kotlin.String>? = null,

    @Json(name = "not")
    val not: kotlin.Boolean? = null

)
