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

import org.sinou.pydia.openapi.model.ResourcePolicyQueryQueryType

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param type 
 * @param userId 
 */


data class RestResourcePolicyQuery (

    @Json(name = "Type")
    val type: ResourcePolicyQueryQueryType? = ResourcePolicyQueryQueryType.cONTEXT,

    @Json(name = "UserId")
    val userId: kotlin.String? = null

)

