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

import org.sinou.pydia.openapi.model.JobsAction
import org.sinou.pydia.openapi.model.JobsActionMessage

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param action 
 * @param inputMessage 
 * @param outputMessage 
 */


data class JobsActionLog (

    @Json(name = "Action")
    val action: JobsAction? = null,

    @Json(name = "InputMessage")
    val inputMessage: JobsActionMessage? = null,

    @Json(name = "OutputMessage")
    val outputMessage: JobsActionMessage? = null

)

