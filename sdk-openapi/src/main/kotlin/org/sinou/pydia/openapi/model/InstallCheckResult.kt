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
 * @param jsonResult 
 * @param name 
 * @param success 
 */


data class InstallCheckResult (

    @Json(name = "JsonResult")
    val jsonResult: kotlin.String? = null,

    @Json(name = "Name")
    val name: kotlin.String? = null,

    @Json(name = "Success")
    val success: kotlin.Boolean? = null

) {


}

