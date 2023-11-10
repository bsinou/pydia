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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param nodes 
 * @param recursive 
 * @param removePermanently 
 */


data class RestDeleteNodesRequest (

    @Json(name = "Nodes")
    val nodes: kotlin.collections.List<TreeNode>? = null,

    @Json(name = "Recursive")
    val recursive: kotlin.Boolean? = null,

    @Json(name = "RemovePermanently")
    val removePermanently: kotlin.Boolean? = null

)

