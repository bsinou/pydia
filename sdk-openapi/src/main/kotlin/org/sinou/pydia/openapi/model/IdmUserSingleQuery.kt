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

import org.sinou.pydia.openapi.model.IdmNodeType

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param attributeAnyValue 
 * @param attributeName 
 * @param attributeValue 
 * @param connectedSince 
 * @param fullPath 
 * @param groupPath 
 * @param hasProfile 
 * @param hasRole 
 * @param login 
 * @param nodeType 
 * @param password 
 * @param recursive 
 * @param uuid 
 * @param not 
 */


data class IdmUserSingleQuery (

    @Json(name = "AttributeAnyValue")
    val attributeAnyValue: kotlin.Boolean? = null,

    @Json(name = "AttributeName")
    val attributeName: kotlin.String? = null,

    @Json(name = "AttributeValue")
    val attributeValue: kotlin.String? = null,

    @Json(name = "ConnectedSince")
    val connectedSince: kotlin.String? = null,

    @Json(name = "FullPath")
    val fullPath: kotlin.String? = null,

    @Json(name = "GroupPath")
    val groupPath: kotlin.String? = null,

    @Json(name = "HasProfile")
    val hasProfile: kotlin.String? = null,

    @Json(name = "HasRole")
    val hasRole: kotlin.String? = null,

    @Json(name = "Login")
    val login: kotlin.String? = null,

    @Json(name = "NodeType")
    val nodeType: IdmNodeType? = IdmNodeType.UNKNOWN,

    @Json(name = "Password")
    val password: kotlin.String? = null,

    @Json(name = "Recursive")
    val recursive: kotlin.Boolean? = null,

    @Json(name = "Uuid")
    val uuid: kotlin.String? = null,

    @Json(name = "not")
    val not: kotlin.Boolean? = null

) {


}

