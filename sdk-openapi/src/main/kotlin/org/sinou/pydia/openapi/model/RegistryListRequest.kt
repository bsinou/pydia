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

import org.sinou.pydia.openapi.model.RegistryOptions

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param adjacentsOptions 
 * @param options 
 */


data class RegistryListRequest (

    @Json(name = "adjacentsOptions")
    val adjacentsOptions: RegistryOptions? = null,

    @Json(name = "options")
    val options: RegistryOptions? = null

) {


}

