package com.lacomprago.data.api

import com.google.gson.Gson
import com.lacomprago.data.api.model.CartRequest
import com.lacomprago.data.api.model.CartResponse
import com.lacomprago.data.api.model.OrderListResponse
import com.lacomprago.data.api.model.OrderResponse
import com.lacomprago.data.api.model.OrderSummary
import com.lacomprago.data.api.model.ValidateTokenResponse
import com.lacomprago.storage.TokenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * API Client for making HTTP requests to the supermarket API.
 * Uses OkHttp for HTTP communication and Gson for JSON serialization.
 *
 * @property httpClient The OkHttpClient instance for making requests
 * @property gson Gson instance for JSON serialization/deserialization
 */
class ApiClient(
    private val httpClient: OkHttpClient,
    private val gson: Gson = Gson()
) {
    
    /**
     * Validate a token with the API.
     *
     * @param token The token to validate
     * @return true if the token is valid, false otherwise
     * @throws ApiException if the request fails
     */
    suspend fun validateToken(token: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}api/validate")
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        
        httpClient.newCall(request).execute().use { response ->
            when {
                response.isSuccessful -> {
                    val json = response.body?.string()
                        ?: throw ApiException("Empty response body", response.code)
                    val validateResponse = gson.fromJson(json, ValidateTokenResponse::class.java)
                    validateResponse.valid
                }
                response.code == 401 -> false
                else -> throw ApiException("Token validation failed: ${response.code}", response.code)
            }
        }
    }
    
    /**
     * Get the list of orders from the API.
     *
     * @return List of order summaries
     * @throws ApiException if the request fails
     */
    suspend fun getOrderList(): List<OrderSummary> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}api/orders")
            .get()
            .build()
        
        httpClient.newCall(request).execute().use { response ->
            handleResponse<OrderListResponse>(response, "Failed to fetch orders")
        }.orders
    }
    
    /**
     * Get details of a specific order.
     *
     * @param orderId The ID of the order to fetch
     * @return Full order details
     * @throws ApiException if the request fails
     */
    suspend fun getOrderDetails(orderId: String): OrderResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}api/orders/$orderId")
            .get()
            .build()
        
        httpClient.newCall(request).execute().use { response ->
            handleResponse(response, "Failed to fetch order $orderId")
        }
    }
    
    /**
     * Create a shopping cart with the API.
     *
     * @param cartRequest The cart items to create
     * @return The created cart response
     * @throws ApiException if the request fails
     */
    suspend fun createCart(cartRequest: CartRequest): CartResponse = withContext(Dispatchers.IO) {
        val json = gson.toJson(cartRequest)
        val body = json.toRequestBody(MEDIA_TYPE_JSON)
        
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}api/cart")
            .post(body)
            .build()
        
        httpClient.newCall(request).execute().use { response ->
            handleResponse(response, "Failed to create cart")
        }
    }
    
    /**
     * Handle the response and parse JSON to the expected type.
     *
     * @param response The HTTP response
     * @param errorMessage Error message prefix for failures
     * @return Parsed response object
     * @throws ApiException if the response is unsuccessful
     */
    private inline fun <reified T> handleResponse(response: Response, errorMessage: String): T {
        if (!response.isSuccessful) {
            throw ApiException("$errorMessage: ${response.code}", response.code)
        }
        
        val json = response.body?.string()
            ?: throw ApiException("Empty response body", response.code)
        
        return gson.fromJson(json, T::class.java)
    }
    
    companion object {
        private val MEDIA_TYPE_JSON = "application/json".toMediaType()
        
        /**
         * Create an OkHttpClient configured for API calls.
         *
         * @param tokenStorage Token storage for authentication
         * @return Configured OkHttpClient
         */
        fun createHttpClient(tokenStorage: TokenStorage): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(TokenInterceptor(tokenStorage))
                .build()
        }
        
        /**
         * Create a fully configured ApiClient.
         *
         * @param tokenStorage Token storage for authentication
         * @return Configured ApiClient
         */
        fun create(tokenStorage: TokenStorage): ApiClient {
            return ApiClient(createHttpClient(tokenStorage))
        }
    }
}
