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

package org.sinou.pydia.openapi.api

import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.HttpUrl

import org.sinou.pydia.openapi.model.UpdateApplyUpdateRequest
import org.sinou.pydia.openapi.model.UpdateApplyUpdateResponse
import org.sinou.pydia.openapi.model.UpdateUpdateRequest
import org.sinou.pydia.openapi.model.UpdateUpdateResponse

import org.sinou.pydia.openapi.infrastructure.ApiClient
import org.sinou.pydia.openapi.infrastructure.ApiResponse
import org.sinou.pydia.openapi.infrastructure.ClientException
import org.sinou.pydia.openapi.infrastructure.ClientError
import org.sinou.pydia.openapi.infrastructure.ServerException
import org.sinou.pydia.openapi.infrastructure.ServerError
import org.sinou.pydia.openapi.infrastructure.MultiValueMap
import org.sinou.pydia.openapi.infrastructure.RequestConfig
import org.sinou.pydia.openapi.infrastructure.RequestMethod
import org.sinou.pydia.openapi.infrastructure.ResponseType
import org.sinou.pydia.openapi.infrastructure.Success

class UpdateServiceApi(basePath: kotlin.String = defaultBasePath, client: OkHttpClient = ApiClient.defaultClient) : ApiClient(basePath, client) {
    companion object {
        @JvmStatic
        val defaultBasePath: String by lazy {
            System.getProperties().getProperty(ApiClient.baseUrlKey, "http://localhost")
        }
    }

    /**
     * Apply an update to a given version
     * 
     * @param targetVersion Version of the target binary
     * @param body 
     * @return UpdateApplyUpdateResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun applyUpdate(targetVersion: kotlin.String, body: UpdateApplyUpdateRequest) : UpdateApplyUpdateResponse {
        val localVarResponse = applyUpdateWithHttpInfo(targetVersion = targetVersion, body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as UpdateApplyUpdateResponse
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     * Apply an update to a given version
     * 
     * @param targetVersion Version of the target binary
     * @param body 
     * @return ApiResponse<UpdateApplyUpdateResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun applyUpdateWithHttpInfo(targetVersion: kotlin.String, body: UpdateApplyUpdateRequest) : ApiResponse<UpdateApplyUpdateResponse?> {
        val localVariableConfig = applyUpdateRequestConfig(targetVersion = targetVersion, body = body)

        return request<UpdateApplyUpdateRequest, UpdateApplyUpdateResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation applyUpdate
     *
     * @param targetVersion Version of the target binary
     * @param body 
     * @return RequestConfig
     */
    fun applyUpdateRequestConfig(targetVersion: kotlin.String, body: UpdateApplyUpdateRequest) : RequestConfig<UpdateApplyUpdateRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.PATCH,
            path = "/update/{TargetVersion}".replace("{"+"TargetVersion"+"}", encodeURIComponent(targetVersion.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Check the remote server to see if there are available binaries
     * 
     * @param body 
     * @return UpdateUpdateResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun updateRequired(body: UpdateUpdateRequest) : UpdateUpdateResponse {
        val localVarResponse = updateRequiredWithHttpInfo(body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as UpdateUpdateResponse
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     * Check the remote server to see if there are available binaries
     * 
     * @param body 
     * @return ApiResponse<UpdateUpdateResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun updateRequiredWithHttpInfo(body: UpdateUpdateRequest) : ApiResponse<UpdateUpdateResponse?> {
        val localVariableConfig = updateRequiredRequestConfig(body = body)

        return request<UpdateUpdateRequest, UpdateUpdateResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation updateRequired
     *
     * @param body 
     * @return RequestConfig
     */
    fun updateRequiredRequestConfig(body: UpdateUpdateRequest) : RequestConfig<UpdateUpdateRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/update",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }


    private fun encodeURIComponent(uriComponent: kotlin.String): kotlin.String =
        HttpUrl.Builder().scheme("http").host("localhost").addPathSegment(uriComponent).build().encodedPathSegments[0]
}
