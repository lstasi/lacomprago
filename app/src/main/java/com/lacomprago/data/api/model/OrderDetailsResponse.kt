package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Response from the API when fetching order details with product items.
 *
 * This is used to get the individual products in an order.
 *
 * @property id Order ID
 * @property orderId Order ID (duplicate field from API)
 * @property items List of products/items in the order
 */
data class OrderDetailsResponse(
    val id: Long?,
    @SerializedName("order_id")
    val orderId: Long?,
    val items: List<OrderDetailItem>?
)

/**
 * Individual product item in an order details response.
 *
 * @property productId The product identifier
 * @property productName Display name of the product
 * @property quantity Number of units purchased
 * @property category Product category
 * @property price Unit price
 * @property totalPrice Total price for this item
 */
data class OrderDetailItem(
    @SerializedName("product_id")
    val productId: String?,
    @SerializedName("product_name")
    val productName: String?,
    val quantity: Int?,
    val category: String?,
    val price: String?,
    @SerializedName("total_price")
    val totalPrice: String?
)
