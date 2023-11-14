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
 * @param acceptEULA 
 * @param email 
 * @param stagingCA 
 */


data class InstallTLSLetsEncrypt (

    @Json(name = "AcceptEULA")
    val acceptEULA: kotlin.Boolean? = null,

    @Json(name = "Email")
    val email: kotlin.String? = null,

    @Json(name = "StagingCA")
    val stagingCA: kotlin.Boolean? = null

)

