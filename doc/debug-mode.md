# LaCompraGo - Debug Mode Specification

## Overview

Debug Mode is a special UI screen available only in debug builds that allows developers to test and validate individual API endpoints before integrating them into the main application flow.

## Purpose

1. **API Validation**: Test each Mercadona API endpoint individually
2. **Request Inspection**: View the exact request being sent
3. **Response Inspection**: View the raw API response
4. **Token Testing**: Validate token functionality before production use
5. **Development Aid**: Speed up development by testing endpoints in isolation

## Requirements

### Availability
- Debug Mode screen is **ONLY visible in debug builds**
- Hidden from release builds via `BuildConfig.DEBUG` flag
- Accessible from the main screen via a hidden gesture or menu item

### UI Components

#### 1. Token Management Section
```
┌─────────────────────────────────────────────────────┐
│  Token Management                                   │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Current Token Status: [Valid/Invalid/None]         │
│                                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │ [Paste token here...]                          │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  [Save Token]    [Clear Token]    [Validate Token]  │
│                                                     │
│  Customer ID: ____________________                  │
│  [Save Customer ID]                                 │
│                                                     │
└─────────────────────────────────────────────────────┘
```

#### 2. Endpoint Selection Section
```
┌─────────────────────────────────────────────────────┐
│  Available Endpoints                                │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ○ GET  /customers/{id}/          [Customer Info]   │
│  ○ GET  /customers/{id}/cart/     [Get Cart]        │
│  ○ GET  /customers/{id}/orders/   [List Orders]     │
│  ○ GET  /customers/{id}/orders/{orderId}/ [Order]   │
│  ○ GET  /customers/{id}/recommendations/myregulars/ │
│  ○ PUT  /postal-codes/actions/change-pc/            │
│                                                     │
└─────────────────────────────────────────────────────┘
```

#### 3. Request Configuration Section
```
┌─────────────────────────────────────────────────────┐
│  Request Configuration                              │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Endpoint: GET /customers/{customer_id}/orders/     │
│                                                     │
│  Path Parameters:                                   │
│  ┌─────────────────┐ ┌─────────────────┐            │
│  │ customer_id     │ │ 12345           │            │
│  └─────────────────┘ └─────────────────┘            │
│                                                     │
│  Query Parameters:                                  │
│  ┌─────────────────┐ ┌─────────────────┐            │
│  │ page            │ │ 1               │            │
│  └─────────────────┘ └─────────────────┘            │
│                                                     │
│  Request Body (for POST/PUT):                       │
│  ┌───────────────────────────────────────────────┐  │
│  │ {                                              │  │
│  │   "id": "cart_123",                            │  │
│  │   "version": 1                                 │  │
│  │ }                                              │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
└─────────────────────────────────────────────────────┘
```

#### 4. Request Preview Section
```
┌─────────────────────────────────────────────────────┐
│  Request Preview                                    │
├─────────────────────────────────────────────────────┤
│                                                     │
│  URL: https://tienda.mercadona.es/api/customers/... │
│                                                     │
│  Headers:                                           │
│  Authorization: Bearer eyJhbGc...                   │
│  Content-Type: application/json                     │
│  User-Agent: LaCompraGo/1.0 (Android)               │
│                                                     │
│  Body: (none)                                       │
│                                                     │
│  [Send Request]                                     │
│                                                     │
└─────────────────────────────────────────────────────┘
```

#### 5. Response Section
```
┌─────────────────────────────────────────────────────┐
│  Response                                           │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Status: 200 OK                                     │
│  Time: 342ms                                        │
│                                                     │
│  Headers:                                           │
│  Content-Type: application/json                     │
│  x-customer-wh: mad1                                │
│                                                     │
│  Body:                                              │
│  ┌───────────────────────────────────────────────┐  │
│  │ {                                              │  │
│  │   "id": 12345,                                 │  │
│  │   "name": "John",                              │  │
│  │   "email": "john@example.com",                 │  │
│  │   ...                                          │  │
│  │ }                                              │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  [Copy Response]    [Clear]                         │
│                                                     │
└─────────────────────────────────────────────────────┘
```

## Endpoint Test Cases

### 1. Customer Info Endpoint
- **Purpose**: Validate token and get customer information
- **Endpoint**: `GET /customers/{customer_id}/`
- **Required Fields**: customer_id
- **Expected Response**: Customer object with name, email, cart_id
- **Validates**: Token is valid and customer_id is correct

### 2. List Orders Endpoint
- **Purpose**: Get paginated list of orders
- **Endpoint**: `GET /customers/{customer_id}/orders/?page={page}`
- **Required Fields**: customer_id
- **Optional Fields**: page (default: 1)
- **Expected Response**: Order list with next_page and results
- **Validates**: Order history access

### 3. Get Order Details Endpoint
- **Purpose**: Get details of a specific order
- **Endpoint**: `GET /customers/{customer_id}/orders/{order_id}/`
- **Required Fields**: customer_id, order_id
- **Expected Response**: Full order details with products
- **Validates**: Individual order access

### 4. Get Cart Endpoint
- **Purpose**: Get current shopping cart
- **Endpoint**: `GET /customers/{customer_id}/cart/`
- **Required Fields**: customer_id
- **Expected Response**: Cart with lines, products, summary
- **Validates**: Cart access

### 5. Get Recommendations Endpoint
- **Purpose**: Get personalized product recommendations
- **Endpoint**: `GET /customers/{customer_id}/recommendations/myregulars/{type}/`
- **Required Fields**: customer_id, type (precision|recall)
- **Expected Response**: List of recommended products
- **Validates**: Recommendations API access

