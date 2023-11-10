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
 * @param channel 
 * @param currentVersion 
 * @param GOARCH 
 * @param GOOS 
 * @param licenseInfo 
 * @param packageName 
 * @param serviceName 
 */


data class UpdateUpdateRequest (

    @Json(name = "Channel")
    val channel: kotlin.String? = null,

    @Json(name = "CurrentVersion")
    val currentVersion: kotlin.String? = null,

    @Json(name = "GOARCH")
    val GOARCH: kotlin.String? = null,

    @Json(name = "GOOS")
    val GOOS: kotlin.String? = null,

    @Json(name = "LicenseInfo")
    val licenseInfo: kotlin.collections.Map<kotlin.String, kotlin.String>? = null,

    @Json(name = "PackageName")
    val packageName: kotlin.String? = null,

    @Json(name = "ServiceName")
    val serviceName: kotlin.String? = null

)

