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

import org.sinou.pydia.openapi.model.EncryptionExport
import org.sinou.pydia.openapi.model.EncryptionImport

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param exports 
 * @param imports 
 */


data class EncryptionKeyInfo (

    @Json(name = "Exports")
    val exports: kotlin.collections.List<EncryptionExport>? = null,

    @Json(name = "Imports")
    val imports: kotlin.collections.List<EncryptionImport>? = null

)

