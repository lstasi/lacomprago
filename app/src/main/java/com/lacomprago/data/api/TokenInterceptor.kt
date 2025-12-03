package com.lacomprago.data.api

import com.lacomprago.storage.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor that adds authentication token to requests.
 * Automatically clears token on 401 Unauthorized responses.
 *
 * @property tokenStorage Token storage for retrieving and clearing tokens
 */
class TokenInterceptor(
    private val tokenStorage: TokenStorage
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenStorage.getToken()
        
        // Build the request, adding token if available
        val request = if (token != null) {
            original.newBuilder()
                .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$token")
                .build()
        } else {
            original
        }
        
        // Execute the request
        val response = chain.proceed(request)
        
        // Handle 401 Unauthorized - clear the invalid token
        if (response.code == HTTP_UNAUTHORIZED) {
            tokenStorage.clearToken()
        }
        
        return response
    }
    
    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val HTTP_UNAUTHORIZED = 401
    }
}
