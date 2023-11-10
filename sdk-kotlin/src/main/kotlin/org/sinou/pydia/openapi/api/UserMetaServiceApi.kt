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

import org.sinou.pydia.openapi.model.IdmSearchUserMetaRequest
import org.sinou.pydia.openapi.model.IdmUpdateUserMetaNamespaceRequest
import org.sinou.pydia.openapi.model.IdmUpdateUserMetaNamespaceResponse
import org.sinou.pydia.openapi.model.IdmUpdateUserMetaRequest
import org.sinou.pydia.openapi.model.IdmUpdateUserMetaResponse
import org.sinou.pydia.openapi.model.RestBulkMetaResponse
import org.sinou.pydia.openapi.model.RestDeleteUserMetaTagsResponse
import org.sinou.pydia.openapi.model.RestError
import org.sinou.pydia.openapi.model.RestListUserMetaTagsResponse
import org.sinou.pydia.openapi.model.RestPutUserMetaTagRequest
import org.sinou.pydia.openapi.model.RestPutUserMetaTagResponse
import org.sinou.pydia.openapi.model.RestUserBookmarksRequest
import org.sinou.pydia.openapi.model.RestUserMetaCollection
import org.sinou.pydia.openapi.model.RestUserMetaNamespaceCollection

import com.squareup.moshi.Json

import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.infrastructure.ApiResponse
import org.openapitools.client.infrastructure.ClientException
import org.openapitools.client.infrastructure.ClientError
import org.openapitools.client.infrastructure.ServerException
import org.openapitools.client.infrastructure.ServerError
import org.openapitools.client.infrastructure.MultiValueMap
import org.openapitools.client.infrastructure.PartConfig
import org.openapitools.client.infrastructure.RequestConfig
import org.openapitools.client.infrastructure.RequestMethod
import org.openapitools.client.infrastructure.ResponseType
import org.openapitools.client.infrastructure.Success
import org.openapitools.client.infrastructure.toMultiValue

class UserMetaServiceApi(basePath: kotlin.String = defaultBasePath, client: OkHttpClient = ApiClient.defaultClient) : ApiClient(basePath, client) {
    companion object {
        @JvmStatic
        val defaultBasePath: String by lazy {
            System.getProperties().getProperty(ApiClient.baseUrlKey, "http://localhost")
        }
    }

