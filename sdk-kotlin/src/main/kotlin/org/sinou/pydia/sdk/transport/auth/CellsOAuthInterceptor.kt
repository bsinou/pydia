package org.sinou.pydia.sdk.transport.auth

import okhttp3.Interceptor

private const val TOKEN_TYPE = "bearer"

class CellsOAuthInterceptor(
    private val getToken: () -> String?,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        getToken()?.let {
            request = request.newBuilder()
                .addHeader("Authorization", "$TOKEN_TYPE $it")
                .build()
        }

        return chain.proceed(request)
    }
}