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

import org.sinou.pydia.openapi.model.IdmUser
import org.sinou.pydia.openapi.model.RestDeleteResponse
import org.sinou.pydia.openapi.model.RestSearchUserRequest
import org.sinou.pydia.openapi.model.RestUsersCollection
import org.sinou.pydia.openapi.model.UserCanRepresentEitherAUserOrAGroup

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

class UserServiceApi(basePath: kotlin.String = defaultBasePath, client: OkHttpClient = ApiClient.defaultClient) : ApiClient(basePath, client) {
    companion object {
        @JvmStatic
        val defaultBasePath: String by lazy {
            System.getProperties().getProperty(ApiClient.baseUrlKey, "http://localhost")
        }
    }

    /**
     * Delete a user
     * 
     * @param login User login is used to connect, field is empty for groups
     * @param uuid User unique identifier (optional)
     * @param groupPath Path to the parent group (optional)
     * @param password Password can be passed to be updated (but never read back), field is empty for groups (optional)
     * @param oldPassword OldPassword must be set when a user updates her own password (optional)
     * @param isGroup Whether this object is a group or a user (optional)
     * @param groupLabel Label of the group, field is empty for users (optional)
     * @param lastConnected Last successful connection timestamp (optional)
     * @param policiesContextEditable Context-resolved to quickly check if user is editable or not. (optional)
     * @return RestDeleteResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun deleteUser(login: kotlin.String, uuid: kotlin.String? = null, groupPath: kotlin.String? = null, password: kotlin.String? = null, oldPassword: kotlin.String? = null, isGroup: kotlin.Boolean? = null, groupLabel: kotlin.String? = null, lastConnected: kotlin.Int? = null, policiesContextEditable: kotlin.Boolean? = null) : RestDeleteResponse {
        val localVarResponse = deleteUserWithHttpInfo(login = login, uuid = uuid, groupPath = groupPath, password = password, oldPassword = oldPassword, isGroup = isGroup, groupLabel = groupLabel, lastConnected = lastConnected, policiesContextEditable = policiesContextEditable)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestDeleteResponse
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
     * Delete a user
     * 
     * @param login User login is used to connect, field is empty for groups
     * @param uuid User unique identifier (optional)
     * @param groupPath Path to the parent group (optional)
     * @param password Password can be passed to be updated (but never read back), field is empty for groups (optional)
     * @param oldPassword OldPassword must be set when a user updates her own password (optional)
     * @param isGroup Whether this object is a group or a user (optional)
     * @param groupLabel Label of the group, field is empty for users (optional)
     * @param lastConnected Last successful connection timestamp (optional)
     * @param policiesContextEditable Context-resolved to quickly check if user is editable or not. (optional)
     * @return ApiResponse<RestDeleteResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun deleteUserWithHttpInfo(login: kotlin.String, uuid: kotlin.String?, groupPath: kotlin.String?, password: kotlin.String?, oldPassword: kotlin.String?, isGroup: kotlin.Boolean?, groupLabel: kotlin.String?, lastConnected: kotlin.Int?, policiesContextEditable: kotlin.Boolean?) : ApiResponse<RestDeleteResponse?> {
        val localVariableConfig = deleteUserRequestConfig(login = login, uuid = uuid, groupPath = groupPath, password = password, oldPassword = oldPassword, isGroup = isGroup, groupLabel = groupLabel, lastConnected = lastConnected, policiesContextEditable = policiesContextEditable)

        return request<Unit, RestDeleteResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation deleteUser
     *
     * @param login User login is used to connect, field is empty for groups
     * @param uuid User unique identifier (optional)
     * @param groupPath Path to the parent group (optional)
     * @param password Password can be passed to be updated (but never read back), field is empty for groups (optional)
     * @param oldPassword OldPassword must be set when a user updates her own password (optional)
     * @param isGroup Whether this object is a group or a user (optional)
     * @param groupLabel Label of the group, field is empty for users (optional)
     * @param lastConnected Last successful connection timestamp (optional)
     * @param policiesContextEditable Context-resolved to quickly check if user is editable or not. (optional)
     * @return RequestConfig
     */
    fun deleteUserRequestConfig(login: kotlin.String, uuid: kotlin.String?, groupPath: kotlin.String?, password: kotlin.String?, oldPassword: kotlin.String?, isGroup: kotlin.Boolean?, groupLabel: kotlin.String?, lastConnected: kotlin.Int?, policiesContextEditable: kotlin.Boolean?) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf<kotlin.String, kotlin.collections.List<kotlin.String>>()
            .apply {
                if (uuid != null) {
                    put("Uuid", listOf(uuid.toString()))
                }
                if (groupPath != null) {
                    put("GroupPath", listOf(groupPath.toString()))
                }
                if (password != null) {
                    put("Password", listOf(password.toString()))
                }
                if (oldPassword != null) {
                    put("OldPassword", listOf(oldPassword.toString()))
                }
                if (isGroup != null) {
                    put("IsGroup", listOf(isGroup.toString()))
                }
                if (groupLabel != null) {
                    put("GroupLabel", listOf(groupLabel.toString()))
                }
                if (lastConnected != null) {
                    put("LastConnected", listOf(lastConnected.toString()))
                }
                if (policiesContextEditable != null) {
                    put("PoliciesContextEditable", listOf(policiesContextEditable.toString()))
                }
            }
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.DELETE,
            path = "/user/{Login}".replace("{"+"Login"+"}", encodeURIComponent(login.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Get a user by login
     * 
     * @param login User login is used to connect, field is empty for groups
     * @param uuid User unique identifier (optional)
     * @param groupPath Path to the parent group (optional)
     * @param password Password can be passed to be updated (but never read back), field is empty for groups (optional)
     * @param oldPassword OldPassword must be set when a user updates her own password (optional)
     * @param isGroup Whether this object is a group or a user (optional)
     * @param groupLabel Label of the group, field is empty for users (optional)
     * @param lastConnected Last successful connection timestamp (optional)
     * @param policiesContextEditable Context-resolved to quickly check if user is editable or not. (optional)
     * @return IdmUser
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun getUser(login: kotlin.String, uuid: kotlin.String? = null, groupPath: kotlin.String? = null, password: kotlin.String? = null, oldPassword: kotlin.String? = null, isGroup: kotlin.Boolean? = null, groupLabel: kotlin.String? = null, lastConnected: kotlin.Int? = null, policiesContextEditable: kotlin.Boolean? = null) : IdmUser {
        val localVarResponse = getUserWithHttpInfo(login = login, uuid = uuid, groupPath = groupPath, password = password, oldPassword = oldPassword, isGroup = isGroup, groupLabel = groupLabel, lastConnected = lastConnected, policiesContextEditable = policiesContextEditable)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as IdmUser
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
     * Get a user by login
     * 
     * @param login User login is used to connect, field is empty for groups
     * @param uuid User unique identifier (optional)
     * @param groupPath Path to the parent group (optional)
     * @param password Password can be passed to be updated (but never read back), field is empty for groups (optional)
     * @param oldPassword OldPassword must be set when a user updates her own password (optional)
     * @param isGroup Whether this object is a group or a user (optional)
     * @param groupLabel Label of the group, field is empty for users (optional)
     * @param lastConnected Last successful connection timestamp (optional)
     * @param policiesContextEditable Context-resolved to quickly check if user is editable or not. (optional)
     * @return ApiResponse<IdmUser?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun getUserWithHttpInfo(login: kotlin.String, uuid: kotlin.String?, groupPath: kotlin.String?, password: kotlin.String?, oldPassword: kotlin.String?, isGroup: kotlin.Boolean?, groupLabel: kotlin.String?, lastConnected: kotlin.Int?, policiesContextEditable: kotlin.Boolean?) : ApiResponse<IdmUser?> {
        val localVariableConfig = getUserRequestConfig(login = login, uuid = uuid, groupPath = groupPath, password = password, oldPassword = oldPassword, isGroup = isGroup, groupLabel = groupLabel, lastConnected = lastConnected, policiesContextEditable = policiesContextEditable)

        return request<Unit, IdmUser>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation getUser
     *
     * @param login User login is used to connect, field is empty for groups
     * @param uuid User unique identifier (optional)
     * @param groupPath Path to the parent group (optional)
     * @param password Password can be passed to be updated (but never read back), field is empty for groups (optional)
     * @param oldPassword OldPassword must be set when a user updates her own password (optional)
     * @param isGroup Whether this object is a group or a user (optional)
     * @param groupLabel Label of the group, field is empty for users (optional)
     * @param lastConnected Last successful connection timestamp (optional)
     * @param policiesContextEditable Context-resolved to quickly check if user is editable or not. (optional)
     * @return RequestConfig
     */
    fun getUserRequestConfig(login: kotlin.String, uuid: kotlin.String?, groupPath: kotlin.String?, password: kotlin.String?, oldPassword: kotlin.String?, isGroup: kotlin.Boolean?, groupLabel: kotlin.String?, lastConnected: kotlin.Int?, policiesContextEditable: kotlin.Boolean?) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf<kotlin.String, kotlin.collections.List<kotlin.String>>()
            .apply {
                if (uuid != null) {
                    put("Uuid", listOf(uuid.toString()))
                }
                if (groupPath != null) {
                    put("GroupPath", listOf(groupPath.toString()))
                }
                if (password != null) {
                    put("Password", listOf(password.toString()))
                }
                if (oldPassword != null) {
                    put("OldPassword", listOf(oldPassword.toString()))
                }
                if (isGroup != null) {
                    put("IsGroup", listOf(isGroup.toString()))
                }
                if (groupLabel != null) {
                    put("GroupLabel", listOf(groupLabel.toString()))
                }
                if (lastConnected != null) {
                    put("LastConnected", listOf(lastConnected.toString()))
                }
                if (policiesContextEditable != null) {
                    put("PoliciesContextEditable", listOf(policiesContextEditable.toString()))
                }
            }
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/user/{Login}".replace("{"+"Login"+"}", encodeURIComponent(login.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Just save a user roles, without other datas
     * 
     * @param login User login is used to connect, field is empty for groups
     * @param body 
     * @return IdmUser
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun putRoles(login: kotlin.String, body: UserCanRepresentEitherAUserOrAGroup) : IdmUser {
        val localVarResponse = putRolesWithHttpInfo(login = login, body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as IdmUser
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
     * Just save a user roles, without other datas
     * 
     * @param login User login is used to connect, field is empty for groups
     * @param body 
     * @return ApiResponse<IdmUser?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun putRolesWithHttpInfo(login: kotlin.String, body: UserCanRepresentEitherAUserOrAGroup) : ApiResponse<IdmUser?> {
        val localVariableConfig = putRolesRequestConfig(login = login, body = body)

        return request<UserCanRepresentEitherAUserOrAGroup, IdmUser>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation putRoles
     *
     * @param login User login is used to connect, field is empty for groups
     * @param body 
     * @return RequestConfig
     */
    fun putRolesRequestConfig(login: kotlin.String, body: UserCanRepresentEitherAUserOrAGroup) : RequestConfig<UserCanRepresentEitherAUserOrAGroup> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.PUT,
            path = "/user/roles/{Login}".replace("{"+"Login"+"}", encodeURIComponent(login.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Create or update a user
     * 
     * @param login User login is used to connect, field is empty for groups
     * @param body 
     * @return IdmUser
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun putUser(login: kotlin.String, body: UserCanRepresentEitherAUserOrAGroup) : IdmUser {
        val localVarResponse = putUserWithHttpInfo(login = login, body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as IdmUser
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
     * Create or update a user
     * 
     * @param login User login is used to connect, field is empty for groups
     * @param body 
     * @return ApiResponse<IdmUser?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun putUserWithHttpInfo(login: kotlin.String, body: UserCanRepresentEitherAUserOrAGroup) : ApiResponse<IdmUser?> {
        val localVariableConfig = putUserRequestConfig(login = login, body = body)

        return request<UserCanRepresentEitherAUserOrAGroup, IdmUser>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation putUser
     *
     * @param login User login is used to connect, field is empty for groups
     * @param body 
     * @return RequestConfig
     */
    fun putUserRequestConfig(login: kotlin.String, body: UserCanRepresentEitherAUserOrAGroup) : RequestConfig<UserCanRepresentEitherAUserOrAGroup> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.PUT,
            path = "/user/{Login}".replace("{"+"Login"+"}", encodeURIComponent(login.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * List/Search users
     * 
     * @param body 
     * @return RestUsersCollection
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun searchUsers(body: RestSearchUserRequest) : RestUsersCollection {
        val localVarResponse = searchUsersWithHttpInfo(body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestUsersCollection
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
     * List/Search users
     * 
     * @param body 
     * @return ApiResponse<RestUsersCollection?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun searchUsersWithHttpInfo(body: RestSearchUserRequest) : ApiResponse<RestUsersCollection?> {
        val localVariableConfig = searchUsersRequestConfig(body = body)

        return request<RestSearchUserRequest, RestUsersCollection>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation searchUsers
     *
     * @param body 
     * @return RequestConfig
     */
    fun searchUsersRequestConfig(body: RestSearchUserRequest) : RequestConfig<RestSearchUserRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/user",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }


    private fun encodeURIComponent(uriComponent: kotlin.String): kotlin.String =
        HttpUrl.Builder().scheme("http").host("localhost").addPathSegment(uriComponent).build().encodedPathSegments[0]
}
