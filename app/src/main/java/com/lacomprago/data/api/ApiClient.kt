package com.lacomprago.data.api

import android.util.Log
import com.google.gson.Gson
import com.lacomprago.BuildConfig
import com.lacomprago.data.api.model.CartLineRequest
import com.lacomprago.data.api.model.CustomerResponse
import com.lacomprago.data.api.model.MercadonaCartRequest
import com.lacomprago.data.api.model.MercadonaCartResponse
import com.lacomprago.data.api.model.OrderLinesResponse
import com.lacomprago.data.api.model.OrderListResponse
import com.lacomprago.data.api.model.OrderResult
import com.lacomprago.data.api.model.RecommendationsResponse
import com.lacomprago.data.api.model.SetWarehouseRequest
import com.lacomprago.data.api.model.SetWarehouseResponse
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
 * API Client for making HTTP requests to the Mercadona API.
 * Uses OkHttp for HTTP communication and Gson for JSON serialization.
 *
 * All requests are rate limited and validated before execution.
 *
 * @property httpClient The OkHttpClient instance for making requests
 * @property gson Gson instance for JSON serialization/deserialization
 * @property rateLimiter Rate limiter to prevent exceeding API limits
 */
class ApiClient(
    private val httpClient: OkHttpClient,
    private val gson: Gson = Gson(),
    private val rateLimiter: RateLimiter = RateLimiter()
) {

    /**
     * Get customer information and validate token.
     *
     * This is the primary method for token validation - if it returns
     * successfully, the token is valid.
     *
     * Endpoint: GET /api/customers/{customer_id}/
     *
     * @param customerId The customer ID
     * @return CustomerResponse with customer details
     * @throws ApiException if the request fails or validation fails
     */
    suspend fun getCustomerInfo(customerId: String): CustomerResponse = withContext(Dispatchers.IO) {
        ApiValidation.validateCustomerId(customerId).throwIfInvalid()
        
        rateLimiter.acquire()
        
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}customers/$customerId/")
            .get()
            .build()
        
        logRequest(request)
        
        httpClient.newCall(request).execute().use { response ->
            logResponse(response)
            handleResponse(response, "Failed to get customer info")
        }
    }

    /**
     * Validate a token by attempting to fetch customer info.
     *
     * @param customerId The customer ID associated with the token
     * @return true if the token is valid, false otherwise
     */
    suspend fun validateToken(customerId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            getCustomerInfo(customerId)
            true
        } catch (e: ApiException) {
            if (e.httpCode == 401 || e.httpCode == 403) {
                false
            } else {
                throw e
            }
        }
    }
    
    /**
     * Get the list of orders from the API.
     *
     * Endpoint: GET /api/customers/{customer_id}/orders/?page={page}
     *
     * @param customerId The customer ID
     * @param page The page number (default 1)
     * @return OrderListResponse containing results and pagination info
     * @throws ApiException if the request fails
     */
    suspend fun getOrderList(customerId: String, page: Int = 1): OrderListResponse = withContext(Dispatchers.IO) {
        ApiValidation.validateCustomerId(customerId).throwIfInvalid()
        
        rateLimiter.acquire()
        
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}customers/$customerId/orders/?page=$page")
            .get()
            .build()
        
        logRequest(request)
        
        httpClient.newCall(request).execute().use { response ->
            logResponse(response)
            handleResponse(response, "Failed to fetch orders")
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
        ApiValidation.validateCustomerId(customerId).throwIfInvalid()
        
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
     * Get the lines (products) of a specific order.
     *
     * Endpoint: GET /api/customers/{customer_id}/orders/{order_id}/lines/prepared/
     *
     * @param customerId The customer ID
     * @param orderId The ID of the order to fetch lines from
     * @return OrderLinesResponse containing the list of products in the order
     * @throws ApiException if the request fails
     */
    suspend fun getOrderLines(customerId: String, orderId: String): OrderLinesResponse = withContext(Dispatchers.IO) {
        ApiValidation.validateCustomerId(customerId).throwIfInvalid()
        ApiValidation.validateOrderId(orderId).throwIfInvalid()

        rateLimiter.acquire()

        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}customers/$customerId/orders/$orderId/lines/prepared/")
            .get()
            .build()

        logRequest(request)

        httpClient.newCall(request).execute().use { response ->
            logResponse(response)
            handleResponse(response, "Failed to fetch order lines for order $orderId")
        }
    }

    /**
     * Get current shopping cart.
     *
     * Endpoint: GET /api/customers/{customer_id}/cart/
     *
     * @param customerId The customer ID
     * @return Cart details including items and summary
     * @throws ApiException if the request fails
     */
    suspend fun getCart(customerId: String): MercadonaCartResponse = withContext(Dispatchers.IO) {
        ApiValidation.validateCustomerId(customerId).throwIfInvalid()
        
        rateLimiter.acquire()
        
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}customers/$customerId/cart/")
            .get()
            .build()
        
        logRequest(request)
        
        httpClient.newCall(request).execute().use { response ->
            logResponse(response)
            handleResponse(response, "Failed to fetch cart")
        }
    }

    /**
     * Update shopping cart with new items.
     *
     * Endpoint: PUT /api/customers/{customer_id}/cart/
     *
     * Important: The version must match the current cart version.
     * Sending an empty lines array clears the cart.
     *
     * @param customerId The customer ID
     * @param cartRequest The cart update request
     * @return Updated cart details
     * @throws ApiException if the request fails
     */
    suspend fun updateCart(customerId: String, cartRequest: MercadonaCartRequest): MercadonaCartResponse = withContext(Dispatchers.IO) {
        ApiValidation.validateCustomerId(customerId).throwIfInvalid()
        
        rateLimiter.acquire()
        
        val json = gson.toJson(cartRequest)
        val body = json.toRequestBody(MEDIA_TYPE_JSON)
        
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}customers/$customerId/cart/")
            .put(body)
            .build()
        
        logRequest(request, json)
        
        httpClient.newCall(request).execute().use { response ->
            logResponse(response)
            handleResponse(response, "Failed to update cart")
        }
    }

    /**
     * Get personalized product recommendations.
     *
     * Endpoint: GET /api/customers/{customer_id}/recommendations/myregulars/{type}/
     *
     * @param customerId The customer ID
     * @param type Type of recommendations: "precision" (what I most buy) or "recall" (I also buy)
     * @return List of recommended products
     * @throws ApiException if the request fails
     */
    suspend fun getRecommendations(customerId: String, type: String = "precision"): RecommendationsResponse = withContext(Dispatchers.IO) {
        ApiValidation.validateCustomerId(customerId).throwIfInvalid()
        ApiValidation.validateRecommendationType(type).throwIfInvalid()
        
        rateLimiter.acquire()
        
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}customers/$customerId/recommendations/myregulars/$type/")
            .get()
            .build()
        
        logRequest(request)
        
        httpClient.newCall(request).execute().use { response ->
            logResponse(response)
            handleResponse(response, "Failed to fetch recommendations")
        }
    }

    /**
     * Set warehouse by postal code.
     *
     * Endpoint: PUT /api/postal-codes/actions/change-pc/
     *
     * @param postalCode The postal code to set
     * @return Whether the warehouse was changed
     * @throws ApiException if the request fails
     */
    suspend fun setWarehouse(postalCode: String): SetWarehouseResponse = withContext(Dispatchers.IO) {
        ApiValidation.validatePostalCode(postalCode).throwIfInvalid()
        
        rateLimiter.acquire()
        
        val setWarehouseRequest = SetWarehouseRequest(postalCode)
        val json = gson.toJson(setWarehouseRequest)
        val body = json.toRequestBody(MEDIA_TYPE_JSON)
        
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}postal-codes/actions/change-pc/")
            .put(body)
            .build()
        
        logRequest(request, json)
        
        httpClient.newCall(request).execute().use { response ->
            logResponse(response)
            handleResponse(response, "Failed to set warehouse")
        }
    }

    /**
     * Execute a raw API request for debug purposes.
     *
     * @param method HTTP method (GET, POST, PUT, DELETE)
     * @param path API path (without base URL)
     * @param queryParams Query parameters
     * @param body Request body for POST/PUT
     * @return DebugApiResponse with full request/response details
     */
    suspend fun executeDebugRequest(
        method: String,
        path: String,
        queryParams: Map<String, String> = emptyMap(),
        body: String? = null
    ): DebugApiResponse = withContext(Dispatchers.IO) {
        rateLimiter.acquire()
        
        val urlBuilder = StringBuilder("${ApiConfig.BASE_URL}$path")
        if (queryParams.isNotEmpty()) {
            urlBuilder.append("?")
            urlBuilder.append(queryParams.entries.joinToString("&") { "${it.key}=${it.value}" })
        }
        val url = urlBuilder.toString()
        
        val requestBuilder = Request.Builder().url(url)
        
        when (method.uppercase()) {
            "GET" -> requestBuilder.get()
            "POST" -> requestBuilder.post(
                (body ?: "").toRequestBody(MEDIA_TYPE_JSON)
            )
            "PUT" -> requestBuilder.put(
                (body ?: "").toRequestBody(MEDIA_TYPE_JSON)
            )
            "DELETE" -> requestBuilder.delete(
                body?.toRequestBody(MEDIA_TYPE_JSON)
            )
        }
        
        val request = requestBuilder.build()
        val startTime = System.currentTimeMillis()
        
        logRequest(request, body)
        
        try {
            httpClient.newCall(request).execute().use { response ->
                val duration = System.currentTimeMillis() - startTime
                val responseBody = response.body?.string()
                
                logResponse(response)
                
                DebugApiResponse(
                    statusCode = response.code,
                    statusMessage = response.message,
                    headers = response.headers.toMap(),
                    body = responseBody,
                    durationMs = duration,
                    error = null
                )
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            DebugApiResponse(
                statusCode = -1,
                statusMessage = "Error",
                headers = emptyMap(),
                body = null,
                durationMs = duration,
                error = e.message
            )
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
            val errorBody = response.body?.string()
            throw ApiException(
                "$errorMessage: ${response.code} ${response.message}. $errorBody",
                response.code
            )
        }
        
        val json = response.body?.string()
            ?: throw ApiException("Empty response body", response.code)
        
        return gson.fromJson(json, T::class.java)
    }

    /**
     * Log request details in debug builds.
     */
    private fun logRequest(request: Request, body: String? = null) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Request: ${request.method} ${request.url}")
            request.headers.forEach { (name, value) ->
                // Don't log full auth token
                val logValue = if (name.equals("Authorization", ignoreCase = true)) {
                    value.take(20) + "..."
                } else {
                    value
                }
                Log.d(TAG, "  Header: $name: $logValue")
            }
            body?.let {
                Log.d(TAG, "  Body: ${it.take(500)}${if (it.length > 500) "..." else ""}")
            }
        }
    }

    /**
     * Log response details in debug builds.
     */
    private fun logResponse(response: Response) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Response: ${response.code} ${response.message}")
        }
    }

    private fun okhttp3.Headers.toMap(): Map<String, String> {
        return (0 until size).associate { name(it) to value(it) }
    }
    
    companion object {
        private const val TAG = "ApiClient"
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

/**
 * Response from a debug API request.
 */
data class DebugApiResponse(
    val statusCode: Int,
    val statusMessage: String,
    val headers: Map<String, String>,
    val body: String?,
    val durationMs: Long,
    val error: String?
)
