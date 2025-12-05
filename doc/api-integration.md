# LaCompraGo - API Integration

## Overview

This document describes the simplified API integration strategy for LaCompraGo, using token-based authentication and OkHttp for HTTP communication (no Retrofit).

> **IMPORTANT**: This document describes the integration with the **Mercadona API**.
> For detailed endpoint specifications, see [mercadona-api.md](./mercadona-api.md).

## API Architecture

### Base Configuration

```kotlin
object ApiConfig {
    // Real Mercadona API
    const val BASE_URL = "https://tienda.mercadona.es/api/"
    
    // Timeouts
    const val CONNECT_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L // seconds
    const val WRITE_TIMEOUT = 30L // seconds
    
    // Rate limiting
    const val MAX_REQUESTS_PER_MINUTE = 60
    const val REQUEST_DELAY_MS = 1500L // 1.5 seconds between requests
}
```

> ⚠️ **Note**: The Mercadona API is unofficial and reverse-engineered.
> Endpoints may change without notice. Use at your own risk.

### HTTP Client Setup

**OkHttp Configuration (Minimal)**
```kotlin
fun createHttpClient(tokenStorage: TokenStorage): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(TokenInterceptor(tokenStorage))
        .build()
}
```

**No Retrofit**
- Direct OkHttp usage
- Manual request building
- Simpler, fewer dependencies
- More control over requests

## API Endpoints

> **Full Documentation**: See [mercadona-api.md](./mercadona-api.md) for complete endpoint specifications.

### Endpoint Overview

| # | Endpoint | Method | Purpose |
|---|----------|--------|---------|
| 1 | `/api/auth/tokens/` | POST | Login (future) |
| 2 | `/api/customers/{customer_id}/` | GET | Get customer info / Validate token |
| 3 | `/api/customers/{customer_id}/cart/` | GET | Get current cart |
| 4 | `/api/customers/{customer_id}/cart/` | PUT | Update cart |
| 5 | `/api/customers/{customer_id}/orders/` | GET | List orders (paginated) |
| 6 | `/api/customers/{customer_id}/orders/{order_id}/` | GET | Get order details |
| 7 | `/api/customers/{customer_id}/recommendations/myregulars/{type}/` | GET | Get recommendations |
| 8 | `/api/postal-codes/actions/change-pc/` | PUT | Set warehouse |

### 1. Validate Token (Get Customer Info)

Used to validate the token and retrieve customer information including `customer_id`.

```
GET /api/customers/{customer_id}/
```

**Headers**
```
Authorization: Bearer {token}
```

**Response (200 OK)**
```json
{
    "id": 12345,
    "name": "John",
    "last_name": "Doe",
    "email": "john@example.com",
    "cart_id": "cart_abc123",
    "current_postal_code": "28001",
    "uuid": "uuid-string"
}
```

**Response (401 Unauthorized)**
```json
{
    "error": "Invalid token"
}
```

### 2. List Orders

Get list of orders with pagination.

```
GET /api/customers/{customer_id}/orders/?page={page}
```

**Headers**
```
Authorization: Bearer {token}
```

**Query Parameters**
- `page`: Page number (optional, default: 1)

**Response (200 OK)**
```json
{
    "next_page": 2,
    "results": [
        {
            "id": 8312430,
            "order_id": 8312430,
            "status": 2,
            "status_ui": "confirmed",
            "price": "65.94",
            "products_count": 28,
            "start_date": "2024-10-30T15:00:00Z",
            "end_date": "2024-10-30T16:00:00Z"
        }
    ]
}
```

### 3. Get Order Details

Fetch details of a specific order including products.

```
GET /api/customers/{customer_id}/orders/{order_id}/
```

**Headers**
```
Authorization: Bearer {token}
```

**Response (200 OK)**
Full order details including address, payment, slot, and summary.
See [mercadona-api.md](./mercadona-api.md) for complete response structure.

