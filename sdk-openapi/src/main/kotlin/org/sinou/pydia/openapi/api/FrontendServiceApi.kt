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

import org.sinou.pydia.openapi.model.RestFrontBinaryResponse
import org.sinou.pydia.openapi.model.RestFrontBootConfResponse
import org.sinou.pydia.openapi.model.RestFrontEnrollAuthRequest
import org.sinou.pydia.openapi.model.RestFrontEnrollAuthResponse
import org.sinou.pydia.openapi.model.RestFrontMessagesResponse
import org.sinou.pydia.openapi.model.RestFrontPluginsResponse
import org.sinou.pydia.openapi.model.RestFrontSessionRequest
import org.sinou.pydia.openapi.model.RestFrontSessionResponse
import org.sinou.pydia.openapi.model.RestFrontStateResponse
import org.sinou.pydia.openapi.model.RestSettingsMenuResponse

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

class FrontendServiceApi(basePath: kotlin.String = defaultBasePath, client: OkHttpClient = ApiClient.defaultClient) : ApiClient(basePath, client) {
    companion object {
        @JvmStatic
        val defaultBasePath: String by lazy {
            System.getProperties().getProperty(ApiClient.baseUrlKey, "http://localhost")
        }
    }

    /**
     * Add some data to the initial set of parameters loaded by the frontend
     * 
     * @return RestFrontBootConfResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun frontBootConf() : RestFrontBootConfResponse {
        val localVarResponse = frontBootConfWithHttpInfo()

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestFrontBootConfResponse
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
     * Add some data to the initial set of parameters loaded by the frontend
     * 
     * @return ApiResponse<RestFrontBootConfResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun frontBootConfWithHttpInfo() : ApiResponse<RestFrontBootConfResponse?> {
        val localVariableConfig = frontBootConfRequestConfig()

        return request<Unit, RestFrontBootConfResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation frontBootConf
     *
     * @return RequestConfig
     */
    fun frontBootConfRequestConfig() : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/frontend/bootconf",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Generic endpoint that can be implemented by 2FA systems for enrollment
     * 
     * @param body 
     * @return RestFrontEnrollAuthResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun frontEnrollAuth(body: RestFrontEnrollAuthRequest) : RestFrontEnrollAuthResponse {
        val localVarResponse = frontEnrollAuthWithHttpInfo(body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestFrontEnrollAuthResponse
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
     * Generic endpoint that can be implemented by 2FA systems for enrollment
     * 
     * @param body 
     * @return ApiResponse<RestFrontEnrollAuthResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun frontEnrollAuthWithHttpInfo(body: RestFrontEnrollAuthRequest) : ApiResponse<RestFrontEnrollAuthResponse?> {
        val localVariableConfig = frontEnrollAuthRequestConfig(body = body)

        return request<RestFrontEnrollAuthRequest, RestFrontEnrollAuthResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation frontEnrollAuth
     *
     * @param body 
     * @return RequestConfig
     */
    fun frontEnrollAuthRequestConfig(body: RestFrontEnrollAuthRequest) : RequestConfig<RestFrontEnrollAuthRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/frontend/enroll",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Serve list of I18n messages
     * 
     * @param lang 
     * @return RestFrontMessagesResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun frontMessages(lang: kotlin.String) : RestFrontMessagesResponse {
        val localVarResponse = frontMessagesWithHttpInfo(lang = lang)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestFrontMessagesResponse
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
     * Serve list of I18n messages
     * 
     * @param lang 
     * @return ApiResponse<RestFrontMessagesResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun frontMessagesWithHttpInfo(lang: kotlin.String) : ApiResponse<RestFrontMessagesResponse?> {
        val localVariableConfig = frontMessagesRequestConfig(lang = lang)

        return request<Unit, RestFrontMessagesResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation frontMessages
     *
     * @param lang 
     * @return RequestConfig
     */
    fun frontMessagesRequestConfig(lang: kotlin.String) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/frontend/messages/{Lang}".replace("{"+"Lang"+"}", encodeURIComponent(lang.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Serve list of I18n messages
     * 
     * @param lang 
     * @return RestFrontPluginsResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun frontPlugins(lang: kotlin.String) : RestFrontPluginsResponse {
        val localVarResponse = frontPluginsWithHttpInfo(lang = lang)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestFrontPluginsResponse
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
     * Serve list of I18n messages
     * 
     * @param lang 
     * @return ApiResponse<RestFrontPluginsResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun frontPluginsWithHttpInfo(lang: kotlin.String) : ApiResponse<RestFrontPluginsResponse?> {
        val localVariableConfig = frontPluginsRequestConfig(lang = lang)

        return request<Unit, RestFrontPluginsResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation frontPlugins
     *
     * @param lang 
     * @return RequestConfig
     */
    fun frontPluginsRequestConfig(lang: kotlin.String) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/frontend/plugins/{Lang}".replace("{"+"Lang"+"}", encodeURIComponent(lang.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Upload frontend binaries (avatars / logos / bg images)
     * 
     * @param binaryType Currently supported values are USER and GLOBAL
     * @param uuid Id of the binary
     * @return RestFrontBinaryResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun frontPutBinary(binaryType: kotlin.String, uuid: kotlin.String) : RestFrontBinaryResponse {
        val localVarResponse = frontPutBinaryWithHttpInfo(binaryType = binaryType, uuid = uuid)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestFrontBinaryResponse
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
     * Upload frontend binaries (avatars / logos / bg images)
     * 
     * @param binaryType Currently supported values are USER and GLOBAL
     * @param uuid Id of the binary
     * @return ApiResponse<RestFrontBinaryResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun frontPutBinaryWithHttpInfo(binaryType: kotlin.String, uuid: kotlin.String) : ApiResponse<RestFrontBinaryResponse?> {
        val localVariableConfig = frontPutBinaryRequestConfig(binaryType = binaryType, uuid = uuid)

        return request<Unit, RestFrontBinaryResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation frontPutBinary
     *
     * @param binaryType Currently supported values are USER and GLOBAL
     * @param uuid Id of the binary
     * @return RequestConfig
     */
    fun frontPutBinaryRequestConfig(binaryType: kotlin.String, uuid: kotlin.String) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/frontend/binaries/{BinaryType}/{Uuid}".replace("{"+"BinaryType"+"}", encodeURIComponent(binaryType.toString())).replace("{"+"Uuid"+"}", encodeURIComponent(uuid.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Serve frontend binaries directly (avatars / logos / bg images)
     * 
     * @param binaryType Currently supported values are USER and GLOBAL
     * @param uuid Id of the binary
     * @return RestFrontBinaryResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun frontServeBinary(binaryType: kotlin.String, uuid: kotlin.String) : RestFrontBinaryResponse {
        val localVarResponse = frontServeBinaryWithHttpInfo(binaryType = binaryType, uuid = uuid)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestFrontBinaryResponse
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
     * Serve frontend binaries directly (avatars / logos / bg images)
     * 
     * @param binaryType Currently supported values are USER and GLOBAL
     * @param uuid Id of the binary
     * @return ApiResponse<RestFrontBinaryResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun frontServeBinaryWithHttpInfo(binaryType: kotlin.String, uuid: kotlin.String) : ApiResponse<RestFrontBinaryResponse?> {
        val localVariableConfig = frontServeBinaryRequestConfig(binaryType = binaryType, uuid = uuid)

        return request<Unit, RestFrontBinaryResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation frontServeBinary
     *
     * @param binaryType Currently supported values are USER and GLOBAL
     * @param uuid Id of the binary
     * @return RequestConfig
     */
    fun frontServeBinaryRequestConfig(binaryType: kotlin.String, uuid: kotlin.String) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/frontend/binaries/{BinaryType}/{Uuid}".replace("{"+"BinaryType"+"}", encodeURIComponent(binaryType.toString())).replace("{"+"Uuid"+"}", encodeURIComponent(uuid.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Handle JWT
     * 
     * @param body 
     * @return RestFrontSessionResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun frontSession(body: RestFrontSessionRequest) : RestFrontSessionResponse {
        val localVarResponse = frontSessionWithHttpInfo(body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestFrontSessionResponse
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
     * Handle JWT
     * 
     * @param body 
     * @return ApiResponse<RestFrontSessionResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun frontSessionWithHttpInfo(body: RestFrontSessionRequest) : ApiResponse<RestFrontSessionResponse?> {
        val localVariableConfig = frontSessionRequestConfig(body = body)

        return request<RestFrontSessionRequest, RestFrontSessionResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation frontSession
     *
     * @param body 
     * @return RequestConfig
     */
    fun frontSessionRequestConfig(body: RestFrontSessionRequest) : RequestConfig<RestFrontSessionRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/frontend/session",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Send XML state registry
     * 
     * @param xpath  (optional)
     * @return RestFrontStateResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun frontState(xpath: kotlin.String? = null) : RestFrontStateResponse {
        val localVarResponse = frontStateWithHttpInfo(xpath = xpath)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestFrontStateResponse
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
     * Send XML state registry
     * 
     * @param xpath  (optional)
     * @return ApiResponse<RestFrontStateResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun frontStateWithHttpInfo(xpath: kotlin.String?) : ApiResponse<RestFrontStateResponse?> {
        val localVariableConfig = frontStateRequestConfig(xpath = xpath)

        return request<Unit, RestFrontStateResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation frontState
     *
     * @param xpath  (optional)
     * @return RequestConfig
     */
    fun frontStateRequestConfig(xpath: kotlin.String?) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf<kotlin.String, kotlin.collections.List<kotlin.String>>()
            .apply {
                if (xpath != null) {
                    put("XPath", listOf(xpath.toString()))
                }
            }
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/frontend/state",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Sends a tree of nodes to be used a menu in the Settings panel
     * 
     * @return RestSettingsMenuResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun settingsMenu() : RestSettingsMenuResponse {
        val localVarResponse = settingsMenuWithHttpInfo()

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestSettingsMenuResponse
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
     * Sends a tree of nodes to be used a menu in the Settings panel
     * 
     * @return ApiResponse<RestSettingsMenuResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun settingsMenuWithHttpInfo() : ApiResponse<RestSettingsMenuResponse?> {
        val localVariableConfig = settingsMenuRequestConfig()

        return request<Unit, RestSettingsMenuResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation settingsMenu
     *
     * @return RequestConfig
     */
    fun settingsMenuRequestConfig() : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/frontend/settings-menu",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }


    private fun encodeURIComponent(uriComponent: kotlin.String): kotlin.String =
        HttpUrl.Builder().scheme("http").host("localhost").addPathSegment(uriComponent).build().encodedPathSegments[0]
}
