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

## API Models (Mercadona API)

> **Note**: These models match the actual Mercadona API response structures.
> See [mercadona-api.md](./mercadona-api.md) for complete endpoint documentation.

### 1. Customer Response

Customer information returned when validating token.

```kotlin
data class CustomerResponse(
    val id: Int,
    val name: String,
    @SerializedName("last_name") val lastName: String,
    val email: String,
    @SerializedName("cart_id") val cartId: String,
    @SerializedName("current_postal_code") val currentPostalCode: String,
    val uuid: String,
    @SerializedName("send_offers") val sendOffers: Boolean = false
)
```

**API JSON Response**
```json
{
    "id": 12345,
    "name": "John",
    "last_name": "Doe",
    "email": "john@example.com",
    "cart_id": "cart_abc123",
    "current_postal_code": "28001",
    "uuid": "uuid-string",
    "send_offers": false
}
```

### 2. Order List Response

Paginated list of orders from the Mercadona API.

```kotlin
data class OrderListResponse(
    @SerializedName("next_page") val nextPage: Int?,
    val results: List<OrderSummary>
)

data class OrderSummary(
    val id: Int,
    @SerializedName("order_id") val orderId: Int,
    val status: Int,
    @SerializedName("status_ui") val statusUi: String,
    val price: String,
    @SerializedName("products_count") val productsCount: Int,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    val address: OrderAddress? = null,
    @SerializedName("payment_method") val paymentMethod: PaymentMethod? = null,
    val slot: DeliverySlot? = null,
    val summary: OrderPriceSummary? = null,
    @SerializedName("warehouse_code") val warehouseCode: String? = null
)

data class OrderAddress(
    val id: Int,
    val address: String,
    @SerializedName("address_detail") val addressDetail: String? = null,
    val comments: String? = null,
    @SerializedName("postal_code") val postalCode: String,
    val town: String,
    val latitude: String? = null,
    val longitude: String? = null,
    @SerializedName("permanent_address") val permanentAddress: Boolean = false,
    @SerializedName("entered_manually") val enteredManually: Boolean = false
)

data class PaymentMethod(
    val id: Int,
    @SerializedName("credit_card_number") val creditCardNumber: String,
    @SerializedName("credit_card_type") val creditCardType: Int,
    @SerializedName("default_card") val defaultCard: Boolean = false,
    @SerializedName("expiration_status") val expirationStatus: String,
    @SerializedName("expires_month") val expiresMonth: String,
    @SerializedName("expires_year") val expiresYear: String
)

data class DeliverySlot(
    val id: String,
    val available: Boolean,
    val start: String,
    val end: String,
    val price: String
)

data class OrderPriceSummary(
    val products: String,
    val slot: String,
    @SerializedName("tax_base") val taxBase: String,
    val taxes: String,
    val total: String,
    @SerializedName("volume_extra_cost") val volumeExtraCost: VolumeExtraCost? = null
)

data class VolumeExtraCost(
    @SerializedName("cost_by_extra_liter") val costByExtraLiter: String,
    val threshold: Int,
    val total: String,
    @SerializedName("total_extra_liters") val totalExtraLiters: Double
)
```

**API JSON Response**
```json
{
    "next_page": null,
    "results": [
        {
            "id": 8312430,
            "order_id": 8312430,
            "status": 2,
            "status_ui": "confirmed",
            "price": "65.94",
            "products_count": 28,
            "start_date": "2024-10-30T15:00:00Z",
            "end_date": "2024-10-30T16:00:00Z",
            "warehouse_code": "mad1",
            "summary": {
                "products": "65.94",
                "slot": "7.21",
                "tax_base": "67.07",
                "taxes": "6.08",
                "total": "73.15",
                "volume_extra_cost": {
                    "cost_by_extra_liter": "0.1",
                    "threshold": 70,
                    "total": "0.00",
                    "total_extra_liters": 0.0
                }
            }
        }
    ]
}
```

**Order Status Values**
| Status | Status UI | Description |
|--------|-----------|-------------|
| 0 | pending | Order is pending |
| 1 | processing | Order is being processed |
| 2 | confirmed | Order is confirmed |
| 3 | delivered | Order has been delivered |
| 4 | cancelled | Order was cancelled |

### 3. Order Details Response

Full order details including products (lines).

