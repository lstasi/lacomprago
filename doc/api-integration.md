# LaComprago - API Integration

## Overview

This document describes the API integration strategy for LaComprago, including authentication, endpoints, request/response formats, error handling, and best practices.

## API Architecture

### Base Configuration

```kotlin
object ApiConfig {
    const val BASE_URL = "https://api.supermarket.example.com/v1/"
    const val CONNECT_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L // seconds
    const val WRITE_TIMEOUT = 30L // seconds
}
```

### HTTP Client Setup

**OkHttp Configuration**
```kotlin
OkHttpClient.Builder()
    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
    .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
    .addInterceptor(AuthInterceptor())
    .addInterceptor(LoggingInterceptor())
    .addInterceptor(NetworkConnectionInterceptor())
    .build()
```

**Retrofit Configuration**
```kotlin
Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```

## API Endpoints

### 1. Authentication Endpoints

#### OAuth Authorization
```
GET /oauth/authorize
```

**Query Parameters**
- `client_id`: Application client ID
- `redirect_uri`: Callback URI
- `response_type`: "code"
- `scope`: Requested permissions (e.g., "orders:read cart:write")
- `state`: CSRF protection token

**Response**
- Redirects to authorization page
- Returns authorization code on success

#### Token Exchange
```
POST /oauth/token
```

**Request Body**
```json
{
    "grant_type": "authorization_code",
    "code": "AUTH_CODE",
    "client_id": "CLIENT_ID",
    "client_secret": "CLIENT_SECRET",
    "redirect_uri": "REDIRECT_URI"
}
```

**Response (200 OK)**
```json
{
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "refresh_token_value",
    "token_type": "Bearer",
    "expires_in": 3600,
    "scope": "orders:read cart:write"
}
```

#### Token Refresh
```
POST /oauth/token
```

**Request Body**
```json
{
    "grant_type": "refresh_token",
    "refresh_token": "REFRESH_TOKEN",
    "client_id": "CLIENT_ID",
    "client_secret": "CLIENT_SECRET"
}
```

**Response (200 OK)**
```json
{
    "access_token": "new_access_token",
    "refresh_token": "new_refresh_token",
    "token_type": "Bearer",
    "expires_in": 3600
}
```

### 2. User Endpoints

#### Get User Profile
```
GET /api/user/profile
```

**Headers**
```
Authorization: Bearer {access_token}
```

**Response (200 OK)**
```json
{
    "id": "user_123",
    "email": "user@example.com",
    "name": "John Doe",
    "created_at": "2024-01-15T10:30:00Z"
}
```

### 3. Orders Endpoints

#### Get Order History
```
GET /api/orders
```

**Headers**
```
Authorization: Bearer {access_token}
```

**Query Parameters**
- `page`: Page number (default: 1)
- `page_size`: Items per page (default: 20, max: 100)
- `start_date`: ISO 8601 date (optional)
- `end_date`: ISO 8601 date (optional)
- `status`: Order status filter (optional)

**Response (200 OK)**
```json
{
    "orders": [
        {
            "id": "order_123",
            "order_number": "ORD-2024-001",
            "order_date": "2024-10-20T15:30:00Z",
            "total_amount": 125.50,
            "currency": "EUR",
            "status": "COMPLETED",
            "store_id": "store_001",
            "store_name": "SuperMarket Downtown",
            "items": [
                {
                    "id": "item_456",
                    "product_id": "prod_789",
                    "product_name": "Milk 1L",
                    "quantity": 2,
                    "unit_price": 1.99,
                    "total_price": 3.98,
                    "category": "Dairy",
                    "brand": "Local Farm",
                    "unit": "liter"
                }
            ]
        }
    ],
    "total": 150,
    "page": 1,
    "page_size": 20,
    "has_more": true
}
```

#### Get Order Details
```
GET /api/orders/{order_id}
```

**Headers**
```
Authorization: Bearer {access_token}
```

**Path Parameters**
- `order_id`: Order identifier

