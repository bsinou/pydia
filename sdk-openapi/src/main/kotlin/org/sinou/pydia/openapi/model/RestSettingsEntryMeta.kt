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
 * @param advanced 
 * @param component 
 * @param iconClass 
 * @param indexed 
 * @param props 
 */


data class RestSettingsEntryMeta (

    @Json(name = "advanced")
    val advanced: kotlin.Boolean? = null,

    @Json(name = "component")
    val component: kotlin.String? = null,

    @Json(name = "icon_class")
    val iconClass: kotlin.String? = null,

    @Json(name = "indexed")
    val indexed: kotlin.collections.List<kotlin.String>? = null,

    @Json(name = "props")
    val props: kotlin.String? = null

) {


}

