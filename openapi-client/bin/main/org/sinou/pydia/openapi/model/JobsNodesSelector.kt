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

import org.sinou.pydia.openapi.model.ServiceQuery

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param all 
 * @param clearInput 
 * @param collect 
 * @param description 
 * @param fanOutInput 
 * @param label 
 * @param pathes 
 * @param query 
 * @param timeout 
 */


data class JobsNodesSelector (

    @Json(name = "All")
    val all: kotlin.Boolean? = null,

    @Json(name = "ClearInput")
    val clearInput: kotlin.Boolean? = null,

    @Json(name = "Collect")
    val collect: kotlin.Boolean? = null,

    @Json(name = "Description")
    val description: kotlin.String? = null,

    @Json(name = "FanOutInput")
    val fanOutInput: kotlin.Boolean? = null,

    @Json(name = "Label")
    val label: kotlin.String? = null,

    @Json(name = "Pathes")
    val pathes: kotlin.collections.List<kotlin.String>? = null,

    @Json(name = "Query")
    val query: ServiceQuery? = null,

    @Json(name = "Timeout")
    val timeout: kotlin.String? = null

)

