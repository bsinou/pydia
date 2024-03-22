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
 * Values: None,Pause,Resume,Stop,Delete,RunOnce,Inactive,Active
 */

@JsonClass(generateAdapter = false)
enum class JobsCommand(val value: kotlin.String) {

    @Json(name = "None")
    None("None"),

    @Json(name = "Pause")
    Pause("Pause"),

    @Json(name = "Resume")
    Resume("Resume"),

    @Json(name = "Stop")
    Stop("Stop"),

    @Json(name = "Delete")
    Delete("Delete"),

    @Json(name = "RunOnce")
    RunOnce("RunOnce"),

    @Json(name = "Inactive")
    Inactive("Inactive"),

    @Json(name = "Active")
    Active("Active");

    /**
     * Override [toString()] to avoid using the enum variable name as the value, and instead use
     * the actual value defined in the API spec file.
     *
     * This solves a problem when the variable name and its value are different, and ensures that
     * the client sends the correct enum values to the server always.
     */
    override fun toString(): kotlin.String = value

    companion object {
        /**
         * Converts the provided [data] to a [String] on success, null otherwise.
         */
        fun encode(data: kotlin.Any?): kotlin.String? = if (data is JobsCommand) "$data" else null

        /**
         * Returns a valid [JobsCommand] for [data], null otherwise.
         */
        fun decode(data: kotlin.Any?): JobsCommand? = data?.let {
          val normalizedData = "$it".lowercase()
          values().firstOrNull { value ->
            it == value || normalizedData == "$value".lowercase()
          }
        }
    }
}
