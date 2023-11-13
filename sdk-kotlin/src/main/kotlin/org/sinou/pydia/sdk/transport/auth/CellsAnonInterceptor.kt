package org.sinou.pydia.sdk.transport.auth

import okhttp3.Interceptor
import org.sinou.pydia.sdk.transport.auth.AuthNames.Companion.USER_AGENT_HEADER

class CellsAnonInterceptor(
    private val userAgent: String
) : Interceptor {

//    private val logTag = "CellsAnonInterceptor"

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var builder = chain.request().newBuilder()
        builder = builder.addHeader(USER_AGENT_HEADER, userAgent)
        return chain.proceed(builder.build())
    }
}