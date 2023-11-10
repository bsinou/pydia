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

import org.sinou.pydia.openapi.model.IdmRole
import org.sinou.pydia.openapi.model.RestError
import org.sinou.pydia.openapi.model.RestRolesCollection
import org.sinou.pydia.openapi.model.RestSearchRoleRequest
import org.sinou.pydia.openapi.model.SetRoleRequest

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

class RoleServiceApi(basePath: kotlin.String = defaultBasePath, client: OkHttpClient = ApiClient.defaultClient) : ApiClient(basePath, client) {
    companion object {
        @JvmStatic
        val defaultBasePath: String by lazy {
            System.getProperties().getProperty(ApiClient.baseUrlKey, "http://localhost")
        }
    }

    /**
     * Delete a Role by ID
     * 
     * @param uuid Unique identifier of this role
     * @param label Label of this role (optional)
     * @param isTeam Whether this role represents a user team or not (optional)
     * @param groupRole Whether this role is attached to a Group object (optional)
     * @param userRole Whether this role is attached to a User object (optional)
     * @param lastUpdated Last modification date of the role (optional)
     * @param autoApplies List of profiles (standard, shared, admin) on which the role will be automatically applied (optional)
     * @param policiesContextEditable Whether the policies resolve into an editable state (optional)
     * @param forceOverride Is used in a stack of roles, this one will always be applied last. (optional)
     * @return IdmRole
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun deleteRole(uuid: kotlin.String, label: kotlin.String? = null, isTeam: kotlin.Boolean? = null, groupRole: kotlin.Boolean? = null, userRole: kotlin.Boolean? = null, lastUpdated: kotlin.Int? = null, autoApplies: kotlin.collections.List<kotlin.String>? = null, policiesContextEditable: kotlin.Boolean? = null, forceOverride: kotlin.Boolean? = null) : IdmRole {
        val localVarResponse = deleteRoleWithHttpInfo(uuid = uuid, label = label, isTeam = isTeam, groupRole = groupRole, userRole = userRole, lastUpdated = lastUpdated, autoApplies = autoApplies, policiesContextEditable = policiesContextEditable, forceOverride = forceOverride)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as IdmRole
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
     * Delete a Role by ID
     * 
     * @param uuid Unique identifier of this role
     * @param label Label of this role (optional)
     * @param isTeam Whether this role represents a user team or not (optional)
     * @param groupRole Whether this role is attached to a Group object (optional)
     * @param userRole Whether this role is attached to a User object (optional)
     * @param lastUpdated Last modification date of the role (optional)
     * @param autoApplies List of profiles (standard, shared, admin) on which the role will be automatically applied (optional)
     * @param policiesContextEditable Whether the policies resolve into an editable state (optional)
     * @param forceOverride Is used in a stack of roles, this one will always be applied last. (optional)
     * @return ApiResponse<IdmRole?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun deleteRoleWithHttpInfo(uuid: kotlin.String, label: kotlin.String?, isTeam: kotlin.Boolean?, groupRole: kotlin.Boolean?, userRole: kotlin.Boolean?, lastUpdated: kotlin.Int?, autoApplies: kotlin.collections.List<kotlin.String>?, policiesContextEditable: kotlin.Boolean?, forceOverride: kotlin.Boolean?) : ApiResponse<IdmRole?> {
        val localVariableConfig = deleteRoleRequestConfig(uuid = uuid, label = label, isTeam = isTeam, groupRole = groupRole, userRole = userRole, lastUpdated = lastUpdated, autoApplies = autoApplies, policiesContextEditable = policiesContextEditable, forceOverride = forceOverride)

        return request<Unit, IdmRole>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation deleteRole
     *
     * @param uuid Unique identifier of this role
     * @param label Label of this role (optional)
     * @param isTeam Whether this role represents a user team or not (optional)
     * @param groupRole Whether this role is attached to a Group object (optional)
     * @param userRole Whether this role is attached to a User object (optional)
     * @param lastUpdated Last modification date of the role (optional)
     * @param autoApplies List of profiles (standard, shared, admin) on which the role will be automatically applied (optional)
     * @param policiesContextEditable Whether the policies resolve into an editable state (optional)
     * @param forceOverride Is used in a stack of roles, this one will always be applied last. (optional)
     * @return RequestConfig
     */
    fun deleteRoleRequestConfig(uuid: kotlin.String, label: kotlin.String?, isTeam: kotlin.Boolean?, groupRole: kotlin.Boolean?, userRole: kotlin.Boolean?, lastUpdated: kotlin.Int?, autoApplies: kotlin.collections.List<kotlin.String>?, policiesContextEditable: kotlin.Boolean?, forceOverride: kotlin.Boolean?) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf<kotlin.String, kotlin.collections.List<kotlin.String>>()
            .apply {
                if (label != null) {
                    put("Label", listOf(label.toString()))
                }
                if (isTeam != null) {
                    put("IsTeam", listOf(isTeam.toString()))
                }
                if (groupRole != null) {
                    put("GroupRole", listOf(groupRole.toString()))
                }
                if (userRole != null) {
                    put("UserRole", listOf(userRole.toString()))
                }
                if (lastUpdated != null) {
                    put("LastUpdated", listOf(lastUpdated.toString()))
                }
                if (autoApplies != null) {
                    put("AutoApplies", toMultiValue(autoApplies.toList(), "multi"))
                }
                if (policiesContextEditable != null) {
                    put("PoliciesContextEditable", listOf(policiesContextEditable.toString()))
                }
                if (forceOverride != null) {
                    put("ForceOverride", listOf(forceOverride.toString()))
                }
            }
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.DELETE,
            path = "/role/{Uuid}".replace("{"+"Uuid"+"}", encodeURIComponent(uuid.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Get a Role by ID
     * 
     * @param uuid Unique identifier of this role
     * @param label Label of this role (optional)
     * @param isTeam Whether this role represents a user team or not (optional)
     * @param groupRole Whether this role is attached to a Group object (optional)
     * @param userRole Whether this role is attached to a User object (optional)
     * @param lastUpdated Last modification date of the role (optional)
     * @param autoApplies List of profiles (standard, shared, admin) on which the role will be automatically applied (optional)
     * @param policiesContextEditable Whether the policies resolve into an editable state (optional)
     * @param forceOverride Is used in a stack of roles, this one will always be applied last. (optional)
     * @return IdmRole
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun getRole(uuid: kotlin.String, label: kotlin.String? = null, isTeam: kotlin.Boolean? = null, groupRole: kotlin.Boolean? = null, userRole: kotlin.Boolean? = null, lastUpdated: kotlin.Int? = null, autoApplies: kotlin.collections.List<kotlin.String>? = null, policiesContextEditable: kotlin.Boolean? = null, forceOverride: kotlin.Boolean? = null) : IdmRole {
        val localVarResponse = getRoleWithHttpInfo(uuid = uuid, label = label, isTeam = isTeam, groupRole = groupRole, userRole = userRole, lastUpdated = lastUpdated, autoApplies = autoApplies, policiesContextEditable = policiesContextEditable, forceOverride = forceOverride)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as IdmRole
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
     * Get a Role by ID
     * 
     * @param uuid Unique identifier of this role
     * @param label Label of this role (optional)
     * @param isTeam Whether this role represents a user team or not (optional)
     * @param groupRole Whether this role is attached to a Group object (optional)
     * @param userRole Whether this role is attached to a User object (optional)
     * @param lastUpdated Last modification date of the role (optional)
     * @param autoApplies List of profiles (standard, shared, admin) on which the role will be automatically applied (optional)
     * @param policiesContextEditable Whether the policies resolve into an editable state (optional)
     * @param forceOverride Is used in a stack of roles, this one will always be applied last. (optional)
     * @return ApiResponse<IdmRole?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun getRoleWithHttpInfo(uuid: kotlin.String, label: kotlin.String?, isTeam: kotlin.Boolean?, groupRole: kotlin.Boolean?, userRole: kotlin.Boolean?, lastUpdated: kotlin.Int?, autoApplies: kotlin.collections.List<kotlin.String>?, policiesContextEditable: kotlin.Boolean?, forceOverride: kotlin.Boolean?) : ApiResponse<IdmRole?> {
        val localVariableConfig = getRoleRequestConfig(uuid = uuid, label = label, isTeam = isTeam, groupRole = groupRole, userRole = userRole, lastUpdated = lastUpdated, autoApplies = autoApplies, policiesContextEditable = policiesContextEditable, forceOverride = forceOverride)

        return request<Unit, IdmRole>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation getRole
     *
     * @param uuid Unique identifier of this role
     * @param label Label of this role (optional)
     * @param isTeam Whether this role represents a user team or not (optional)
     * @param groupRole Whether this role is attached to a Group object (optional)
     * @param userRole Whether this role is attached to a User object (optional)
     * @param lastUpdated Last modification date of the role (optional)
     * @param autoApplies List of profiles (standard, shared, admin) on which the role will be automatically applied (optional)
     * @param policiesContextEditable Whether the policies resolve into an editable state (optional)
     * @param forceOverride Is used in a stack of roles, this one will always be applied last. (optional)
     * @return RequestConfig
     */
    fun getRoleRequestConfig(uuid: kotlin.String, label: kotlin.String?, isTeam: kotlin.Boolean?, groupRole: kotlin.Boolean?, userRole: kotlin.Boolean?, lastUpdated: kotlin.Int?, autoApplies: kotlin.collections.List<kotlin.String>?, policiesContextEditable: kotlin.Boolean?, forceOverride: kotlin.Boolean?) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf<kotlin.String, kotlin.collections.List<kotlin.String>>()
            .apply {
                if (label != null) {
                    put("Label", listOf(label.toString()))
                }
                if (isTeam != null) {
                    put("IsTeam", listOf(isTeam.toString()))
                }
                if (groupRole != null) {
                    put("GroupRole", listOf(groupRole.toString()))
                }
                if (userRole != null) {
                    put("UserRole", listOf(userRole.toString()))
                }
                if (lastUpdated != null) {
                    put("LastUpdated", listOf(lastUpdated.toString()))
                }
                if (autoApplies != null) {
                    put("AutoApplies", toMultiValue(autoApplies.toList(), "multi"))
                }
                if (policiesContextEditable != null) {
                    put("PoliciesContextEditable", listOf(policiesContextEditable.toString()))
                }
                if (forceOverride != null) {
                    put("ForceOverride", listOf(forceOverride.toString()))
                }
            }
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/role/{Uuid}".replace("{"+"Uuid"+"}", encodeURIComponent(uuid.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Search Roles
     * 
     * @param body 
     * @return RestRolesCollection
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun searchRoles(body: RestSearchRoleRequest) : RestRolesCollection {
        val localVarResponse = searchRolesWithHttpInfo(body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as RestRolesCollection
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
     * Search Roles
     * 
     * @param body 
     * @return ApiResponse<RestRolesCollection?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun searchRolesWithHttpInfo(body: RestSearchRoleRequest) : ApiResponse<RestRolesCollection?> {
        val localVariableConfig = searchRolesRequestConfig(body = body)

        return request<RestSearchRoleRequest, RestRolesCollection>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation searchRoles
     *
     * @param body 
     * @return RequestConfig
     */
    fun searchRolesRequestConfig(body: RestSearchRoleRequest) : RequestConfig<RestSearchRoleRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/role",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Create or update a Role
     * 
     * @param uuid Unique identifier of this role
     * @param body 
     * @return IdmRole
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun setRole(uuid: kotlin.String, body: SetRoleRequest) : IdmRole {
        val localVarResponse = setRoleWithHttpInfo(uuid = uuid, body = body)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as IdmRole
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
     * Create or update a Role
     * 
     * @param uuid Unique identifier of this role
     * @param body 
     * @return ApiResponse<IdmRole?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun setRoleWithHttpInfo(uuid: kotlin.String, body: SetRoleRequest) : ApiResponse<IdmRole?> {
        val localVariableConfig = setRoleRequestConfig(uuid = uuid, body = body)

        return request<SetRoleRequest, IdmRole>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation setRole
     *
     * @param uuid Unique identifier of this role
     * @param body 
     * @return RequestConfig
     */
    fun setRoleRequestConfig(uuid: kotlin.String, body: SetRoleRequest) : RequestConfig<SetRoleRequest> {
        val localVariableBody = body
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.PUT,
            path = "/role/{Uuid}".replace("{"+"Uuid"+"}", encodeURIComponent(uuid.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }


    private fun encodeURIComponent(uriComponent: kotlin.String): kotlin.String =
        HttpUrl.Builder().scheme("http").host("localhost").addPathSegment(uriComponent).build().encodedPathSegments[0]
}