**Response (200 OK)**
```json
{
    "id": "order_123",
    "order_number": "ORD-2024-001",
    "order_date": "2024-10-20T15:30:00Z",
    "total_amount": 125.50,
    "currency": "EUR",
    "status": "COMPLETED",
    "store_id": "store_001",
    "store_name": "SuperMarket Downtown",
    "items": [...]
}
```

### 4. Shopping Cart Endpoints

#### Create Shopping Cart
```
POST /api/cart
```

**Headers**
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Request Body**
```json
{
    "items": [
        {
            "product_id": "prod_789",
            "quantity": 2
        },
        {
            "product_id": "prod_123",
            "quantity": 1
        }
    ]
}
```

**Response (201 Created)**
```json
{
    "cart_id": "cart_456",
    "status": "SUBMITTED",
    "created_at": "2024-10-28T12:00:00Z",
    "items": [
        {
            "product_id": "prod_789",
            "product_name": "Milk 1L",
            "quantity": 2,
            "price": 1.99,
            "total": 3.98
        }
    ],
    "total_amount": 25.50
}
```

#### Get Cart Status
```
GET /api/cart/{cart_id}
```

**Headers**
```
Authorization: Bearer {access_token}
```

**Path Parameters**
- `cart_id`: Cart identifier

**Response (200 OK)**
```json
{
    "cart_id": "cart_456",
    "status": "PROCESSING",
    "created_at": "2024-10-28T12:00:00Z",
    "updated_at": "2024-10-28T12:05:00Z",
    "items": [...],
    "total_amount": 25.50
}
```

### 5. Products Endpoints (If Available)

#### Search Products
```
GET /api/products/search
```

**Headers**
```
Authorization: Bearer {access_token}
```

**Query Parameters**
- `q`: Search query
- `category`: Filter by category
- `page`: Page number
- `page_size`: Items per page

**Response (200 OK)**
```json
{
    "products": [
        {
            "id": "prod_789",
            "name": "Milk 1L",
            "category": "Dairy",
            "brand": "Local Farm",
            "unit": "liter",
            "current_price": 1.99,
            "in_stock": true
        }
    ],
    "total": 50,
    "page": 1,
    "page_size": 20
}
```

## Interceptors

### 1. AuthInterceptor

**Purpose**: Add authentication token to requests

```kotlin
class AuthInterceptor(
    private val tokenProvider: TokenProvider
) : Interceptor {
    override fun intercept(chain: Chain): Response {
        val original = chain.request()
        
        val token = tokenProvider.getAccessToken()
        
        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        
        val response = chain.proceed(request)
        
        // Handle token expiration (401)
        if (response.code == 401) {
            // Attempt token refresh
            val newToken = tokenProvider.refreshToken()
            if (newToken != null) {
                // Retry with new token
                return chain.proceed(
                    original.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                )
            }
        }
        
        return response
    }
}
```

### 2. LoggingInterceptor

**Purpose**: Log HTTP requests and responses (debug builds only)

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

### 3. NetworkConnectionInterceptor

**Purpose**: Check network connectivity before making requests

```kotlin
class NetworkConnectionInterceptor(
    private val context: Context
) : Interceptor {
    override fun intercept(chain: Chain): Response {
        if (!isNetworkAvailable()) {
            throw NoNetworkException()
        }
        
        return chain.proceed(chain.request())
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        return capabilities?.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) == true
    }
}
```

## Error Handling

### Error Response Format

**Standard Error Response**
```json
{
    "error": {
        "code": "INVALID_TOKEN",
        "message": "The provided access token is invalid or expired",
        "details": {
            "field": "access_token",
            "reason": "expired"
        }
    }
}
```

### HTTP Status Codes

| Code | Meaning | Action |
|------|---------|--------|
| 200 | Success | Process response |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Validate request parameters |
| 401 | Unauthorized | Refresh token or re-authenticate |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 429 | Too Many Requests | Implement rate limiting backoff |
| 500 | Server Error | Retry with exponential backoff |
| 503 | Service Unavailable | Retry later |

### Error Models

```kotlin
data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any>?
)

sealed class ApiException(message: String) : Exception(message) {
    class NetworkException(message: String) : ApiException(message)
    class AuthException(message: String) : ApiException(message)
    class ServerException(message: String) : ApiException(message)
    class ValidationException(message: String) : ApiException(message)
    class NotFoundException(message: String) : ApiException(message)
}
```

