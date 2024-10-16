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
 * @param any 
 * @param empty 
 * @param subjects 
 */


data class ServiceResourcePolicyQuery (

    @Json(name = "Any")
    val any: kotlin.Boolean? = null,

    @Json(name = "Empty")
    val empty: kotlin.Boolean? = null,

    @Json(name = "Subjects")
    val subjects: kotlin.collections.List<kotlin.String>? = null

) {


}

