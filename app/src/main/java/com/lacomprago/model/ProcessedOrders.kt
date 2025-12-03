package com.lacomprago.model

/**
 * Track which orders have been processed to avoid duplicates.
 * Used for serialization/deserialization of the processed_orders.json file.
 *
 * @property processedOrderIds List of order IDs that have been processed
 * @property lastProcessedAt Timestamp when the last order was processed (in milliseconds)
 */
data class ProcessedOrders(
    val processedOrderIds: List<String> = emptyList(),
    val lastProcessedAt: Long? = null
)
