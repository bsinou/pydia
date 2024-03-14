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

import org.sinou.pydia.openapi.model.ListLogRequestLogFormat

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * ListLogRequest launches a parameterised query in the log repository and streams the results.
 *
 * @param format 
 * @param page 
 * @param query 
 * @param propertySize 
 */


data class LogListLogRequest (

    @Json(name = "Format")
    val format: ListLogRequestLogFormat? = ListLogRequestLogFormat.JSON,

    @Json(name = "Page")
    val page: kotlin.Int? = null,

    @Json(name = "Query")
    val query: kotlin.String? = null,

    @Json(name = "Size")
    val propertySize: kotlin.Int? = null

)

