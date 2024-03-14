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
import org.sinou.pydia.openapi.model.RestRecommendRequest
import org.sinou.pydia.openapi.model.RestRecommendResponse
import org.sinou.pydia.openapi.model.RestRelationResponse
import org.sinou.pydia.openapi.model.RestUserStateResponse

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

class GraphServiceApi(basePath: kotlin.String = defaultBasePath, client: OkHttpClient = ApiClient.defaultClient) : ApiClient(basePath, client) {
    companion object {
        @JvmStatic
        val defaultBasePath: String by lazy {
            System.getProperties().getProperty(ApiClient.baseUrlKey, "http://localhost")
        }
    }

    /**
     * 
     * 
     * @param body 
     * @return RestRecommendResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun recommend(body: RestRecommendRequest) : RestRecommendResponse {
        val localVarResponse = recommendWithHttpInfo(body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestRecommendResponse
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
     * 
     * 
     * @param body 
     * @return ApiResponse<RestRecommendResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun recommendWithHttpInfo(body: RestRecommendRequest) : ApiResponse<RestRecommendResponse?> {
        val localVariableConfig = recommendRequestConfig(body = body)

        return request<RestRecommendRequest, RestRecommendResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation recommend
     *
     * @param body 
     * @return RequestConfig
     */
    fun recommendRequestConfig(body: RestRecommendRequest) : RequestConfig<RestRecommendRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/graph/recommend",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Compute relation of context user with another user
     * 
     * @param userId 
     * @return RestRelationResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun relation(userId: kotlin.String) : RestRelationResponse {
        val localVarResponse = relationWithHttpInfo(userId = userId)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestRelationResponse
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
     * Compute relation of context user with another user
     * 
     * @param userId 
     * @return ApiResponse<RestRelationResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun relationWithHttpInfo(userId: kotlin.String) : ApiResponse<RestRelationResponse?> {
        val localVariableConfig = relationRequestConfig(userId = userId)

        return request<Unit, RestRelationResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation relation
     *
     * @param userId 
     * @return RequestConfig
     */
    fun relationRequestConfig(userId: kotlin.String) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/graph/relation/{UserId}".replace("{"+"UserId"+"}", encodeURIComponent(userId.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Compute accessible workspaces for a given user
     * 
     * @param segment 
     * @return RestUserStateResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun userState(segment: kotlin.String) : RestUserStateResponse {
        val localVarResponse = userStateWithHttpInfo(segment = segment)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestUserStateResponse
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
     * Compute accessible workspaces for a given user
     * 
     * @param segment 
     * @return ApiResponse<RestUserStateResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun userStateWithHttpInfo(segment: kotlin.String) : ApiResponse<RestUserStateResponse?> {
        val localVariableConfig = userStateRequestConfig(segment = segment)

        return request<Unit, RestUserStateResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation userState
     *
     * @param segment 
     * @return RequestConfig
     */
    fun userStateRequestConfig(segment: kotlin.String) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/graph/state/{Segment}".replace("{"+"Segment"+"}", encodeURIComponent(segment.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }


    private fun encodeURIComponent(uriComponent: kotlin.String): kotlin.String =
        HttpUrl.Builder().scheme("http").host("localhost").addPathSegment(uriComponent).build().encodedPathSegments[0]
}
