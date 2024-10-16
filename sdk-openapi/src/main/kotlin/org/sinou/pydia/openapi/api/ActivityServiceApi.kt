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

import org.sinou.pydia.openapi.model.ActivityObject
import org.sinou.pydia.openapi.model.ActivitySearchSubscriptionsRequest
import org.sinou.pydia.openapi.model.ActivityStreamActivitiesRequest
import org.sinou.pydia.openapi.model.ActivitySubscription
import org.sinou.pydia.openapi.model.RestSubscriptionsCollection

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

class ActivityServiceApi(basePath: kotlin.String = defaultBasePath, client: OkHttpClient = ApiClient.defaultClient) : ApiClient(basePath, client) {
    companion object {
        @JvmStatic
        val defaultBasePath: String by lazy {
            System.getProperties().getProperty(ApiClient.baseUrlKey, "http://localhost")
        }
    }

    /**
     * Load subscriptions to other users/nodes feeds
     * 
     * @param body 
     * @return RestSubscriptionsCollection
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun searchSubscriptions(body: ActivitySearchSubscriptionsRequest) : RestSubscriptionsCollection {
        val localVarResponse = searchSubscriptionsWithHttpInfo(body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestSubscriptionsCollection
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
     * Load subscriptions to other users/nodes feeds
     * 
     * @param body 
     * @return ApiResponse<RestSubscriptionsCollection?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun searchSubscriptionsWithHttpInfo(body: ActivitySearchSubscriptionsRequest) : ApiResponse<RestSubscriptionsCollection?> {
        val localVariableConfig = searchSubscriptionsRequestConfig(body = body)

        return request<ActivitySearchSubscriptionsRequest, RestSubscriptionsCollection>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation searchSubscriptions
     *
     * @param body 
     * @return RequestConfig
     */
    fun searchSubscriptionsRequestConfig(body: ActivitySearchSubscriptionsRequest) : RequestConfig<ActivitySearchSubscriptionsRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/activity/subscriptions",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Load the the feeds of the currently logged user
     * 
     * @param body 
     * @return ActivityObject
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun stream(body: ActivityStreamActivitiesRequest) : ActivityObject {
        val localVarResponse = streamWithHttpInfo(body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as ActivityObject
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
     * Load the the feeds of the currently logged user
     * 
     * @param body 
     * @return ApiResponse<ActivityObject?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun streamWithHttpInfo(body: ActivityStreamActivitiesRequest) : ApiResponse<ActivityObject?> {
        val localVariableConfig = streamRequestConfig(body = body)

        return request<ActivityStreamActivitiesRequest, ActivityObject>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation stream
     *
     * @param body 
     * @return RequestConfig
     */
    fun streamRequestConfig(body: ActivityStreamActivitiesRequest) : RequestConfig<ActivityStreamActivitiesRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/activity/stream",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Manage subscriptions to other users/nodes feeds
     * 
     * @param body 
     * @return ActivitySubscription
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun subscribe(body: ActivitySubscription) : ActivitySubscription {
        val localVarResponse = subscribeWithHttpInfo(body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as ActivitySubscription
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
     * Manage subscriptions to other users/nodes feeds
     * 
     * @param body 
     * @return ApiResponse<ActivitySubscription?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun subscribeWithHttpInfo(body: ActivitySubscription) : ApiResponse<ActivitySubscription?> {
        val localVariableConfig = subscribeRequestConfig(body = body)

        return request<ActivitySubscription, ActivitySubscription>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation subscribe
     *
     * @param body 
     * @return RequestConfig
     */
    fun subscribeRequestConfig(body: ActivitySubscription) : RequestConfig<ActivitySubscription> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/activity/subscribe",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }


    private fun encodeURIComponent(uriComponent: kotlin.String): kotlin.String =
        HttpUrl.Builder().scheme("http").host("localhost").addPathSegment(uriComponent).build().encodedPathSegments[0]
}
