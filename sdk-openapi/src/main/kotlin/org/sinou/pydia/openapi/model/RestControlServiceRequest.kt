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

import org.sinou.pydia.openapi.model.CtlServiceCommand

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param command 
 * @param nodeName 
 * @param serviceName 
 */


data class RestControlServiceRequest (

    @Json(name = "Command")
    val command: CtlServiceCommand? = CtlServiceCommand.START,

    @Json(name = "NodeName")
    val nodeName: kotlin.String? = null,

    @Json(name = "ServiceName")
    val serviceName: kotlin.String? = null

) {


}

