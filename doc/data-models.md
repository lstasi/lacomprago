# LaCompraGo - Data Models

## Overview

This document defines the simplified data models used in LaCompraGo. The application uses JSON files for local storage and simple data classes for API communication.

## Storage Strategy

### JSON Files
- **products.json**: Product list with frequency and last purchase date
- **processed_orders.json**: List of order IDs that have been processed

## Domain Models

### 1. Product Model

The core data structure for tracking products.

```kotlin
data class Product(
    val id: String,
    val name: String,
    val frequency: Int,              // Number of times purchased
    val lastPurchase: Long,          // Timestamp of last purchase
    val category: String? = null,    // Optional category
    val totalQuantity: Double = 0.0  // Total quantity purchased
)
```

**JSON Representation**
```json
{
  "id": "prod_123",
  "name": "Milk 1L",
  "frequency": 24,
  "lastPurchase": 1698765432000,
  "category": "Dairy",
  "totalQuantity": 48.0
}
```

### 2. Product List Storage

Container for all products.

```kotlin
data class ProductList(
    val products: List<Product>,
    val lastUpdated: Long = System.currentTimeMillis()
)
```

**JSON File: products.json**
```json
{
  "products": [
    {
      "id": "prod_123",
      "name": "Milk 1L",
      "frequency": 24,
      "lastPurchase": 1698765432000,
      "category": "Dairy",
      "totalQuantity": 48.0
    },
    {
      "id": "prod_456",
      "name": "Bread",
      "frequency": 20,
      "lastPurchase": 1698851832000,
      "category": "Bakery",
      "totalQuantity": 20.0
    }
  ],
  "lastUpdated": 1698865432000
}
```

### 3. Processed Orders Tracking

Track which orders have been processed to avoid duplicates.

```kotlin
data class ProcessedOrders(
    val processedOrderIds: MutableList<String> = mutableListOf(),
    val lastProcessedAt: Long? = null
)
```

**JSON File: processed_orders.json**
```json
{
  "processedOrderIds": [
    "order_123",
    "order_456",
    "order_789"
  ],
  "lastProcessedAt": 1698865432000
}
```

## API Models

### 1. Order Response

Order data received from API.

```kotlin
data class OrderResponse(
    val id: String,
    val orderNumber: String,
    val orderDate: String,          // ISO 8601 format
    val totalAmount: Double,
    val items: List<OrderItemResponse>
)
```

**API JSON Response**
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

### 2. Order Item Response

Individual item in an order.

```kotlin
data class OrderItemResponse(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val category: String? = null
)
```

### 3. Order List Response

List of orders from API.

```kotlin
data class OrderListResponse(
    val orders: List<OrderSummary>
)

data class OrderSummary(
    val id: String,
    val orderDate: String
)
```

**API JSON Response**
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

### 4. Shopping Cart Request

Data sent to API to create shopping cart.

```kotlin
data class CartRequest(
    val items: List<CartItemRequest>
)

data class CartItemRequest(
    val productId: String,
    val quantity: Int
)
```

**API JSON Request**
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

### 5. Shopping Cart Response

Response from API after cart creation.

```kotlin
data class CartResponse(
    val cartId: String,
    val status: String,
    val createdAt: String
)
```

**API JSON Response**
```json
{
  "cartId": "cart_789",
  "status": "CREATED",
  "createdAt": "2024-10-28T12:00:00Z"
}
```

## UI State Models

### 1. Product List UI State

```kotlin
sealed class ProductListState {
    object Loading : ProductListState()
    data class Success(val products: List<Product>) : ProductListState()
    data class Error(val message: String) : ProductListState()
}
```

### 2. Order Processing State