### Retry Strategy

**Exponential Backoff**
```kotlin
class RetryPolicy {
    companion object {
        const val MAX_RETRIES = 3
        const val INITIAL_BACKOFF_MS = 1000L
        const val MAX_BACKOFF_MS = 10000L
        const val BACKOFF_MULTIPLIER = 2.0
    }
    
    fun shouldRetry(attempt: Int, exception: Exception): Boolean {
        return attempt < MAX_RETRIES && isRetryable(exception)
    }
    
    private fun isRetryable(exception: Exception): Boolean {
        return exception is IOException ||
               (exception is HttpException && 
                exception.code() in listOf(429, 500, 503))
    }
    
    fun getBackoffDelay(attempt: Int): Long {
        val delay = (INITIAL_BACKOFF_MS * 
                    Math.pow(BACKOFF_MULTIPLIER, attempt.toDouble())).toLong()
        return minOf(delay, MAX_BACKOFF_MS)
    }
}
```

## Rate Limiting

### Client-Side Rate Limiting

**Strategy**
- Implement request throttling
- Queue requests when rate limit reached
- Display user-friendly messages

**Implementation**
```kotlin
class RateLimiter {
    private val requestTimes = mutableListOf<Long>()
    private val maxRequestsPerMinute = 60
    
    fun canMakeRequest(): Boolean {
        val now = System.currentTimeMillis()
        val oneMinuteAgo = now - 60_000
        
        // Remove old entries
        requestTimes.removeAll { it < oneMinuteAgo }
        
        return requestTimes.size < maxRequestsPerMinute
    }
    
    fun recordRequest() {
        requestTimes.add(System.currentTimeMillis())
    }
}
```

## Caching Strategy

### Cache Headers

**Respect Server Cache Directives**
- `Cache-Control`
- `ETag`
- `Last-Modified`

**OkHttp Cache**
```kotlin
val cacheSize = 10 * 1024 * 1024 // 10 MB
val cache = Cache(context.cacheDir, cacheSize)

OkHttpClient.Builder()
    .cache(cache)
    .build()
```

### Data Freshness

**Order History**
- Cache for 5 minutes
- Refresh on pull-to-refresh

**Product Data**
- Cache for 1 hour
- Refresh when creating cart

**User Profile**
- Cache for session duration
- Refresh on app launch

## Security Best Practices

### 1. HTTPS Only
- All API calls use HTTPS
- No plain HTTP allowed

### 2. Certificate Pinning (Optional)
```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.supermarket.example.com", "sha256/AAAAAAAAAA...")
    .build()

OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

### 3. Token Security
- Store tokens encrypted
- Never log tokens
- Clear tokens on logout
- Rotate refresh tokens

### 4. Request Validation
- Validate all input parameters
- Sanitize user input
- Use HTTPS for sensitive data

## Testing Strategy

### 1. Mock API Responses
```kotlin
@Test
fun testOrderHistorySuccess() {
    val mockResponse = MockResponse()
        .setResponseCode(200)
        .setBody(ordersJson)
    
    mockWebServer.enqueue(mockResponse)
    
    // Test API call
}
```

### 2. Error Scenarios
- Network failures
- Token expiration
- Invalid responses
- Rate limiting

### 3. Integration Tests
- End-to-end API flows
- Authentication process
- Data synchronization

## Monitoring and Analytics

### API Metrics
- Request success/failure rates
- Response times
- Error frequency by endpoint
- Network error rates

### Logging
- Request/response in debug mode
- Error logs in production
- Performance metrics
- User actions

## API Versioning

### Version Strategy
- Use versioned endpoints (`/v1/`, `/v2/`)
- Maintain backward compatibility
- Gradual migration path

### Version Handling
```kotlin
object ApiVersion {
    const val CURRENT = "v1"
    const val SUPPORTED = listOf("v1")
}
```

## Conclusion

This API integration strategy ensures secure, reliable, and efficient communication with the supermarket API, handling authentication, data synchronization, error scenarios, and providing a solid foundation for the LaComprago application.
