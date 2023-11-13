package org.sinou.pydia.sdk.api

import org.sinou.pydia.openapi.infrastructure.ServerException
import org.sinou.pydia.sdk.api.ErrorCodes.Companion.toMessage
import java.io.IOException

/**
 * Generic exception for the java SDK that is server type agnostic and supports both Pydio Cells and the legacy Pydio 8 server.
 * It cannot yet extend the swagger generated API exception that is specific to Cells.
 *
 *
 * This can be changed when we stop supporting Pydio 8.
 *
 *
 * // TODO smelly code, refactor with a clean generic error handling strategy.
 */
open class SDKException : Exception {
    var code = 0
        private set

    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    val isAuthorizationError: Boolean
        /**
         * Returns true if the current exception code is one of the well-known code for authorization issue
         */
        get() = code == ErrorCodes.no_token_available || code == ErrorCodes.authentication_required || code == ErrorCodes.refresh_token_expired || code == ErrorCodes.token_expired || code == ErrorCodes.authentication_with_captcha_required || code == HttpStatus.UNAUTHORIZED.value
    val isNetworkError: Boolean
        get() = code == ErrorCodes.unreachable_host || code == ErrorCodes.no_internet || code == ErrorCodes.con_failed || code == ErrorCodes.con_closed || code == ErrorCodes.con_read_failed || code == ErrorCodes.con_write_failed || code == HttpStatus.BAD_GATEWAY.value || code == HttpStatus.SERVICE_UNAVAILABLE.value || code == HttpStatus.GATEWAY_TIMEOUT.value

    // Legacy inherited SDK specific constructors => ease implementation of the Android app.
    constructor(code: Int, message: String?, cause: Throwable?) : this(message, cause) {
        this.code = code
    }

    constructor(code: Int, message: String?) : this(message) {
        this.code = code
    }

    constructor(code: Int, cause: Exception?) : this(toMessage(code), cause) {
        this.code = code
    }

    constructor(code: Int) : this(toMessage(code)) {
        this.code = code
    }

    // FIXME factorize and clean exception we have added 2 times the same object
    class RemoteIOException(s: String?) : SDKException(ErrorCodes.con_failed, s)
    internal class IO : SDKException {
        constructor(code: Int, message: String?, cause: Throwable?) : super(code, message, cause)
        constructor(message: String?, cause: Throwable?) : super(message, cause)
    }

    internal inner class Api : SDKException()
    internal inner class Auth : SDKException() {}

    @Suppress("unused")
    private constructor()

    companion object {
        private const val logTag = "SDKException"
        fun fromServerException(cause: ServerException): SDKException {

            return SDKException(cause.statusCode, "Unexpected Server Exception", cause)
            // FIXME this must be refined after migration to Kotlin
//            val body = cause.response?.let {  }responseBody
//            val headers = cause.responseHeaders
//            return if (notEmpty(body)) {
//                if (headers.containsKey("content-type") && headers["content-type"]!![0].startsWith("application/json")) {
//                    val gson = Gson()
//                    // Best effort to gather more info about the error
//                    try {
//                        val responseBody: HashMap<String, String> =
//                            gson.fromJson(body, HashMap::class.java) as HashMap<String, String>
//                        return SDKException(cause.code, responseBody["Title"], cause)
//                    } catch (je: Exception) {
//                        Log.w(logTag, "can't get info from JSON response body: ${je.message}")
//                    }
//                    SDKException(cause.code, "JSON parse error", cause)
//                    // if (headers.containsKey("content-type") && headers.get("content-type").get(0).startsWith("text/plain")) {
//                } else {
//                    SDKException(cause.code, body, cause)
//                }
//            } else if (cause.cause is UnknownHostException ||
//                cause.cause is SocketException ||
//                cause.cause is IOException
//            ) {
//                // Probably network error
//                SDKException(ErrorCodes.con_failed, cause.cause!!.message, cause.cause)
//            } else {
//                SDKException(
//                    cause.code,
//                    "Unhandled ApiException #" + cause.code + ": " + cause.message,
//                    cause
//                )
//            }
        }

        /* Boiler plate shortcuts */
        fun cancel(message: String?): SDKException {
            return SDKException(ErrorCodes.cancelled, message)
        }

        fun isCancellation(e: SDKException): Boolean {
            return ErrorCodes.cancelled == e.code
        }

        fun malFormURI(e: Exception?): SDKException {
            return SDKException(ErrorCodes.bad_uri, e)
        }

        fun encoding(e: Exception?): SDKException {
            return SDKException(ErrorCodes.encoding_failed, e)
        }

        fun noSpaceLeft(e: IOException?): SDKException {
            return SDKException(ErrorCodes.write_failed_no_space, e)
        }

        fun unexpectedContent(e: Exception?): SDKException {
            return SDKException(ErrorCodes.unexpected_content, e)
        }

        fun notFound(e: Exception?): SDKException {
            return SDKException(ErrorCodes.not_found, e)
        }

        fun conFailed(message: String?, e: IOException?): SDKException {
            return IO(ErrorCodes.con_failed, message, e)
        }

        fun conReadFailed(message: String?, e: IOException?): SDKException {
            return IO(ErrorCodes.con_read_failed, message, e)
        }

        fun conWriteFailed(message: String?, e: IOException?): SDKException {
            return IO(ErrorCodes.con_write_failed, message, e)
        }
    }
}