### 4. Get Recommendations

Get personalized product recommendations.

```
GET /api/customers/{customer_id}/recommendations/myregulars/{type}/
```

**Parameters**
- `type`: `precision` (most bought) or `recall` (also buy)

**Response (200 OK)**
```json
{
    "next_page": null,
    "results": [
        {
            "product": {
                "id": "84780",
                "display_name": "Product Name",
                "categories": [...],
                "price_instructions": {...}
            },
            "recommended_quantity": 1
        }
    ]
}
```

### 5. Update Cart

Add products to the shopping cart.

```
PUT /api/customers/{customer_id}/cart/
```

**Headers**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
    "id": "{cart_id}",
    "version": 1,
    "lines": [
        {
            "quantity": 2,
            "product_id": "51621",
            "sources": []
        }
    ]
}
```

**Response (200 OK)**
Returns the updated cart with products.

## API Client Implementation

### Robust API Client with Rate Limiting

```kotlin
class ApiClient(
    private val httpClient: OkHttpClient,
    private val gson: Gson,
    private val rateLimiter: RateLimiter = RateLimiter()
) {
    
    /**
     * Get customer information and validate token.
     * This is the primary way to validate if a token is valid.
     *
     * @param customerId The customer ID
     * @return CustomerInfo if successful
     * @throws ApiException if the request fails
     */
    suspend fun getCustomerInfo(customerId: String): CustomerInfo = withContext(Dispatchers.IO) {
        require(customerId.isNotBlank()) { "Customer ID cannot be blank" }
        
        rateLimiter.acquire()
        
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}customers/$customerId/")
            .get()
            .build()
        
        httpClient.newCall(request).execute().use { response ->
            handleResponse(response, "Failed to fetch customer info")
        }
    }
    
    /**
     * Get paginated list of orders.
     *
     * @param customerId The customer ID
     * @param page Page number (default: 1)
     * @return OrderListResponse with results and next_page
     * @throws ApiException if the request fails
     */
    suspend fun getOrderList(customerId: String, page: Int = 1): OrderListResponse = withContext(Dispatchers.IO) {
        require(customerId.isNotBlank()) { "Customer ID cannot be blank" }
        require(page >= 1) { "Page must be >= 1" }
        
        rateLimiter.acquire()
        
        val url = "${ApiConfig.BASE_URL}customers/$customerId/orders/?page=$page"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        
        httpClient.newCall(request).execute().use { response ->
            handleResponse(response, "Failed to fetch orders")
        }
    }
    
    /**
     * Get details of a specific order.
     *
     * @param customerId The customer ID
     * @param orderId The order ID
     * @return Full order details
     * @throws ApiException if the request fails
     */
    suspend fun getOrderDetails(customerId: String, orderId: Int): OrderResponse = withContext(Dispatchers.IO) {
        require(customerId.isNotBlank()) { "Customer ID cannot be blank" }
        require(orderId > 0) { "Order ID must be positive" }
        
        rateLimiter.acquire()
        
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}customers/$customerId/orders/$orderId/")
            .get()
            .build()
        
        httpClient.newCall(request).execute().use { response ->
            handleResponse(response, "Failed to fetch order $orderId")
        }
    }
    
    /**
     * Get customer's shopping cart.
     *
     * @param customerId The customer ID
     * @return Cart with lines and summary
     * @throws ApiException if the request fails
     */
    suspend fun getCart(customerId: String): CartResponse = withContext(Dispatchers.IO) {
        require(customerId.isNotBlank()) { "Customer ID cannot be blank" }
        
        rateLimiter.acquire()
        
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}customers/$customerId/cart/")
            .get()
            .build()
        
        httpClient.newCall(request).execute().use { response ->
            handleResponse(response, "Failed to fetch cart")
        }
    }
    
    /**
     * Update shopping cart with products.
     *
     * @param customerId The customer ID
     * @param cartRequest Cart update request
     * @return Updated cart
     * @throws ApiException if the request fails
     */
    suspend fun updateCart(customerId: String, cartRequest: CartUpdateRequest): CartResponse = withContext(Dispatchers.IO) {
        require(customerId.isNotBlank()) { "Customer ID cannot be blank" }
        require(cartRequest.id.isNotBlank()) { "Cart ID cannot be blank" }
        
        rateLimiter.acquire()
        
        val json = gson.toJson(cartRequest)
        val body = json.toRequestBody(MEDIA_TYPE_JSON)
        
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}customers/$customerId/cart/")
            .put(body)
            .build()
        
        httpClient.newCall(request).execute().use { response ->
            handleResponse(response, "Failed to update cart")
        }
    }
    
    /**
     * Get personalized product recommendations.
     *
     * @param customerId The customer ID
     * @param type Recommendation type: "precision" or "recall"
     * @return List of recommended products
     * @throws ApiException if the request fails
     */
    suspend fun getRecommendations(customerId: String, type: String = "precision"): RecommendationsResponse = withContext(Dispatchers.IO) {
        require(customerId.isNotBlank()) { "Customer ID cannot be blank" }
        require(type in listOf("precision", "recall")) { "Type must be 'precision' or 'recall'" }
        
        rateLimiter.acquire()
        
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}customers/$customerId/recommendations/myregulars/$type/")
            .get()
            .build()
        
        httpClient.newCall(request).execute().use { response ->
            handleResponse(response, "Failed to fetch recommendations")
        }
    }
    
    /**
     * Handle the response and parse JSON to the expected type.
     */
    private inline fun <reified T> handleResponse(response: Response, errorMessage: String): T {
        when {
            response.isSuccessful -> {
                val json = response.body?.string()
                    ?: throw ApiException("Empty response body", response.code)
                return gson.fromJson(json, T::class.java)
            }
            response.code == 401 -> {
                throw ApiException("Unauthorized - token may be invalid", 401)
            }
            response.code == 429 -> {
                throw ApiException("Rate limited - too many requests", 429)
            }
            else -> {
                throw ApiException("$errorMessage: ${response.code}", response.code)
            }
        }
    }
    
    companion object {
        private val MEDIA_TYPE_JSON = "application/json".toMediaType()
    }
}
```

## Interceptors

### Token Interceptor

Add authentication token to all requests.

```kotlin
class TokenInterceptor(
    private val tokenStorage: TokenStorage
) : Interceptor {
    
    override fun intercept(chain: Chain): Response {
        val original = chain.request()
        val token = tokenStorage.getToken()
        
        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        
        val response = chain.proceed(request)
        
        // Handle 401 Unauthorized
        if (response.code == 401) {
            // Token is invalid, clear it
            tokenStorage.clearToken()
        }
        
        return response
    }
}
```

## Error Handling

### API Exception

```kotlin
class ApiException(message: String) : Exception(message)

