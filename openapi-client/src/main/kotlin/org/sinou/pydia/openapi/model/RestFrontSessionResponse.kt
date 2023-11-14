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

import org.sinou.pydia.openapi.model.AuthToken

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param error 
 * @param expireTime 
 * @param JWT 
 * @param redirectTo 
 * @param token 
 * @param trigger 
 * @param triggerInfo 
 */


data class RestFrontSessionResponse (

    @Json(name = "Error")
    val error: kotlin.String? = null,

    @Json(name = "ExpireTime")
    val expireTime: kotlin.Int? = null,

    @Json(name = "JWT")
    val JWT: kotlin.String? = null,

    @Json(name = "RedirectTo")
    val redirectTo: kotlin.String? = null,

    @Json(name = "Token")
    val token: AuthToken? = null,

    @Json(name = "Trigger")
    val trigger: kotlin.String? = null,

    @Json(name = "TriggerInfo")
    val triggerInfo: kotlin.collections.Map<kotlin.String, kotlin.String>? = null

)
