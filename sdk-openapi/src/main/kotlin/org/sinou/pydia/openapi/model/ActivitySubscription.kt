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

import org.sinou.pydia.openapi.model.ActivityOwnerType

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param events 
 * @param objectId 
 * @param objectType 
 * @param userId 
 */


data class ActivitySubscription (

    @Json(name = "Events")
    val events: kotlin.collections.List<kotlin.String>? = null,

    @Json(name = "ObjectId")
    val objectId: kotlin.String? = null,

    @Json(name = "ObjectType")
    val objectType: ActivityOwnerType? = ActivityOwnerType.NODE,

    @Json(name = "UserId")
    val userId: kotlin.String? = null

) {


}

