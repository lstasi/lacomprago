package com.lacomprago.data.api

import com.google.gson.Gson
import com.lacomprago.data.api.model.CartRequest
import com.lacomprago.data.api.model.CartResponse
import com.lacomprago.data.api.model.OrderDetailsResponse
import com.lacomprago.data.api.model.OrderListResponse
import com.lacomprago.data.api.model.OrderResult
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
     * @param customerId The customer ID
     * @param page The page number (default 1)
     * @return OrderListResponse containing results and pagination info
     * @throws ApiException if the request fails
     */
    suspend fun getOrderList(customerId: String, page: Int = 1): OrderListResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}api/customers/$customerId/orders/?lang=es&wh=bcn1&page=$page")
            .get()
            .build()
        
        httpClient.newCall(request).execute().use { response ->
            handleResponse<OrderListResponse>(response, "Failed to fetch orders")
        }
    }
    
    /**
     * Get all orders from all pages.
     *
     * @param customerId The customer ID
     * @return List of all order results from all pages
     * @throws ApiException if the request fails
     */
    suspend fun getAllOrders(customerId: String): List<OrderResult> = withContext(Dispatchers.IO) {
        val allOrders = mutableListOf<OrderResult>()
        var page = 1
        var hasMorePages = true
        
        while (hasMorePages) {
            val response = getOrderList(customerId, page)
            allOrders.addAll(response.results)
            
            hasMorePages = response.nextPage != null
            page++
        }
        
        allOrders
    }
    
    /**
     * Get details of a specific order including product items.
     *
     * @param customerId The customer ID
     * @param orderId The ID of the order to fetch
     * @return Order details with product items
     * @throws ApiException if the request fails
     */
    suspend fun getOrderDetails(customerId: String, orderId: String): OrderDetailsResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}api/customers/$customerId/orders/$orderId/?lang=es&wh=bcn1")
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
