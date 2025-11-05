# LaCompraGo - API Integration

## Overview

This document describes the simplified API integration strategy for LaCompraGo, using token-based authentication and OkHttp for HTTP communication (no Retrofit).

## API Architecture

### Base Configuration

```kotlin
object ApiConfig {
    const val BASE_URL = "https://api.supermarket.example.com/"
    const val CONNECT_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L // seconds
    const val WRITE_TIMEOUT = 30L // seconds
}
```

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

### 1. Token Validation

Validate the token on first use.

```
GET /api/validate
```

**Headers**
```
Authorization: Bearer {token}
```

**Response (200 OK)**
```json
{
  "valid": true
}
```

**Response (401 Unauthorized)**
```json
{
  "error": "Invalid token"
}
```

### 2. List Orders

Get list of order IDs.

```
GET /api/orders
```

**Headers**
```
Authorization: Bearer {token}
```

**Response (200 OK)**
```json
{
  "orders": [
    {
      "id": "order_123",
      "orderDate": "2024-10-20T15:30:00Z"
    },
    {
      "id": "order_456",
      "orderDate": "2024-10-15T10:20:00Z"
    }
  ]
}
```

### 3. Get Order Details

Fetch details of a specific order.

```
GET /api/orders/{orderId}
```

**Headers**
```
Authorization: Bearer {token}
```

**Path Parameters**
- `orderId`: Order identifier

**Response (200 OK)**
```json
{
  "id": "order_123",
  "orderNumber": "ORD-2024-001",
  "orderDate": "2024-10-20T15:30:00Z",
  "totalAmount": 125.50,
  "items": [
    {
      "productId": "prod_123",
      "productName": "Milk 1L",
      "quantity": 2,
      "category": "Dairy"
    },
    {
      "productId": "prod_456",
      "productName": "Bread",
      "quantity": 1,
      "category": "Bakery"
    }
  ]
}
```

### 4. Create Shopping Cart

Submit a shopping cart to the API.

```
POST /api/cart
```

**Headers**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
  "items": [
    {
      "productId": "prod_123",
      "quantity": 2
    },
    {
      "productId": "prod_456",
      "quantity": 1
    }
  ]
}
```

**Response (201 Created)**
```json
{
  "cartId": "cart_789",
  "status": "CREATED",
  "createdAt": "2024-10-28T12:00:00Z"
}
```

## API Client Implementation

### Simple OkHttp API Client

```kotlin
class ApiClient(
    private val httpClient: OkHttpClient,
    private val gson: Gson
) {
    
    fun validateToken(token: String): Response {
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}api/validate")
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        
        return httpClient.newCall(request).execute()
    }
    
    suspend fun getOrderList(): List<OrderSummary> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}api/orders")
            .get()
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw ApiException("Failed to fetch orders: ${response.code}")
        }
        
        val json = response.body?.string() ?: throw ApiException("Empty response")
        val orderListResponse = gson.fromJson(json, OrderListResponse::class.java)
        
        orderListResponse.orders
    }
    
    suspend fun getOrderDetails(orderId: String): OrderResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}api/orders/$orderId")
            .get()
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw ApiException("Failed to fetch order $orderId: ${response.code}")
        }
        
        val json = response.body?.string() ?: throw ApiException("Empty response")
        gson.fromJson(json, OrderResponse::class.java)
    }
    
    suspend fun createCart(cartRequest: CartRequest): CartResponse = withContext(Dispatchers.IO) {
        val json = gson.toJson(cartRequest)
        val body = json.toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}api/cart")
            .post(body)
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw ApiException("Failed to create cart: ${response.code}")
        }
        
        val responseJson = response.body?.string() ?: throw ApiException("Empty response")
        gson.fromJson(responseJson, CartResponse::class.java)
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

### Sequential Order Download

```kotlin
suspend fun processOrders(
    apiClient: ApiClient,
    onProgress: (Int, Int, String) -> Unit,
    shouldContinue: () -> Boolean
): Int {
    var processedCount = 0
    
    // 1. Get list of orders
    val orders = apiClient.getOrderList()
    
    // 2. Load processed orders
    val processedOrders = loadProcessedOrders()
    
    // 3. Filter unprocessed orders
    val unprocessedOrders = orders.filter { 
        it.id !in processedOrders.processedOrderIds 
    }
    
    val totalOrders = unprocessedOrders.size
    
    // 4. Process each order sequentially
    for ((index, orderSummary) in unprocessedOrders.withIndex()) {
        // Check if cancelled
        if (!shouldContinue()) {
            break
        }
        
        try {
            // Update progress
            onProgress(index + 1, totalOrders, orderSummary.id)
            
            // Fetch order details
            val order = apiClient.getOrderDetails(orderSummary.id)
            
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

### Simple Rate Limiter

```kotlin
class RateLimiter(private val requestsPerMinute: Int = 60) {
    private val requestTimes = mutableListOf<Long>()
    
    suspend fun acquire() {
        val now = System.currentTimeMillis()
        val oneMinuteAgo = now - 60_000
        
        // Remove old entries
        requestTimes.removeAll { it < oneMinuteAgo }
        
        if (requestTimes.size >= requestsPerMinute) {
            // Wait until we can make another request
            val oldestRequest = requestTimes.first()
            val waitTime = 60_000 - (now - oldestRequest)
            delay(waitTime)
        }
        
        requestTimes.add(System.currentTimeMillis())
    }
}

// Usage
suspend fun getOrderWithRateLimit(orderId: String): OrderResponse {
    rateLimiter.acquire()
    return apiClient.getOrderDetails(orderId)
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

This simplified API integration strategy provides:
- Minimal dependencies (OkHttp + Gson only)
- Simple token-based authentication
- Sequential order processing with progress tracking
- Proper error handling
- Cancellable operations
- Suitable for LaCompraGo's requirements