sealed class ApiError {
    object NetworkError : ApiError()
    object Unauthorized : ApiError()
    data class ServerError(val code: Int) : ApiError()
    data class Unknown(val message: String) : ApiError()
}

fun handleApiError(exception: Exception): ApiError {
    return when (exception) {
        is IOException -> ApiError.NetworkError
        is ApiException -> {
            if (exception.message?.contains("401") == true) {
                ApiError.Unauthorized
            } else {
                ApiError.Unknown(exception.message ?: "Unknown error")
            }
        }
        else -> ApiError.Unknown(exception.message ?: "Unknown error")
    }
}
```

### Retry Strategy

Simple retry for network errors.

```kotlin
suspend fun <T> retryApiCall(
    maxRetries: Int = 3,
    delayMs: Long = 1000,
    block: suspend () -> T
): T {
    var lastException: Exception? = null
    
    repeat(maxRetries) { attempt ->
        try {
            return block()
        } catch (e: IOException) {
            lastException = e
            if (attempt < maxRetries - 1) {
                delay(delayMs * (attempt + 1)) // Exponential backoff
            }
        }
    }
    
    throw lastException ?: Exception("Unknown error")
}
```

## HTTP Status Codes

| Code | Meaning | Action |
|------|---------|--------|
| 200 | Success | Process response |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Validate request parameters |
| 401 | Unauthorized | Invalid token - prompt for new token |
| 404 | Not Found | Resource doesn't exist |
| 500 | Server Error | Retry with backoff |
| 503 | Service Unavailable | Retry later |

## Order Processing Flow

### Sequential Order Download with Pagination

The Mercadona API returns orders in a paginated format. We must handle pagination
and process orders sequentially with appropriate delays.

```kotlin
suspend fun processOrders(
    customerId: String,
    apiClient: ApiClient,
    onProgress: (Int, Int, Int) -> Unit,
    shouldContinue: () -> Boolean
): Int {
    var processedCount = 0
    var totalCount = 0
    val allOrders = mutableListOf<OrderSummary>()
    
    // 1. Fetch all pages of orders
    var page = 1
    do {
        val orderList = apiClient.getOrderList(customerId, page)
        allOrders.addAll(orderList.results)
        page = orderList.next_page ?: break
    } while (shouldContinue())
    
    // 2. Load processed orders
    val processedOrders = loadProcessedOrders()
    
    // 3. Filter unprocessed orders
    val unprocessedOrders = allOrders.filter { 
        it.id !in processedOrders.processedOrderIds 
    }
    
    totalCount = unprocessedOrders.size
    
    // 4. Process each order sequentially with delays
    for ((index, orderSummary) in unprocessedOrders.withIndex()) {
        // Check if cancelled
        if (!shouldContinue()) {
            break
        }
        
        try {
            // Update progress
            onProgress(index + 1, totalCount, orderSummary.id)
            
            // Fetch order details (rate limiter handles delays)
            val order = apiClient.getOrderDetails(customerId, orderSummary.id)
            
            // Update products
            updateProductsFromOrder(order)
            
            // Mark order as processed
            markOrderAsProcessed(orderSummary.id)
            
            processedCount++
            
        } catch (e: Exception) {
            // Log error but continue with next order
            Log.e("OrderProcessing", "Error processing order ${orderSummary.id}", e)
        }
    }
    
    return processedCount
}
```

### Product Extraction from Mercadona Order

Mercadona orders contain products with detailed information. We extract what we need.

```kotlin
fun extractProductsFromOrder(order: MercadonaOrderResponse): List<ProductUpdate> {
    val orderDate = parseIsoDate(order.start_date)
    
    // Orders may have a 'lines' or 'products' field depending on the endpoint
    // We need to handle both cases
    
    return order.lines?.map { line ->
        ProductUpdate(
            id = line.product.id,
            name = line.product.display_name,
            quantity = line.quantity,
            category = line.product.categories.firstOrNull()?.name,
            orderDate = orderDate
        )
    } ?: emptyList()
}