```kotlin
// Order details use the same OrderSummary structure but include lines
// when fetching individual order details

data class OrderLine(
    val product: MercadonaProduct,
    val quantity: Double,
    val version: Int,
    val sources: List<String> = emptyList()
)

data class MercadonaProduct(
    val id: String,
    @SerializedName("display_name") val displayName: String,
    val categories: List<ProductCategory>,
    @SerializedName("price_instructions") val priceInstructions: PriceInstructions,
    val packaging: String? = null,
    val limit: Int = 999,
    val published: Boolean = true,
    @SerializedName("share_url") val shareUrl: String? = null,
    val slug: String? = null,
    val thumbnail: String? = null,
    val badges: ProductBadges? = null
)

data class ProductCategory(
    val id: Int,
    val level: Int,
    val name: String,
    val order: Int
)

data class PriceInstructions(
    @SerializedName("unit_price") val unitPrice: String,
    @SerializedName("bulk_price") val bulkPrice: String,
    @SerializedName("reference_price") val referencePrice: String,
    @SerializedName("reference_format") val referenceFormat: String,
    @SerializedName("size_format") val sizeFormat: String,
    @SerializedName("unit_size") val unitSize: Double,
    @SerializedName("unit_name") val unitName: String? = null,
    @SerializedName("total_units") val totalUnits: Int? = null,
    val iva: Int,
    @SerializedName("is_new") val isNew: Boolean = false,
    @SerializedName("is_pack") val isPack: Boolean = false,
    @SerializedName("pack_size") val packSize: Int? = null,
    @SerializedName("selling_method") val sellingMethod: Int = 0,
    @SerializedName("approx_size") val approxSize: Boolean = false,
    @SerializedName("bunch_selector") val bunchSelector: Boolean = false,
    @SerializedName("min_bunch_amount") val minBunchAmount: Double = 1.0,
    @SerializedName("increment_bunch_amount") val incrementBunchAmount: Double = 1.0,
    @SerializedName("unit_selector") val unitSelector: Boolean = true,
    @SerializedName("price_decreased") val priceDecreased: Boolean = false,
    @SerializedName("drained_weight") val drainedWeight: Double? = null
)

data class ProductBadges(
    @SerializedName("is_water") val isWater: Boolean = false,
    @SerializedName("requires_age_check") val requiresAgeCheck: Boolean = false
)
```

### 4. Cart Response

Shopping cart data from Mercadona API.

```kotlin
data class CartResponse(
    val id: String,
    val lines: List<CartLine>,
    val version: Int,
    @SerializedName("products_count") val productsCount: Int,
    @SerializedName("open_order_id") val openOrderId: Int?,
    val summary: CartSummary
)

data class CartLine(
    val product: MercadonaProduct,
    val quantity: Double,
    val version: Int,
    val sources: List<String> = emptyList()
)

data class CartSummary(
    val total: String
)
```

**API JSON Response**
```json
{
    "id": "cart_abc123",
    "lines": [
        {
            "product": {
                "id": "51621",
                "display_name": "Queso camembert Marcillat",
                "categories": [
                    {
                        "id": 4,
                        "level": 0,
                        "name": "Charcutería y quesos",
                        "order": 159
                    }
                ],
                "price_instructions": {
                    "unit_price": "1.25",
                    "bulk_price": "5.21",
                    "reference_price": "5.21",
                    "reference_format": "kg",
                    "size_format": "kg",
                    "unit_size": 0.24,
                    "iva": 4,
                    "selling_method": 0
                },
                "packaging": "Caja",
                "limit": 999,
                "published": true
            },
            "quantity": 6.0,
            "sources": ["+RP"],
            "version": 5
        }
    ],
    "products_count": 1,
    "open_order_id": null,
    "summary": {
        "total": "7.50"
    },
    "version": 5
}
```

### 5. Cart Update Request

Request body for updating the shopping cart.

```kotlin
data class CartUpdateRequest(
    val id: String,
    val version: Int,
    val lines: List<CartLineRequest>
)

data class CartLineRequest(
    val quantity: Int,
    @SerializedName("product_id") val productId: String,
    val sources: List<String> = emptyList()
)
```

**API JSON Request**
```json
{
    "id": "cart_abc123",
    "version": 5,
    "lines": [
        {
            "quantity": 2,
            "product_id": "51621",
            "sources": []
        }
    ]
}
```

### 6. Recommendations Response

Personalized product recommendations from Mercadona API.

```kotlin
data class RecommendationsResponse(
    @SerializedName("next_page") val nextPage: Int?,
    val results: List<RecommendationItem>
)

data class RecommendationItem(
    val product: MercadonaProduct,
    @SerializedName("recommended_quantity") val recommendedQuantity: Int,
    @SerializedName("selling_method") val sellingMethod: Int = 0
)
```

**API JSON Response**
```json
{
    "next_page": null,
    "results": [
        {
            "product": {
                "id": "84780",
                "display_name": "Empanada de verduras",
                "categories": [
                    {
                        "id": 5,
                        "level": 0,
                        "name": "Panadería y pastelería",
                        "order": 508
                    }
                ],
                "price_instructions": {
                    "unit_price": "2.95",
                    "bulk_price": "5.90",
                    "reference_price": "5.90",
                    "reference_format": "kg",
                    "size_format": "kg",
                    "unit_size": 0.5,
                    "iva": 10,
                    "selling_method": 0
                },
                "packaging": "Pieza",
                "limit": 999,
                "published": true
            },
            "recommended_quantity": 1,
            "selling_method": 0
        }
    ]
}
```

