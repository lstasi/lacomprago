package com.lacomprago.data.api.model

/**
 * Full order details response from the API.
 *
 * @property id Unique identifier for the order
 * @property orderNumber Human-readable order number
 * @property orderDate ISO 8601 formatted date string
 * @property totalAmount Total order amount
 * @property items List of items in the order
 */
data class OrderResponse(
    val id: String,
    val orderNumber: String,
    val orderDate: String,
    val totalAmount: Double,
    val items: List<OrderItemResponse>
)
