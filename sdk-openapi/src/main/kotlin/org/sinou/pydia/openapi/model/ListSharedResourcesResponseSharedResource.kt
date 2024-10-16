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

import org.sinou.pydia.openapi.model.RestCell
import org.sinou.pydia.openapi.model.RestShareLink
import org.sinou.pydia.openapi.model.TreeNode

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param cells 
 * @param link 
 * @param node 
 */


data class ListSharedResourcesResponseSharedResource (

    @Json(name = "Cells")
    val cells: kotlin.collections.List<RestCell>? = null,

    @Json(name = "Link")
    val link: RestShareLink? = null,

    @Json(name = "Node")
    val node: TreeNode? = null

) {


}

