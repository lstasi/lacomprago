package com.lacomprago.data.api.model

/**
 * Individual item in an order response.
 *
 * @property productId Unique identifier for the product
 * @property productName Display name of the product
 * @property quantity Number of items purchased
 * @property category Optional product category
 */
data class OrderItemResponse(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val category: String? = null
)
