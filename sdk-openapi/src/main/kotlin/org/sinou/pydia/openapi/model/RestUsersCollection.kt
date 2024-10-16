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

import org.sinou.pydia.openapi.model.IdmUser

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param groups 
 * @param total 
 * @param users 
 */


data class RestUsersCollection (

    @Json(name = "Groups")
    val groups: kotlin.collections.List<IdmUser>? = null,

    @Json(name = "Total")
    val total: kotlin.Int? = null,

    @Json(name = "Users")
    val users: kotlin.collections.List<IdmUser>? = null

) {


}

