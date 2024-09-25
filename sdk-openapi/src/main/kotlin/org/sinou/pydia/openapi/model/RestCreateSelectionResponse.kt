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
 * @param selectionUUID 
 */


data class RestCreateSelectionResponse (

    @Json(name = "Nodes")
    val nodes: kotlin.collections.List<TreeNode>? = null,

    @Json(name = "SelectionUUID")
    val selectionUUID: kotlin.String? = null

) {


}

