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

import org.sinou.pydia.openapi.model.TreeQuery

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param details 
 * @param from 
 * @param query 
 * @param propertySize 
 * @param sortDirDesc 
 * @param sortField 
 * @param statFlags 
 */


data class TreeSearchRequest (

    @Json(name = "Details")
    val details: kotlin.Boolean? = null,

    @Json(name = "From")
    val from: kotlin.Int? = null,

    @Json(name = "Query")
    val query: TreeQuery? = null,

    @Json(name = "Size")
    val propertySize: kotlin.Int? = null,

    @Json(name = "SortDirDesc")
    val sortDirDesc: kotlin.Boolean? = null,

    @Json(name = "SortField")
    val sortField: kotlin.String? = null,

    @Json(name = "StatFlags")
    val statFlags: kotlin.collections.List<kotlin.Long>? = null

)

