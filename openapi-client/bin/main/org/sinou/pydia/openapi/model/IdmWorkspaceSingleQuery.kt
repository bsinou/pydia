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

import org.sinou.pydia.openapi.model.IdmWorkspaceScope

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param attributeName 
 * @param attributeValue 
 * @param hasAttribute 
 * @param lastUpdated 
 * @param description 
 * @param label 
 * @param not 
 * @param scope 
 * @param slug 
 * @param uuid 
 */


data class IdmWorkspaceSingleQuery (

    @Json(name = "AttributeName")
    val attributeName: kotlin.String? = null,

    @Json(name = "AttributeValue")
    val attributeValue: kotlin.String? = null,

    @Json(name = "HasAttribute")
    val hasAttribute: kotlin.String? = null,

    @Json(name = "LastUpdated")
    val lastUpdated: kotlin.String? = null,

    @Json(name = "description")
    val description: kotlin.String? = null,

    @Json(name = "label")
    val label: kotlin.String? = null,

    @Json(name = "not")
    val not: kotlin.Boolean? = null,

    @Json(name = "scope")
    val scope: IdmWorkspaceScope? = IdmWorkspaceScope.aNY,

    @Json(name = "slug")
    val slug: kotlin.String? = null,

    @Json(name = "uuid")
    val uuid: kotlin.String? = null

)