```kotlin
sealed class OrderProcessingState {
    object Idle : OrderProcessingState()
    data class Processing(
        val currentOrder: Int,
        val totalOrders: Int,
        val currentOrderId: String
    ) : OrderProcessingState()
    data class Completed(val processedCount: Int) : OrderProcessingState()
    data class Cancelled(val processedCount: Int) : OrderProcessingState()
    data class Error(val message: String, val processedCount: Int) : OrderProcessingState()
}
```

### 3. Cart Creation State

```kotlin
sealed class CartState {
    object Idle : CartState()
    object Creating : CartState()
    data class Success(val cartId: String) : CartState()
    data class Error(val message: String) : CartState()
}
```

## Data Operations

### 1. Product Update Logic

When processing an order, update product information:

```kotlin
fun updateProductFromOrder(
    existingProduct: Product?,
    orderItem: OrderItemResponse,
    orderDate: Long
): Product {
    return if (existingProduct != null) {
        // Update existing product
        existingProduct.copy(
            frequency = existingProduct.frequency + 1,
            lastPurchase = maxOf(existingProduct.lastPurchase, orderDate),
            totalQuantity = existingProduct.totalQuantity + orderItem.quantity
        )
    } else {
        // Create new product entry
        Product(
            id = orderItem.productId,
            name = orderItem.productName,
            frequency = 1,
            lastPurchase = orderDate,
            category = orderItem.category,
            totalQuantity = orderItem.quantity.toDouble()
        )
    }
}
```

### 2. Product Frequency Calculation

```kotlin
fun calculateFrequencyScore(product: Product, maxFrequency: Int): Double {
    return if (maxFrequency > 0) {
        product.frequency.toDouble() / maxFrequency.toDouble()
    } else {
        0.0
    }
}

fun getRecommendationLevel(frequencyScore: Double): String {
    return when {
        frequencyScore >= 0.7 -> "ESSENTIAL"
        frequencyScore >= 0.4 -> "REGULAR"
        frequencyScore >= 0.2 -> "OCCASIONAL"
        else -> "RARE"
    }
}
```

### 3. Product Sorting

```kotlin
enum class ProductSortOption {
    FREQUENCY_DESC,
    FREQUENCY_ASC,
    LAST_PURCHASE_DESC,
    LAST_PURCHASE_ASC,
    NAME_ASC,
    NAME_DESC
}

fun sortProducts(products: List<Product>, sortOption: ProductSortOption): List<Product> {
    return when (sortOption) {
        ProductSortOption.FREQUENCY_DESC -> products.sortedByDescending { it.frequency }
        ProductSortOption.FREQUENCY_ASC -> products.sortedBy { it.frequency }
        ProductSortOption.LAST_PURCHASE_DESC -> products.sortedByDescending { it.lastPurchase }
        ProductSortOption.LAST_PURCHASE_ASC -> products.sortedBy { it.lastPurchase }
        ProductSortOption.NAME_ASC -> products.sortedBy { it.name }
        ProductSortOption.NAME_DESC -> products.sortedByDescending { it.name }
    }
}
```

## JSON Serialization

### Using Gson

```kotlin
class JsonStorage(private val context: Context) {
    private val gson = Gson()
    
    fun saveProductList(productList: ProductList) {
        val json = gson.toJson(productList)
        context.openFileOutput("products.json", Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }
    
    fun loadProductList(): ProductList? {
        return try {
            context.openFileInput("products.json").use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                gson.fromJson(json, ProductList::class.java)
            }
        } catch (e: FileNotFoundException) {
            null
        }
    }
    
    fun saveProcessedOrders(processedOrders: ProcessedOrders) {
        val json = gson.toJson(processedOrders)
        context.openFileOutput("processed_orders.json", Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }
    
    fun loadProcessedOrders(): ProcessedOrders {
        return try {
            context.openFileInput("processed_orders.json").use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                gson.fromJson(json, ProcessedOrders::class.java)
            }
        } catch (e: FileNotFoundException) {
            ProcessedOrders()
        }
    }
}
```

## Data Validation

### Product Validation

