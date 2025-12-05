package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Response from the Mercadona API when fetching order details.
 *
 * Endpoint: GET /api/customers/{customer_id}/orders/{order_id}/
 *
 * This extends the OrderResult with additional product line items.
 * The response structure is the same as OrderResult plus the lines field.
 *
 * @property id Order ID
 * @property orderId Order ID (duplicate field from API)
 * @property lines List of products/items in the order
 * @property address Delivery address details
 * @property startDate Delivery window start time
 * @property endDate Delivery window end time
 * @property price Total price of products
 * @property productsCount Number of products in the order
 * @property status Numeric status code
 * @property statusUi Human-readable status (e.g., "delivered")
 * @property summary Order summary with totals and taxes
 * @property warehouseCode Warehouse identifier
 */
data class OrderDetailsResponse(
    val id: Long,
    @SerializedName("order_id")
    val orderId: Long,
    val lines: List<OrderLine>?,
    val address: OrderAddress?,
    @SerializedName("start_date")
    val startDate: String?,
    @SerializedName("end_date")
    val endDate: String?,
    val price: String?,
    @SerializedName("products_count")
    val productsCount: Int?,
    val status: Int?,
    @SerializedName("status_ui")
    val statusUi: String?,
    val summary: OrderSummaryDetails?,
    @SerializedName("warehouse_code")
    val warehouseCode: String?
)

/**
 * Individual product line in an order.
 *
 * This matches the CartLine structure but is used for order history.
 *
 * @property product Product details
 * @property quantity Quantity purchased
 * @property sources Source identifiers
 * @property version Line version
 */
data class OrderLine(
    val product: CartProduct?,
    val quantity: Double?,
    val sources: List<String>?,
    val version: Int?
)

