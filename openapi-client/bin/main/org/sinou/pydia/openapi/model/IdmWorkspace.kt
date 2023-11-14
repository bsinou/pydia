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
import org.sinou.pydia.openapi.model.ServiceResourcePolicy
import org.sinou.pydia.openapi.model.TreeNode

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * A Workspace is composed of a set of nodes UUIDs and is used to provide accesses to the tree via ACLs.
 *
 * @param attributes 
 * @param description 
 * @param label 
 * @param lastUpdated 
 * @param policies 
 * @param policiesContextEditable 
 * @param rootNodes 
 * @param rootUUIDs 
 * @param scope 
 * @param slug 
 * @param UUID 
 */


data class IdmWorkspace (

    @Json(name = "Attributes")
    val attributes: kotlin.String? = null,

    @Json(name = "Description")
    val description: kotlin.String? = null,

    @Json(name = "Label")
    val label: kotlin.String? = null,

    @Json(name = "LastUpdated")
    val lastUpdated: kotlin.Int? = null,

    @Json(name = "Policies")
    val policies: kotlin.collections.List<ServiceResourcePolicy>? = null,

    @Json(name = "PoliciesContextEditable")
    val policiesContextEditable: kotlin.Boolean? = null,

    @Json(name = "RootNodes")
    val rootNodes: kotlin.collections.Map<kotlin.String, TreeNode>? = null,

    @Json(name = "RootUUIDs")
    val rootUUIDs: kotlin.collections.List<kotlin.String>? = null,

    @Json(name = "Scope")
    val scope: IdmWorkspaceScope? = IdmWorkspaceScope.aNY,

    @Json(name = "Slug")
    val slug: kotlin.String? = null,

    @Json(name = "UUID")
    val UUID: kotlin.String? = null

)

