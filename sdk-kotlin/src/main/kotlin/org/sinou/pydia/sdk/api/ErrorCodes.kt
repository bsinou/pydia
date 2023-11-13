package org.sinou.pydia.sdk.api

/**
 * Custom Error Codes for the Cells SDK.
 * Error codes between 100 and 600 are the standard HTTP status code that are returned
 * by the underlying generated openAPI SDK.
 */
interface ErrorCodes {
    companion object {
        /**
         * Returns a message given its code
         * TODO: externalise strings and add i18n
         *
         * @param code the message code number
         * @return the corresponding human readable message (not yet i18ned)
         */
        @JvmStatic
        fun toMessage(code: Int): String? {
            when (code) {
                ok -> return "OK"
                resource_found -> return "Resource found"
                no_internet -> return "No internet"
                not_found -> return "Not found"
                redirect -> return "Redirect"
                bad_uri -> return "Bad URI"
                unreachable_host -> return "Unreachable host"
                not_pydio_server -> return "Not a Pydio server"
                pydio_server_not_supported -> return "Pydio server is not supported"
                unsupported_method -> return "Unsupported method"
                unsupported_scheme -> return "Unsupported scheme"
                ssl_error -> return "TLS error"
                ssl_certificate_not_signed -> return "TLS certificate not signed"
                tls_init -> return "TLS init"
                invalid_credentials -> return "Invalid credentials"
                authentication_required -> return "Authentication required"
                no_token_available -> return "No token available"
                token_expired -> return "Token expired"
                authentication_with_captcha_required -> return "Authentication with captcha required"
                refresh_token_expired -> return "Refresh token has expired"
                con_failed -> return "Connection failed"
                con_closed -> return "Connection closed"
                con_read_failed -> return "Connection read failed"
                con_write_failed -> return "Connection write failed"
                write_failed_no_space -> return "No space left on device"
                server_configuration_issue -> return "Could not retrieve Auth info, please check your server config"
                unexpected_content -> return "Unexpected content"
                unexpected_response -> return "Unexpected response"
                no_local_file -> return "File cannot be found locally"
                no_local_node -> return "Record could not be found in local index"
                local_io_error -> return "Could not read/write in local application directory"
                outdated_local_file -> return "Local file is outdated"
                api_error -> return "API error"
                timeout -> return "Timeout reached"
                panic -> return "Panic"
                internal_error -> return "Internal server error"
                init_failed -> return "Initialisation failed"
                bad_config -> return "Bad configuration"
                encoding_failed -> return "Encoding failed"
                illegal_argument -> return "Illegal argument"
            }
            return "Unknown exception (code: $code)"
        }

        fun fromHttpStatus(status: Int): Int {
            when (status) {
                200 -> return ok
                401, 403 -> return authentication_required
                404 -> return not_found
            }
            return unexpected_response
        }

        const val ok = 0
        const val resource_found = 1
        const val cancelled = 2
        const val no_internet = 10

        //    int no_un_metered_connection = 11;
        //    int no_metered_connection = 12;
        const val not_found = 13
        const val redirect = 14
        const val bad_uri = 15
        const val unreachable_host = 16
        const val not_pydio_server = 20
        const val pydio_server_not_supported = 21
        const val unsupported_method = 22
        const val unsupported_scheme = 23
        const val ssl_error = 24
        const val ssl_certificate_not_signed = 25
        const val tls_init = 26

        //    int unknown_account = 30;
        const val invalid_credentials = 31
        const val authentication_required = 32
        const val authentication_with_captcha_required = 33
        const val token_expired = 34
        const val no_token_available = 35
        const val refresh_token_expired = 36
        const val cannot_refresh_token = 37
        const val refresh_token_not_valid = 38
        const val con_failed = 40
        const val con_closed = 41
        const val con_read_failed = 42
        const val con_write_failed = 43
        const val unexpected_content = 44
        const val unexpected_response = 45
        const val write_failed_no_space = 46
        const val server_configuration_issue = 47
        const val api_error = 50
        const val unsupported = 51
        const val timeout = 52
        const val no_local_file = 60
        const val no_local_node = 61
        const val local_io_error = 62
        const val outdated_local_file = 63
        const val panic = 80
        const val internal_error = 81
        const val init_failed = 82
        const val configuration_error = 83
        const val bad_config = 84
        const val encoding_failed = 85
        const val illegal_argument = 86
    }
}