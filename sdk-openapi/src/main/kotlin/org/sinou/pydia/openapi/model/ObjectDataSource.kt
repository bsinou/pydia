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

import org.sinou.pydia.openapi.model.ObjectEncryptionMode
import org.sinou.pydia.openapi.model.ObjectStorageType

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param apiKey 
 * @param apiSecret 
 * @param creationDate 
 * @param disabled 
 * @param encryptionKey 
 * @param encryptionMode 
 * @param flatStorage 
 * @param lastSynchronizationDate 
 * @param name 
 * @param objectsBaseFolder 
 * @param objectsBucket 
 * @param objectsHost 
 * @param objectsPort 
 * @param objectsSecure 
 * @param objectsServiceName 
 * @param peerAddress 
 * @param skipSyncOnRestart 
 * @param storageConfiguration 
 * @param storageType 
 * @param versioningPolicyName 
 * @param watch 
 */


data class ObjectDataSource (

    @Json(name = "ApiKey")
    val apiKey: kotlin.String? = null,

    @Json(name = "ApiSecret")
    val apiSecret: kotlin.String? = null,

    @Json(name = "CreationDate")
    val creationDate: kotlin.Int? = null,

    @Json(name = "Disabled")
    val disabled: kotlin.Boolean? = null,

    @Json(name = "EncryptionKey")
    val encryptionKey: kotlin.String? = null,

    @Json(name = "EncryptionMode")
    val encryptionMode: ObjectEncryptionMode? = ObjectEncryptionMode.CLEAR,

    @Json(name = "FlatStorage")
    val flatStorage: kotlin.Boolean? = null,

    @Json(name = "LastSynchronizationDate")
    val lastSynchronizationDate: kotlin.Int? = null,

    @Json(name = "Name")
    val name: kotlin.String? = null,

    @Json(name = "ObjectsBaseFolder")
    val objectsBaseFolder: kotlin.String? = null,

    @Json(name = "ObjectsBucket")
    val objectsBucket: kotlin.String? = null,

    @Json(name = "ObjectsHost")
    val objectsHost: kotlin.String? = null,

    @Json(name = "ObjectsPort")
    val objectsPort: kotlin.Int? = null,

    @Json(name = "ObjectsSecure")
    val objectsSecure: kotlin.Boolean? = null,

    @Json(name = "ObjectsServiceName")
    val objectsServiceName: kotlin.String? = null,

    @Json(name = "PeerAddress")
    val peerAddress: kotlin.String? = null,

    @Json(name = "SkipSyncOnRestart")
    val skipSyncOnRestart: kotlin.Boolean? = null,

    @Json(name = "StorageConfiguration")
    val storageConfiguration: kotlin.collections.Map<kotlin.String, kotlin.String>? = null,

    @Json(name = "StorageType")
    val storageType: ObjectStorageType? = ObjectStorageType.LOCAL,

    @Json(name = "VersioningPolicyName")
    val versioningPolicyName: kotlin.String? = null,

    @Json(name = "Watch")
    val watch: kotlin.Boolean? = null

)
