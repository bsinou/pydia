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


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param label 
 * @param nodeUuid 
 * @param uuid 
 */


data class RestBackgroundJobResult (

    @Json(name = "Label")
    val label: kotlin.String? = null,

    @Json(name = "NodeUuid")
    val nodeUuid: kotlin.String? = null,

    @Json(name = "Uuid")
    val uuid: kotlin.String? = null

)

