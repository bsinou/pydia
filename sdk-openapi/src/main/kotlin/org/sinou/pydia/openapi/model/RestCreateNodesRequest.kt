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
 * @param templateUUID 
 */


data class RestCreateNodesRequest (

    @Json(name = "Nodes")
    val nodes: kotlin.collections.List<TreeNode>? = null,

    @Json(name = "Recursive")
    val recursive: kotlin.Boolean? = null,

    @Json(name = "TemplateUUID")
    val templateUUID: kotlin.String? = null

) {


}

