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

import org.sinou.pydia.openapi.model.RestShareLinkAccessType
import org.sinou.pydia.openapi.model.RestShareLinkTargetUser
import org.sinou.pydia.openapi.model.ServiceResourcePolicy
import org.sinou.pydia.openapi.model.TreeNode

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param accessEnd 
 * @param accessStart 
 * @param currentDownloads 
 * @param description 
 * @param label 
 * @param linkHash 
 * @param linkUrl 
 * @param maxDownloads 
 * @param passwordRequired 
 * @param permissions 
 * @param policies 
 * @param policiesContextEditable 
 * @param restrictToTargetUsers 
 * @param rootNodes 
 * @param targetUsers 
 * @param userLogin 
 * @param userUuid 
 * @param uuid 
 * @param viewTemplateName 
 */


data class RestShareLink (

    @Json(name = "AccessEnd")
    val accessEnd: kotlin.String? = null,

    @Json(name = "AccessStart")
    val accessStart: kotlin.String? = null,

    @Json(name = "CurrentDownloads")
    val currentDownloads: kotlin.String? = null,

    @Json(name = "Description")
    val description: kotlin.String? = null,

    @Json(name = "Label")
    val label: kotlin.String? = null,

    @Json(name = "LinkHash")
    val linkHash: kotlin.String? = null,

    @Json(name = "LinkUrl")
    val linkUrl: kotlin.String? = null,

    @Json(name = "MaxDownloads")
    val maxDownloads: kotlin.String? = null,

    @Json(name = "PasswordRequired")
    val passwordRequired: kotlin.Boolean? = null,

    @Json(name = "Permissions")
    val permissions: kotlin.collections.List<RestShareLinkAccessType>? = null,

    @Json(name = "Policies")
    val policies: kotlin.collections.List<ServiceResourcePolicy>? = null,

    @Json(name = "PoliciesContextEditable")
    val policiesContextEditable: kotlin.Boolean? = null,

    @Json(name = "RestrictToTargetUsers")
    val restrictToTargetUsers: kotlin.Boolean? = null,

    @Json(name = "RootNodes")
    val rootNodes: kotlin.collections.List<TreeNode>? = null,

    @Json(name = "TargetUsers")
    val targetUsers: kotlin.collections.Map<kotlin.String, RestShareLinkTargetUser>? = null,

    @Json(name = "UserLogin")
    val userLogin: kotlin.String? = null,

    @Json(name = "UserUuid")
    val userUuid: kotlin.String? = null,

    @Json(name = "Uuid")
    val uuid: kotlin.String? = null,

    @Json(name = "ViewTemplateName")
    val viewTemplateName: kotlin.String? = null

)
