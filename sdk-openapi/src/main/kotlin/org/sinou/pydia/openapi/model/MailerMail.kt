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

import org.sinou.pydia.openapi.model.MailerUser

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param attachments 
 * @param cc 
 * @param contentHtml 
 * @param contentMarkdown 
 * @param contentPlain 
 * @param dateSent 
 * @param from 
 * @param retries 
 * @param sender 
 * @param subject 
 * @param templateData 
 * @param templateId 
 * @param threadIndex 
 * @param threadUuid 
 * @param to 
 * @param sendErrors 
 */


data class MailerMail (

    @Json(name = "Attachments")
    val attachments: kotlin.collections.List<kotlin.String>? = null,

    @Json(name = "Cc")
    val cc: kotlin.collections.List<MailerUser>? = null,

    @Json(name = "ContentHtml")
    val contentHtml: kotlin.String? = null,

    @Json(name = "ContentMarkdown")
    val contentMarkdown: kotlin.String? = null,

    @Json(name = "ContentPlain")
    val contentPlain: kotlin.String? = null,

    @Json(name = "DateSent")
    val dateSent: kotlin.String? = null,

    @Json(name = "From")
    val from: MailerUser? = null,

    @Json(name = "Retries")
    val retries: kotlin.Int? = null,

    @Json(name = "Sender")
    val sender: MailerUser? = null,

    @Json(name = "Subject")
    val subject: kotlin.String? = null,

    @Json(name = "TemplateData")
    val templateData: kotlin.collections.Map<kotlin.String, kotlin.String>? = null,

    @Json(name = "TemplateId")
    val templateId: kotlin.String? = null,

    @Json(name = "ThreadIndex")
    val threadIndex: kotlin.String? = null,

    @Json(name = "ThreadUuid")
    val threadUuid: kotlin.String? = null,

    @Json(name = "To")
    val to: kotlin.collections.List<MailerUser>? = null,

    @Json(name = "sendErrors")
    val sendErrors: kotlin.collections.List<kotlin.String>? = null

) {


}

