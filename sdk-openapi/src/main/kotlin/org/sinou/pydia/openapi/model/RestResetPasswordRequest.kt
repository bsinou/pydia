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
 * 
 *
 * @param newPassword 
 * @param resetPasswordToken 
 * @param userLogin 
 */


data class RestResetPasswordRequest (

    @Json(name = "NewPassword")
    val newPassword: kotlin.String? = null,

    @Json(name = "ResetPasswordToken")
    val resetPasswordToken: kotlin.String? = null,

    @Json(name = "UserLogin")
    val userLogin: kotlin.String? = null

) {


}