    /**
     * Delete one or all tags for a given namespace (use * for all tags)
     * 
     * @param namespace Delete tags from this namespace
     * @param tags Delete this tag
     * @return RestDeleteUserMetaTagsResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun deleteUserMetaTags(namespace: kotlin.String, tags: kotlin.String) : RestDeleteUserMetaTagsResponse {
        val localVarResponse = deleteUserMetaTagsWithHttpInfo(namespace = namespace, tags = tags)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestDeleteUserMetaTagsResponse
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
     * Delete one or all tags for a given namespace (use * for all tags)
     * 
     * @param namespace Delete tags from this namespace
     * @param tags Delete this tag
     * @return ApiResponse<RestDeleteUserMetaTagsResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun deleteUserMetaTagsWithHttpInfo(namespace: kotlin.String, tags: kotlin.String) : ApiResponse<RestDeleteUserMetaTagsResponse?> {
        val localVariableConfig = deleteUserMetaTagsRequestConfig(namespace = namespace, tags = tags)

        return request<Unit, RestDeleteUserMetaTagsResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation deleteUserMetaTags
     *
     * @param namespace Delete tags from this namespace
     * @param tags Delete this tag
     * @return RequestConfig
     */
    fun deleteUserMetaTagsRequestConfig(namespace: kotlin.String, tags: kotlin.String) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.DELETE,
            path = "/user-meta/tags/{Namespace}/{Tags}".replace("{"+"Namespace"+"}", encodeURIComponent(namespace.toString())).replace("{"+"Tags"+"}", encodeURIComponent(tags.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * List defined meta namespaces
     * 
     * @return RestUserMetaNamespaceCollection
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun listUserMetaNamespace() : RestUserMetaNamespaceCollection {
        val localVarResponse = listUserMetaNamespaceWithHttpInfo()

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestUserMetaNamespaceCollection
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
     * List defined meta namespaces
     * 
     * @return ApiResponse<RestUserMetaNamespaceCollection?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun listUserMetaNamespaceWithHttpInfo() : ApiResponse<RestUserMetaNamespaceCollection?> {
        val localVariableConfig = listUserMetaNamespaceRequestConfig()

        return request<Unit, RestUserMetaNamespaceCollection>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation listUserMetaNamespace
     *
     * @return RequestConfig
     */
    fun listUserMetaNamespaceRequestConfig() : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/user-meta/namespace",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * List Tags for a given namespace
     * 
     * @param namespace List user meta tags for this namespace
     * @return RestListUserMetaTagsResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun listUserMetaTags(namespace: kotlin.String) : RestListUserMetaTagsResponse {
        val localVarResponse = listUserMetaTagsWithHttpInfo(namespace = namespace)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestListUserMetaTagsResponse
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
     * List Tags for a given namespace
     * 
     * @param namespace List user meta tags for this namespace
     * @return ApiResponse<RestListUserMetaTagsResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun listUserMetaTagsWithHttpInfo(namespace: kotlin.String) : ApiResponse<RestListUserMetaTagsResponse?> {
        val localVariableConfig = listUserMetaTagsRequestConfig(namespace = namespace)

        return request<Unit, RestListUserMetaTagsResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation listUserMetaTags
     *
     * @param namespace List user meta tags for this namespace
     * @return RequestConfig
     */
    fun listUserMetaTagsRequestConfig(namespace: kotlin.String) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/user-meta/tags/{Namespace}".replace("{"+"Namespace"+"}", encodeURIComponent(namespace.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Add a new value to Tags for a given namespace
     * 
     * @param namespace Add a tag value for this namespace
     * @param body 
     * @return RestPutUserMetaTagResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun putUserMetaTag(namespace: kotlin.String, body: RestPutUserMetaTagRequest) : RestPutUserMetaTagResponse {
        val localVarResponse = putUserMetaTagWithHttpInfo(namespace = namespace, body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestPutUserMetaTagResponse
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
     * Add a new value to Tags for a given namespace
     * 
     * @param namespace Add a tag value for this namespace
     * @param body 
     * @return ApiResponse<RestPutUserMetaTagResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun putUserMetaTagWithHttpInfo(namespace: kotlin.String, body: RestPutUserMetaTagRequest) : ApiResponse<RestPutUserMetaTagResponse?> {
        val localVariableConfig = putUserMetaTagRequestConfig(namespace = namespace, body = body)

        return request<RestPutUserMetaTagRequest, RestPutUserMetaTagResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation putUserMetaTag
     *
     * @param namespace Add a tag value for this namespace
     * @param body 
     * @return RequestConfig
     */
    fun putUserMetaTagRequestConfig(namespace: kotlin.String, body: RestPutUserMetaTagRequest) : RequestConfig<RestPutUserMetaTagRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/user-meta/tags/{Namespace}".replace("{"+"Namespace"+"}", encodeURIComponent(namespace.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Search a list of meta by node Id or by User id and by namespace
     * 
     * @param body 
     * @return RestUserMetaCollection
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun searchUserMeta(body: IdmSearchUserMetaRequest) : RestUserMetaCollection {
        val localVarResponse = searchUserMetaWithHttpInfo(body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestUserMetaCollection
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
     * Search a list of meta by node Id or by User id and by namespace
     * 
     * @param body 
     * @return ApiResponse<RestUserMetaCollection?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun searchUserMetaWithHttpInfo(body: IdmSearchUserMetaRequest) : ApiResponse<RestUserMetaCollection?> {
        val localVariableConfig = searchUserMetaRequestConfig(body = body)

        return request<IdmSearchUserMetaRequest, RestUserMetaCollection>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation searchUserMeta
     *
     * @param body 
     * @return RequestConfig
     */
    fun searchUserMetaRequestConfig(body: IdmSearchUserMetaRequest) : RequestConfig<IdmSearchUserMetaRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/user-meta/search",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Update/delete user meta
     * 
     * @param body 
     * @return IdmUpdateUserMetaResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun updateUserMeta(body: IdmUpdateUserMetaRequest) : IdmUpdateUserMetaResponse {
        val localVarResponse = updateUserMetaWithHttpInfo(body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as IdmUpdateUserMetaResponse
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
     * Update/delete user meta
     * 
     * @param body 
     * @return ApiResponse<IdmUpdateUserMetaResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun updateUserMetaWithHttpInfo(body: IdmUpdateUserMetaRequest) : ApiResponse<IdmUpdateUserMetaResponse?> {
        val localVariableConfig = updateUserMetaRequestConfig(body = body)

        return request<IdmUpdateUserMetaRequest, IdmUpdateUserMetaResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation updateUserMeta
     *
     * @param body 
     * @return RequestConfig
     */
    fun updateUserMetaRequestConfig(body: IdmUpdateUserMetaRequest) : RequestConfig<IdmUpdateUserMetaRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.PUT,
            path = "/user-meta/update",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Admin: update namespaces
     * 
     * @param body 
     * @return IdmUpdateUserMetaNamespaceResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun updateUserMetaNamespace(body: IdmUpdateUserMetaNamespaceRequest) : IdmUpdateUserMetaNamespaceResponse {
        val localVarResponse = updateUserMetaNamespaceWithHttpInfo(body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as IdmUpdateUserMetaNamespaceResponse
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
     * Admin: update namespaces
     * 
     * @param body 
     * @return ApiResponse<IdmUpdateUserMetaNamespaceResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun updateUserMetaNamespaceWithHttpInfo(body: IdmUpdateUserMetaNamespaceRequest) : ApiResponse<IdmUpdateUserMetaNamespaceResponse?> {
        val localVariableConfig = updateUserMetaNamespaceRequestConfig(body = body)

        return request<IdmUpdateUserMetaNamespaceRequest, IdmUpdateUserMetaNamespaceResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation updateUserMetaNamespace
     *
     * @param body 
     * @return RequestConfig
     */
    fun updateUserMetaNamespaceRequestConfig(body: IdmUpdateUserMetaNamespaceRequest) : RequestConfig<IdmUpdateUserMetaNamespaceRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.PUT,
            path = "/user-meta/namespace",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Special API for Bookmarks, will load userMeta and the associated nodes, and return as a node list
     * 
     * @param body 
     * @return RestBulkMetaResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun userBookmarks(body: RestUserBookmarksRequest) : RestBulkMetaResponse {
        val localVarResponse = userBookmarksWithHttpInfo(body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestBulkMetaResponse
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
     * Special API for Bookmarks, will load userMeta and the associated nodes, and return as a node list
     * 
     * @param body 
     * @return ApiResponse<RestBulkMetaResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun userBookmarksWithHttpInfo(body: RestUserBookmarksRequest) : ApiResponse<RestBulkMetaResponse?> {
        val localVariableConfig = userBookmarksRequestConfig(body = body)

        return request<RestUserBookmarksRequest, RestBulkMetaResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation userBookmarks
     *
     * @param body 
     * @return RequestConfig
     */
    fun userBookmarksRequestConfig(body: RestUserBookmarksRequest) : RequestConfig<RestUserBookmarksRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/user-meta/bookmarks",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }


    private fun encodeURIComponent(uriComponent: kotlin.String): kotlin.String =
        HttpUrl.Builder().scheme("http").host("localhost").addPathSegment(uriComponent).build().encodedPathSegments[0]
}
