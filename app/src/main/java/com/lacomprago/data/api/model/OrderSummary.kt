package com.lacomprago.data.api.model

/**
 * Summary of an order from the API order list.
 *
 * @property id Unique identifier for the order
 * @property orderDate ISO 8601 formatted date string
 */
data class OrderSummary(
    val id: String,
    val orderDate: String
)
