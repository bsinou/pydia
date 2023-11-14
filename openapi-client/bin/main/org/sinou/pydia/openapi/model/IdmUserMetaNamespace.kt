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

import org.sinou.pydia.openapi.model.ServiceResourcePolicy

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param indexable 
 * @param jsonDefinition 
 * @param label 
 * @param namespace 
 * @param order 
 * @param policies 
 * @param policiesContextEditable 
 */


data class IdmUserMetaNamespace (

    @Json(name = "Indexable")
    val indexable: kotlin.Boolean? = null,

    @Json(name = "JsonDefinition")
    val jsonDefinition: kotlin.String? = null,

    @Json(name = "Label")
    val label: kotlin.String? = null,

    @Json(name = "Namespace")
    val namespace: kotlin.String? = null,

    @Json(name = "Order")
    val order: kotlin.Int? = null,

    @Json(name = "Policies")
    val policies: kotlin.collections.List<ServiceResourcePolicy>? = null,

    @Json(name = "PoliciesContextEditable")
    val policiesContextEditable: kotlin.Boolean? = null

)

