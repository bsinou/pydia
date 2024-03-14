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

import org.sinou.pydia.openapi.model.RestError
import org.sinou.pydia.openapi.model.RestSearchResults
import org.sinou.pydia.openapi.model.TreeSearchRequest

import com.squareup.moshi.Json

import org.sinou.pydia.openapi.infrastructure.ApiClient
import org.sinou.pydia.openapi.infrastructure.ApiResponse
import org.sinou.pydia.openapi.infrastructure.ClientException
import org.sinou.pydia.openapi.infrastructure.ClientError
import org.sinou.pydia.openapi.infrastructure.ServerException
import org.sinou.pydia.openapi.infrastructure.ServerError
import org.sinou.pydia.openapi.infrastructure.MultiValueMap
import org.sinou.pydia.openapi.infrastructure.PartConfig
import org.sinou.pydia.openapi.infrastructure.RequestConfig
import org.sinou.pydia.openapi.infrastructure.RequestMethod
import org.sinou.pydia.openapi.infrastructure.ResponseType
import org.sinou.pydia.openapi.infrastructure.Success
import org.sinou.pydia.openapi.infrastructure.toMultiValue

class SearchServiceApi(basePath: kotlin.String = defaultBasePath, client: OkHttpClient = ApiClient.defaultClient) : ApiClient(basePath, client) {
    companion object {
        @JvmStatic
        val defaultBasePath: String by lazy {
            System.getProperties().getProperty(ApiClient.baseUrlKey, "http://localhost")
        }
    }

    /**
     * Search indexed nodes (files/folders) on various aspects
     * 
     * @param body 
     * @return RestSearchResults
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun nodes(body: TreeSearchRequest) : RestSearchResults {
        val localVarResponse = nodesWithHttpInfo(body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestSearchResults
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
     * Search indexed nodes (files/folders) on various aspects
     * 
     * @param body 
     * @return ApiResponse<RestSearchResults?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun nodesWithHttpInfo(body: TreeSearchRequest) : ApiResponse<RestSearchResults?> {
        val localVariableConfig = nodesRequestConfig(body = body)

        return request<TreeSearchRequest, RestSearchResults>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation nodes
     *
     * @param body 
     * @return RequestConfig
     */
    fun nodesRequestConfig(body: TreeSearchRequest) : RequestConfig<TreeSearchRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/search/nodes",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }


    private fun encodeURIComponent(uriComponent: kotlin.String): kotlin.String =
        HttpUrl.Builder().scheme("http").host("localhost").addPathSegment(uriComponent).build().encodedPathSegments[0]
}
