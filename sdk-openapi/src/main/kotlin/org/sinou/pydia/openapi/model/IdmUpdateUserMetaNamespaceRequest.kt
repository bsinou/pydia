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

import org.sinou.pydia.openapi.model.IdmUserMetaNamespace
import org.sinou.pydia.openapi.model.UpdateUserMetaNamespaceRequestUserMetaNsOp

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param namespaces 
 * @param operation 
 */


data class IdmUpdateUserMetaNamespaceRequest (

    @Json(name = "Namespaces")
    val namespaces: kotlin.collections.List<IdmUserMetaNamespace>? = null,

    @Json(name = "Operation")
    val operation: UpdateUserMetaNamespaceRequestUserMetaNsOp? = UpdateUserMetaNamespaceRequestUserMetaNsOp.PUT

) {


}

