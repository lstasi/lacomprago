package com.lacomprago.model

/**
 * Core data structure for tracking products.
 * Used for storing product information with purchase frequency tracking.
 *
 * @property id Unique identifier for the product
 * @property name Display name of the product
 * @property frequency Number of times the product has been purchased
 * @property lastPurchase Timestamp of the last purchase (in milliseconds)
 * @property category Optional product category
 * @property totalQuantity Total quantity purchased across all orders
 */
data class Product(
    val id: String,
    val name: String,
    val frequency: Int,
    val lastPurchase: Long,
    val category: String? = null,
    val totalQuantity: Double = 0.0
)
