package org.sinou.pydia.sdk.transport.auth

import okhttp3.Interceptor
import org.sinou.pydia.sdk.transport.auth.AuthNames.Companion.AUTH_HEADER
import org.sinou.pydia.sdk.transport.auth.AuthNames.Companion.DEFAULT_TOKEN_TYPE
import org.sinou.pydia.sdk.transport.auth.AuthNames.Companion.USER_AGENT_HEADER

class CellsOAuthInterceptor(
    private val userAgent: String,
    private val getToken: () -> String,
) : Interceptor {

//    private val logTag = "CellsOAuthInterceptor"

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var builder = chain.request().newBuilder()

        builder = builder.addHeader(USER_AGENT_HEADER, userAgent)
            .addHeader(AUTH_HEADER, "$DEFAULT_TOKEN_TYPE ${getToken()}")

//        userAgent?.let {
//            builder = builder.addHeader(USER_AGENT_HEADER, it)
//        }
//
//        getToken()?.let {
//            Log.e(logTag, " Adding '$AUTH_HEADER' Header: [$TOKEN_TYPE $it]")
//            builder = builder.addHeader(AUTH_HEADER, "$TOKEN_TYPE $it")
//        }

        return chain.proceed(builder.build())
    }
}