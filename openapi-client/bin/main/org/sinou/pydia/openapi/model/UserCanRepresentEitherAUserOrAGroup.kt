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

import org.sinou.pydia.openapi.model.IdmRole
import org.sinou.pydia.openapi.model.ServiceResourcePolicy

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param attributes 
 * @param groupLabel 
 * @param groupPath 
 * @param isGroup 
 * @param lastConnected 
 * @param oldPassword 
 * @param password 
 * @param policies 
 * @param policiesContextEditable Context-resolved to quickly check if user is editable or not.
 * @param roles 
 * @param uuid 
 */


data class UserCanRepresentEitherAUserOrAGroup (

    @Json(name = "Attributes")
    val attributes: kotlin.collections.Map<kotlin.String, kotlin.String>? = null,

    @Json(name = "GroupLabel")
    val groupLabel: kotlin.String? = null,

    @Json(name = "GroupPath")
    val groupPath: kotlin.String? = null,

    @Json(name = "IsGroup")
    val isGroup: kotlin.Boolean? = null,

    @Json(name = "LastConnected")
    val lastConnected: kotlin.Int? = null,

    @Json(name = "OldPassword")
    val oldPassword: kotlin.String? = null,

    @Json(name = "Password")
    val password: kotlin.String? = null,

    @Json(name = "Policies")
    val policies: kotlin.collections.List<ServiceResourcePolicy>? = null,

    /* Context-resolved to quickly check if user is editable or not. */
    @Json(name = "PoliciesContextEditable")
    val policiesContextEditable: kotlin.Boolean? = null,

    @Json(name = "Roles")
    val roles: kotlin.collections.List<IdmRole>? = null,

    @Json(name = "Uuid")
    val uuid: kotlin.String? = null

)

