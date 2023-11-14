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

import org.sinou.pydia.openapi.model.RestSettingsAccessRestPolicy

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param description 
 * @param label 
 * @param policies 
 */


data class RestSettingsAccess (

    @Json(name = "Description")
    val description: kotlin.String? = null,

    @Json(name = "Label")
    val label: kotlin.String? = null,

    @Json(name = "Policies")
    val policies: kotlin.collections.List<RestSettingsAccessRestPolicy>? = null

)

