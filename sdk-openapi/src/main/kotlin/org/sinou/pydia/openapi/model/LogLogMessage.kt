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


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * LogMessage is the format used to transmit log messages to clients via the REST API.
 *
 * @param groupPath 
 * @param httpProtocol 
 * @param jsonZaps 
 * @param level 
 * @param logger 
 * @param msg 
 * @param msgId 
 * @param nodePath 
 * @param nodeUuid 
 * @param operationLabel 
 * @param operationUuid 
 * @param profile 
 * @param remoteAddress 
 * @param roleUuids 
 * @param schedulerJobUuid 
 * @param schedulerTaskActionPath 
 * @param schedulerTaskUuid 
 * @param spanParentUuid 
 * @param spanRootUuid 
 * @param spanUuid 
 * @param transferSize 
 * @param ts 
 * @param userAgent 
 * @param userName 
 * @param userUuid 
 * @param wsScope 
 * @param wsUuid 
 */


data class LogLogMessage (

    @Json(name = "GroupPath")
    val groupPath: kotlin.String? = null,

    @Json(name = "HttpProtocol")
    val httpProtocol: kotlin.String? = null,

    @Json(name = "JsonZaps")
    val jsonZaps: kotlin.String? = null,

    @Json(name = "Level")
    val level: kotlin.String? = null,

    @Json(name = "Logger")
    val logger: kotlin.String? = null,

    @Json(name = "Msg")
    val msg: kotlin.String? = null,

    @Json(name = "MsgId")
    val msgId: kotlin.String? = null,

    @Json(name = "NodePath")
    val nodePath: kotlin.String? = null,

    @Json(name = "NodeUuid")
    val nodeUuid: kotlin.String? = null,

    @Json(name = "OperationLabel")
    val operationLabel: kotlin.String? = null,

    @Json(name = "OperationUuid")
    val operationUuid: kotlin.String? = null,

    @Json(name = "Profile")
    val profile: kotlin.String? = null,

    @Json(name = "RemoteAddress")
    val remoteAddress: kotlin.String? = null,

    @Json(name = "RoleUuids")
    val roleUuids: kotlin.collections.List<kotlin.String>? = null,

    @Json(name = "SchedulerJobUuid")
    val schedulerJobUuid: kotlin.String? = null,

    @Json(name = "SchedulerTaskActionPath")
    val schedulerTaskActionPath: kotlin.String? = null,

    @Json(name = "SchedulerTaskUuid")
    val schedulerTaskUuid: kotlin.String? = null,

    @Json(name = "SpanParentUuid")
    val spanParentUuid: kotlin.String? = null,

    @Json(name = "SpanRootUuid")
    val spanRootUuid: kotlin.String? = null,

    @Json(name = "SpanUuid")
    val spanUuid: kotlin.String? = null,

    @Json(name = "TransferSize")
    val transferSize: kotlin.String? = null,

    @Json(name = "Ts")
    val ts: kotlin.Int? = null,

    @Json(name = "UserAgent")
    val userAgent: kotlin.String? = null,

    @Json(name = "UserName")
    val userName: kotlin.String? = null,

    @Json(name = "UserUuid")
    val userUuid: kotlin.String? = null,

    @Json(name = "WsScope")
    val wsScope: kotlin.String? = null,

    @Json(name = "WsUuid")
    val wsUuid: kotlin.String? = null

) {


}