data class ProductUpdate(
    val id: String,
    val name: String,
    val quantity: Double,
    val category: String?,
    val orderDate: Long
)

fun updateLocalProduct(existing: Product?, update: ProductUpdate): Product {
    return if (existing != null) {
        existing.copy(
            frequency = existing.frequency + 1,
            lastPurchase = maxOf(existing.lastPurchase, update.orderDate),
            totalQuantity = existing.totalQuantity + update.quantity
        )
    } else {
        Product(
            id = update.id,
            name = update.name,
            frequency = 1,
            lastPurchase = update.orderDate,
            category = update.category,
            totalQuantity = update.quantity
        )
    }
}
```

## Network Connectivity

### Check Network Before Requests

```kotlin
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager
    
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    
    return capabilities?.hasCapability(
        NetworkCapabilities.NET_CAPABILITY_INTERNET
    ) == true
}
```

## Security Best Practices

### 1. HTTPS Only
- All API calls use HTTPS
- No plain HTTP allowed

### 2. Token Security
- Store token encrypted
- Never log token
- Clear token on 401 error
- Use token in Authorization header

### 3. Request Validation
- Validate all input parameters
- Sanitize user input
- Use HTTPS for sensitive data

## Testing

### API Client Tests

```kotlin
@Test
fun `test get order list`() = runTest {
    val mockResponse = """
        {
          "orders": [
            {"id": "order_123", "orderDate": "2024-10-20T15:30:00Z"}
          ]
        }
    """.trimIndent()
    
    mockWebServer.enqueue(MockResponse().setBody(mockResponse))
    
    val orders = apiClient.getOrderList()
    
    assertEquals(1, orders.size)
    assertEquals("order_123", orders[0].id)
}

