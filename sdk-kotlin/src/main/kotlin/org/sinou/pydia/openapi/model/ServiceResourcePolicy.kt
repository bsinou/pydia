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

import org.sinou.pydia.openapi.model.ServiceResourcePolicyAction
import org.sinou.pydia.openapi.model.ServiceResourcePolicyPolicyEffect

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param action 
 * @param effect 
 * @param jsonConditions 
 * @param resource 
 * @param subject 
 * @param id 
 */


data class ServiceResourcePolicy (

    @Json(name = "Action")
    val action: ServiceResourcePolicyAction? = ServiceResourcePolicyAction.aNY,

    @Json(name = "Effect")
    val effect: ServiceResourcePolicyPolicyEffect? = ServiceResourcePolicyPolicyEffect.deny,

    @Json(name = "JsonConditions")
    val jsonConditions: kotlin.String? = null,

    @Json(name = "Resource")
    val resource: kotlin.String? = null,

    @Json(name = "Subject")
    val subject: kotlin.String? = null,

    @Json(name = "id")
    val id: kotlin.String? = null

)

