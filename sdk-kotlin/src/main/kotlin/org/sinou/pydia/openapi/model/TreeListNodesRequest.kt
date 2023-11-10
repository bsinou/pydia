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

import org.sinou.pydia.openapi.model.TreeNode
import org.sinou.pydia.openapi.model.TreeNodeType

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param ancestors 
 * @param filterType 
 * @param limit 
 * @param node 
 * @param offset 
 * @param recursive 
 * @param sortDirDesc 
 * @param sortField 
 * @param statFlags 
 * @param withCommits 
 * @param withVersions 
 */


data class TreeListNodesRequest (

    @Json(name = "Ancestors")
    val ancestors: kotlin.Boolean? = null,

    @Json(name = "FilterType")
    val filterType: TreeNodeType? = TreeNodeType.uNKNOWN,

    @Json(name = "Limit")
    val limit: kotlin.String? = null,

    @Json(name = "Node")
    val node: TreeNode? = null,

    @Json(name = "Offset")
    val offset: kotlin.String? = null,

    @Json(name = "Recursive")
    val recursive: kotlin.Boolean? = null,

    @Json(name = "SortDirDesc")
    val sortDirDesc: kotlin.Boolean? = null,

    @Json(name = "SortField")
    val sortField: kotlin.String? = null,

    @Json(name = "StatFlags")
    val statFlags: kotlin.collections.List<kotlin.Long>? = null,

    @Json(name = "WithCommits")
    val withCommits: kotlin.Boolean? = null,

    @Json(name = "WithVersions")
    val withVersions: kotlin.Boolean? = null

)

