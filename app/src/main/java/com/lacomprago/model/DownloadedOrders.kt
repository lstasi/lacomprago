package com.lacomprago.model

import com.lacomprago.data.api.model.OrderDetailsResponse

/**
 * Track which orders have been downloaded with their details.
 * Used for serialization/deserialization of the downloaded_orders.json file.
 *
 * This is separate from ProcessedOrders - downloaded orders have their details
 * fetched from the API, while processed orders have had their products extracted.
 *
 * @property downloadedOrderIds Set of order IDs that have been downloaded
 * @property orderDetails Map of order ID to order details
 * @property lastDownloadedAt Timestamp when the last order was downloaded (in milliseconds)
 */
data class DownloadedOrders(
    val downloadedOrderIds: Set<String> = emptySet(),
    val orderDetails: Map<String, OrderDetailsResponse> = emptyMap(),
    val lastDownloadedAt: Long? = null
)
