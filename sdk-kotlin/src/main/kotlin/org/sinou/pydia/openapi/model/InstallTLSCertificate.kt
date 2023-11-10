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
 * @param cellsRootCA 
 * @param certFile 
 * @param keyFile 
 */


data class InstallTLSCertificate (

    @Json(name = "CellsRootCA")
    val cellsRootCA: kotlin.String? = null,

    @Json(name = "CertFile")
    val certFile: kotlin.String? = null,

    @Json(name = "KeyFile")
    val keyFile: kotlin.String? = null

)

