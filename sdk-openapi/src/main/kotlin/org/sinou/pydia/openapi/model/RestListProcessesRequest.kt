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
 * @param peerId 
 * @param serviceName 
 */


data class RestListProcessesRequest (

    @Json(name = "PeerId")
    val peerId: kotlin.String? = null,

    @Json(name = "ServiceName")
    val serviceName: kotlin.String? = null

) {


}

