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

import org.sinou.pydia.openapi.model.RegistryDao
import org.sinou.pydia.openapi.model.RegistryEdge
import org.sinou.pydia.openapi.model.RegistryGeneric
import org.sinou.pydia.openapi.model.RegistryNode
import org.sinou.pydia.openapi.model.RegistryServer
import org.sinou.pydia.openapi.model.RegistryService

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param adjacents 
 * @param dao 
 * @param edge 
 * @param generic 
 * @param id 
 * @param metadata 
 * @param name 
 * @param node 
 * @param server 
 * @param service 
 */


data class RegistryItem (

    @Json(name = "adjacents")
    val adjacents: kotlin.collections.List<RegistryItem>? = null,

    @Json(name = "dao")
    val dao: RegistryDao? = null,

    @Json(name = "edge")
    val edge: RegistryEdge? = null,

    @Json(name = "generic")
    val generic: RegistryGeneric? = null,

    @Json(name = "id")
    val id: kotlin.String? = null,

    @Json(name = "metadata")
    val metadata: kotlin.collections.Map<kotlin.String, kotlin.String>? = null,

    @Json(name = "name")
    val name: kotlin.String? = null,

    @Json(name = "node")
    val node: RegistryNode? = null,

    @Json(name = "server")
    val server: RegistryServer? = null,

    @Json(name = "service")
    val service: RegistryService? = null

) {


}

