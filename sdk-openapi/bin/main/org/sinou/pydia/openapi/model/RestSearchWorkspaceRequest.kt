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

import org.sinou.pydia.openapi.model.IdmWorkspaceSingleQuery
import org.sinou.pydia.openapi.model.RestResourcePolicyQuery
import org.sinou.pydia.openapi.model.ServiceOperationType

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param countOnly 
 * @param groupBy 
 * @param limit 
 * @param offset 
 * @param operation 
 * @param queries 
 * @param resourcePolicyQuery 
 */


data class RestSearchWorkspaceRequest (

    @Json(name = "CountOnly")
    val countOnly: kotlin.Boolean? = null,

    @Json(name = "GroupBy")
    val groupBy: kotlin.Int? = null,

    @Json(name = "Limit")
    val limit: kotlin.String? = null,

    @Json(name = "Offset")
    val offset: kotlin.String? = null,

    @Json(name = "Operation")
    val operation: ServiceOperationType? = ServiceOperationType.OR,

    @Json(name = "Queries")
    val queries: kotlin.collections.List<IdmWorkspaceSingleQuery>? = null,

    @Json(name = "ResourcePolicyQuery")
    val resourcePolicyQuery: RestResourcePolicyQuery? = null

)