@Test
fun `test unauthorized error`() = runTest {
    mockWebServer.enqueue(MockResponse().setResponseCode(401))
    
    assertThrows<ApiException> {
        apiClient.getOrderList()
    }
}
```

## Progress Tracking

### Progress Callback

```kotlin
interface OrderProcessingCallback {
    fun onProgress(current: Int, total: Int, currentOrderId: String)
    fun onComplete(processedCount: Int)
    fun onError(error: String, processedCount: Int)
    fun onCancelled(processedCount: Int)
}
```

### Usage

```kotlin
viewModelScope.launch {
    try {
        val processedCount = processOrders(
            apiClient = apiClient,
            onProgress = { current, total, orderId ->
                _processingState.value = OrderProcessingState.Processing(
                    currentOrder = current,
                    totalOrders = total,
                    currentOrderId = orderId
                )
            },
            shouldContinue = { !isCancelled }
        )
        
        _processingState.value = if (isCancelled) {
            OrderProcessingState.Cancelled(processedCount)
        } else {
            OrderProcessingState.Completed(processedCount)
        }
        
    } catch (e: Exception) {
        _processingState.value = OrderProcessingState.Error(
            message = e.message ?: "Unknown error",
            processedCount = 0
        )
    }
}
```

## API Mocking for Development

### MockWebServer for Testing

```kotlin
class MockApiServer {
    private val mockWebServer = MockWebServer()
    
    fun start() {
        mockWebServer.start()
    }
    
    fun shutdown() {
        mockWebServer.shutdown()
    }
    
    fun enqueueOrderList(orders: List<OrderSummary>) {
        val response = OrderListResponse(orders)
        val json = gson.toJson(response)
        mockWebServer.enqueue(MockResponse().setBody(json))
    }
    
    fun enqueueOrderDetails(order: OrderResponse) {
        val json = gson.toJson(order)
        mockWebServer.enqueue(MockResponse().setBody(json))
    }
    
    fun getUrl(): String {
        return mockWebServer.url("/").toString()
    }
}
```

## Rate Limiting

### Robust Rate Limiter

The Mercadona API is rate limited. We implement strict rate limiting to avoid 429 errors.

```kotlin
class RateLimiter(
    private val maxRequestsPerMinute: Int = ApiConfig.MAX_REQUESTS_PER_MINUTE,
    private val minDelayMs: Long = ApiConfig.REQUEST_DELAY_MS
) {
    private val requestTimes = mutableListOf<Long>()
    private var lastRequestTime = 0L
    
    /**
     * Acquire permission to make a request.
     * Blocks if rate limit would be exceeded.
     */
    suspend fun acquire() {
        val now = System.currentTimeMillis()
        val oneMinuteAgo = now - 60_000
        
        // Ensure minimum delay between requests
        val timeSinceLastRequest = now - lastRequestTime
        if (timeSinceLastRequest < minDelayMs && lastRequestTime > 0) {
            delay(minDelayMs - timeSinceLastRequest)
        }
        
        // Remove old entries
        synchronized(requestTimes) {
            requestTimes.removeAll { it < oneMinuteAgo }
            
            // Check if we're at the limit
            if (requestTimes.size >= maxRequestsPerMinute) {
                val oldestRequest = requestTimes.first()
                val waitTime = 60_000 - (now - oldestRequest) + 1000 // Add 1 second buffer
                if (waitTime > 0) {
                    delay(waitTime)
                }
                requestTimes.removeAll { it < System.currentTimeMillis() - 60_000 }
            }
            
            requestTimes.add(System.currentTimeMillis())
            lastRequestTime = System.currentTimeMillis()
        }
    }
    
    /**
     * Get current request count in the last minute.
     */
    fun getCurrentCount(): Int {
        val oneMinuteAgo = System.currentTimeMillis() - 60_000
        synchronized(requestTimes) {
            requestTimes.removeAll { it < oneMinuteAgo }
            return requestTimes.size
        }
    }
}
```

### Rate Limit Configuration

```kotlin
object ApiConfig {
    // Maximum requests per minute (conservative to avoid blocks)
    const val MAX_REQUESTS_PER_MINUTE = 60
    
