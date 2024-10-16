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
 * @param buildRevision 
 * @param buildStamp 
 * @param endpoints 
 * @param packageLabel 
 * @param packageType 
 * @param version 
 */


data class RestDiscoveryResponse (

    @Json(name = "BuildRevision")
    val buildRevision: kotlin.String? = null,

    @Json(name = "BuildStamp")
    val buildStamp: kotlin.Int? = null,

    @Json(name = "Endpoints")
    val endpoints: kotlin.collections.Map<kotlin.String, kotlin.String>? = null,

    @Json(name = "PackageLabel")
    val packageLabel: kotlin.String? = null,

    @Json(name = "PackageType")
    val packageType: kotlin.String? = null,

    @Json(name = "Version")
    val version: kotlin.String? = null

) {


}