```kotlin
fun validateProduct(product: Product): Boolean {
    return product.id.isNotBlank() &&
           product.name.isNotBlank() &&
           product.frequency > 0 &&
           product.lastPurchase > 0
}
```

### Order Validation

```kotlin
fun validateOrder(order: OrderResponse): Boolean {
    return order.id.isNotBlank() &&
           order.items.isNotEmpty() &&
           order.items.all { validateOrderItem(it) }
}

fun validateOrderItem(item: OrderItemResponse): Boolean {
    return item.productId.isNotBlank() &&
           item.productName.isNotBlank() &&
           item.quantity > 0
}
```

## Data Flow

### Order Processing Flow

```
1. Fetch order IDs from API
2. Load processed_orders.json
3. Filter out already processed orders
4. For each unprocessed order:
   a. Fetch order details from API
   b. Load products.json
   c. For each item in order:
      - Find product in list
      - Update or create product entry
      - Increment frequency
      - Update last purchase if newer
   d. Save updated products.json
   e. Add order ID to processed list
   f. Save processed_orders.json
   g. Update UI progress
```

### Cart Creation Flow

```
1. Get products sorted by frequency
2. Select top products (ESSENTIAL + REGULAR)
3. Calculate suggested quantities
4. Create CartRequest
5. Send to API
6. Handle response
```

## File Storage Location

All JSON files are stored in app-private storage:

```kotlin
// Files are stored in:
context.filesDir // /data/data/com.lacomprago/files/

// Specific files:
// - products.json
// - processed_orders.json
```

**Security**
- Files are in app-private directory
- Not accessible by other apps
- Automatically removed on app uninstall
- No special permissions needed

## Data Persistence

### App Lifecycle

**On App Start**
- Load products.json (if exists)
- Load processed_orders.json (if exists)
- Display product list

**During Order Processing**
- Update products.json after each order
- Update processed_orders.json after each order
- Maintain partial progress

**On App Close**
- Data automatically persisted in JSON files
- No special cleanup needed

## Error Handling

### File I/O Errors

```kotlin
sealed class StorageError {
    object FileNotFound : StorageError()
    object ParseError : StorageError()
    object WriteError : StorageError()
    data class Unknown(val exception: Exception) : StorageError()
}

fun handleStorageError(error: StorageError): String {
    return when (error) {
        is StorageError.FileNotFound -> "No data file found. Start fresh."
        is StorageError.ParseError -> "Error reading data. File may be corrupted."
        is StorageError.WriteError -> "Error saving data. Check storage space."
        is StorageError.Unknown -> "Storage error: ${error.exception.message}"
    }
}
```

## Testing

### Unit Tests

```kotlin
@Test
fun `test product update logic`() {
    val existing = Product(
        id = "prod_123",
        name = "Milk",
        frequency = 5,
        lastPurchase = 1000L,
        totalQuantity = 10.0
    )
    
    val orderItem = OrderItemResponse(
        productId = "prod_123",
        productName = "Milk",
        quantity = 2
    )
    
    val updated = updateProductFromOrder(existing, orderItem, 2000L)
    
    assertEquals(6, updated.frequency)
    assertEquals(2000L, updated.lastPurchase)
    assertEquals(12.0, updated.totalQuantity)
}

@Test
fun `test JSON serialization`() {
    val productList = ProductList(
        products = listOf(
            Product("1", "Milk", 5, 1000L)
        )
    )
    
    val json = gson.toJson(productList)
    val deserialized = gson.fromJson(json, ProductList::class.java)
    
    assertEquals(productList.products.size, deserialized.products.size)
    assertEquals(productList.products[0].name, deserialized.products[0].name)
}
```

## Conclusion

This simplified data model approach using JSON files provides:
- Easy to understand data structures
- Simple serialization/deserialization
- No database dependencies
- Efficient for small to medium datasets
- Perfect for LaCompraGo's requirements