**Recommendation Types**
| Type | Description |
|------|-------------|
| `precision` | Products the customer buys most frequently |
| `recall` | Products the customer also buys (less frequent) |

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

### 1. Product Update Logic (Mercadona API)

When processing an order from the Mercadona API, extract products from order lines:

```kotlin
/**
 * Extract products from Mercadona order lines.
 * The Mercadona API returns products in the 'lines' field.
 */
fun extractProductsFromOrder(
    orderLines: List<OrderLine>,
    orderDate: Long
): List<ProductUpdate> {
    return orderLines.map { line ->
        ProductUpdate(
            id = line.product.id,
            name = line.product.displayName,
            quantity = line.quantity,
            category = line.product.categories.firstOrNull()?.name,
            orderDate = orderDate
        )
    }
}

data class ProductUpdate(
    val id: String,
    val name: String,
    val quantity: Double,
    val category: String?,
    val orderDate: Long
)

/**
 * Update local product with data from Mercadona order.
 */
fun updateProductFromMercadona(
    existingProduct: Product?,
    update: ProductUpdate
): Product {
    return if (existingProduct != null) {
        // Update existing product
        existingProduct.copy(
            frequency = existingProduct.frequency + 1,
            lastPurchase = maxOf(existingProduct.lastPurchase, update.orderDate),
            totalQuantity = existingProduct.totalQuantity + update.quantity
        )
    } else {
        // Create new product entry
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

### Order Validation (Mercadona)

```kotlin
fun validateMercadonaOrder(order: OrderSummary): Boolean {
    return order.id > 0 &&
           order.statusUi.isNotBlank() &&
           order.startDate.isNotBlank()
}

fun validateOrderLine(line: OrderLine): Boolean {
    return line.product.id.isNotBlank() &&
           line.product.displayName.isNotBlank() &&
           line.quantity > 0
}
```

## Data Flow

### Order Processing Flow (Mercadona API)

```
1. Get customer_id from stored customer info
2. Fetch paginated order list from /customers/{customer_id}/orders/?page={n}
3. Load processed_orders.json
4. Filter out already processed orders
5. For each unprocessed order (with pagination handling):
   a. Fetch order details from /customers/{customer_id}/orders/{order_id}/
   b. Load products.json
   c. For each line in order:
      - Extract product info from line.product
      - Find product in local list by id
      - Update or create product entry
      - Increment frequency
      - Update last purchase if newer
   d. Save updated products.json
   e. Add order ID to processed list
   f. Save processed_orders.json
   g. Update UI progress
```

### Cart Creation Flow (Mercadona API)

```
1. Get products sorted by frequency
2. Select top products (ESSENTIAL + REGULAR)
3. Calculate suggested quantities
4. Get current cart from /customers/{customer_id}/cart/
5. Create CartUpdateRequest with:
   - cart_id from customer info
   - current cart version
   - product lines with product_id and quantity
6. PUT to /customers/{customer_id}/cart/
7. Handle response - cart returned with updated products
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
fun `test product update from Mercadona order line`() {
    val existing = Product(
        id = "51621",
        name = "Queso camembert Marcillat",
        frequency = 5,
        lastPurchase = 1000L,
        totalQuantity = 10.0
    )
    
    val update = ProductUpdate(
        id = "51621",
        name = "Queso camembert Marcillat",
        quantity = 2.0,
        category = "Charcutería y quesos",
        orderDate = 2000L
    )
    
    val updated = updateProductFromMercadona(existing, update)
    
    assertEquals(6, updated.frequency)
    assertEquals(2000L, updated.lastPurchase)
    assertEquals(12.0, updated.totalQuantity)
}

@Test
fun `test JSON serialization`() {
    val productList = ProductList(
        products = listOf(
            Product("51621", "Queso camembert", 5, 1000L)
        )
    )
    
    val json = gson.toJson(productList)
    val deserialized = gson.fromJson(json, ProductList::class.java)
    
    assertEquals(productList.products.size, deserialized.products.size)
    assertEquals(productList.products[0].name, deserialized.products[0].name)
}

@Test
fun `test Mercadona order list response parsing`() {
    val json = """
    {
        "next_page": null,
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
    """.trimIndent()
    
    val response = gson.fromJson(json, OrderListResponse::class.java)
    
    assertEquals(null, response.nextPage)
    assertEquals(1, response.results.size)
    assertEquals(8312430, response.results[0].id)
    assertEquals("confirmed", response.results[0].statusUi)
}
```

## Conclusion

This data model approach provides:
- Accurate models matching the Mercadona API response structures
- Easy to understand data structures for local storage
- Simple serialization/deserialization with Gson
- No database dependencies
- Efficient for small to medium datasets
- Perfect for LaCompraGo's requirements

### References

- [Mercadona API Reference](./mercadona-api.md)
- [API Integration Guide](./api-integration.md)
