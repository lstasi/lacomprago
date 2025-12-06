package com.lacomprago.model

import com.lacomprago.data.api.model.OrderResult

/**
 * Cached order list response from API.
 * Used for serialization/deserialization of the cached_orders.json file.
 *
 * @property orders List of all orders from API
 * @property cachedAt Timestamp when the list was cached (in milliseconds)
 */
data class CachedOrderList(
    val orders: List<OrderResult>,
    val cachedAt: Long = System.currentTimeMillis()
)