    // Minimum delay between requests (ms)
    const val REQUEST_DELAY_MS = 1500L
    
    // Delay between order fetches (ms) - for sequential processing
    const val ORDER_FETCH_DELAY_MS = 2000L
}
```

### Usage in Order Processing

```kotlin
suspend fun processOrders(
    customerId: String,
    onProgress: (Int, Int, Int) -> Unit,
    shouldContinue: () -> Boolean
): Int {
    var processedCount = 0
    var page = 1
    var totalCount = 0 // Total unprocessed orders (updated as we fetch pages)
    
    // Fetch all pages of orders
    do {
        val orderList = apiClient.getOrderList(customerId, page)
        val processedOrders = loadProcessedOrders()
        
        val unprocessedOrders = orderList.results.filter { 
            it.id !in processedOrders.processedOrderIds 
        }
        
        totalCount += unprocessedOrders.size
        
        for ((index, orderSummary) in unprocessedOrders.withIndex()) {
            if (!shouldContinue()) break
            
            try {
                onProgress(processedCount + index + 1, totalCount, orderSummary.id)
                
                // Delay between order fetches
                delay(ApiConfig.ORDER_FETCH_DELAY_MS)
                
                val order = apiClient.getOrderDetails(customerId, orderSummary.id)
                updateProductsFromOrder(order)
                markOrderAsProcessed(orderSummary.id)
                
                processedCount++
            } catch (e: Exception) {
                Log.e("OrderProcessing", "Error processing order ${orderSummary.id}", e)
            }
        }
        
        page = orderList.next_page ?: break
    } while (shouldContinue())
    
    return processedCount
}
```

## Monitoring

### Log API Calls (Debug Only)

```kotlin
class LoggingInterceptor : Interceptor {
    override fun intercept(chain: Chain): Response {
        val request = chain.request()
        
        if (BuildConfig.DEBUG) {
            Log.d("API", "Request: ${request.method} ${request.url}")
        }
        
        val response = chain.proceed(request)
        
        if (BuildConfig.DEBUG) {
            Log.d("API", "Response: ${response.code} ${request.url}")
        }
        
        return response
    }
}
```

## Conclusion

This API integration strategy provides:
- Integration with the real Mercadona API
- Robust rate limiting to avoid blocks
- Strict input validation
- Sequential order processing with progress tracking
- Proper error handling
- Support for pagination
- Cancellable operations

### Next Steps

1. Validate endpoints in Debug Mode (see [debug-mode.md](./debug-mode.md))
2. Update data models to match Mercadona response structure
3. Implement customer_id storage alongside token
4. Test with real Mercadona account

### References

- [Mercadona API Reference](./mercadona-api.md)
- [Debug Mode Specification](./debug-mode.md)
- [mercadona-cli GitHub](https://github.com/alfonmga/mercadona-cli)
