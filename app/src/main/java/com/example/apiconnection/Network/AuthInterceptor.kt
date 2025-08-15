package com.example.apiconnection.Network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenManager.get()
        val newReq = if (!token.isNullOrBlank()) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else request
        return chain.proceed(newReq)
    }
}