### 6. Set Warehouse Endpoint
- **Purpose**: Change warehouse by postal code
- **Endpoint**: `PUT /postal-codes/actions/change-pc/`
- **Required Body**: `{"new_postal_code": "28001"}`
- **Expected Response**: `{"warehouse_changed": true}`
- **Validates**: Warehouse selection

## Implementation Details

### File Structure
```
app/src/main/java/com/lacomprago/
├── ui/
│   └── debug/
│       ├── DebugActivity.kt           # Main debug screen
│       ├── EndpointAdapter.kt         # List of endpoints
│       └── ResponseViewerDialog.kt    # Response display
├── viewmodel/
│   └── DebugViewModel.kt              # Debug screen state
└── data/
    └── api/
        └── debug/
            └── EndpointDefinitions.kt # Endpoint configurations
```

### Layout Files
```
app/src/main/res/layout/
├── activity_debug.xml                 # Main debug layout
├── item_endpoint.xml                  # Endpoint list item
├── dialog_response_viewer.xml         # Response dialog
└── layout_request_config.xml          # Request configuration
```

### Access Control

```kotlin
// In MainActivity.kt
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // ...
    
    // Debug mode access - only in debug builds
    if (BuildConfig.DEBUG) {
        setupDebugModeAccess()
    }
}

private fun setupDebugModeAccess() {
    // Option 1: Long press on version text
    binding.versionText.setOnLongClickListener {
        startActivity(Intent(this, DebugActivity::class.java))
        true
    }
    
    // Option 2: Shake gesture (using sensor)
    // Option 3: Secret menu item
}
```

### Endpoint Definition

```kotlin
data class ApiEndpoint(
    val id: String,
    val name: String,
    val method: HttpMethod,
    val path: String,
    val pathParams: List<String> = emptyList(),
    val queryParams: List<String> = emptyList(),
    val hasBody: Boolean = false,
    val description: String
)

enum class HttpMethod { GET, POST, PUT, DELETE }

object EndpointDefinitions {
    val endpoints = listOf(
        ApiEndpoint(
            id = "customer_info",
            name = "Customer Info",
            method = HttpMethod.GET,
            path = "/customers/{customer_id}/",
            pathParams = listOf("customer_id"),
            description = "Get customer information and validate token"
        ),
        ApiEndpoint(
            id = "list_orders",
            name = "List Orders",
            method = HttpMethod.GET,
            path = "/customers/{customer_id}/orders/",
            pathParams = listOf("customer_id"),
            queryParams = listOf("page"),
            description = "Get paginated list of orders"
        ),
        ApiEndpoint(
            id = "get_order",
            name = "Get Order Details",
            method = HttpMethod.GET,
            path = "/customers/{customer_id}/orders/{order_id}/",
            pathParams = listOf("customer_id", "order_id"),
            description = "Get details of a specific order"
        ),
        ApiEndpoint(
            id = "get_cart",
            name = "Get Cart",
            method = HttpMethod.GET,
            path = "/customers/{customer_id}/cart/",
            pathParams = listOf("customer_id"),
            description = "Get current shopping cart"
        ),
        ApiEndpoint(
            id = "get_recommendations",
            name = "Get Recommendations",
            method = HttpMethod.GET,
            path = "/customers/{customer_id}/recommendations/myregulars/{type}/",
            pathParams = listOf("customer_id", "type"),
            description = "Get personalized product recommendations"
        ),
        ApiEndpoint(
            id = "set_warehouse",
            name = "Set Warehouse",
            method = HttpMethod.PUT,
            path = "/postal-codes/actions/change-pc/",
            hasBody = true,
            description = "Change warehouse by postal code"
        )
    )
}
```

### DebugViewModel State

```kotlin
sealed class DebugState {
    object Idle : DebugState()
    object Loading : DebugState()
    data class RequestReady(val request: DebugRequest) : DebugState()
    data class Success(val response: DebugResponse) : DebugState()
    data class Error(val message: String, val response: DebugResponse?) : DebugState()
}

data class DebugRequest(
    val method: HttpMethod,
    val url: String,
    val headers: Map<String, String>,
    val body: String?
)

data class DebugResponse(
    val statusCode: Int,
    val statusMessage: String,
    val headers: Map<String, String>,
    val body: String,
    val timeMs: Long
)
```

## Validation Checklist

Each endpoint should be validated for:
- [ ] Request is built correctly
- [ ] Headers are correct (Authorization, Content-Type)
- [ ] Response is parsed successfully
- [ ] Error cases are handled (401, 404, 500)
- [ ] Rate limiting is respected
- [ ] Timeouts are handled

## Security Considerations

1. **Debug Only**: Ensure all debug code is excluded from release builds
2. **No Token Logging**: Never log actual token values
3. **Sanitize Responses**: Be careful when displaying sensitive data
4. **Clear on Exit**: Option to clear token when leaving debug mode

## Testing Workflow

1. **Token Setup**
   - Paste token in Token Management section
   - Save customer_id
   - Validate token using Customer Info endpoint

2. **Order Testing**
   - Test List Orders endpoint
   - Get a valid order_id from response
   - Test Get Order Details with that order_id

3. **Cart Testing**
   - Test Get Cart endpoint
   - Verify cart structure

4. **Recommendations Testing**
   - Test with type=precision
   - Test with type=recall

5. **Error Testing**
   - Test with invalid token
   - Test with invalid customer_id
   - Test with invalid order_id
   - Test network timeout behavior

## Menu Access (Hidden)

To access Debug Mode in the app:
1. Go to Settings or About screen
2. Long-press on the version number 5 times
3. Debug Mode option becomes visible
4. Enter Debug Mode

## Conclusion

Debug Mode provides essential tooling for validating the Mercadona API integration before building production features. It ensures each endpoint works correctly and helps identify issues early in development.
