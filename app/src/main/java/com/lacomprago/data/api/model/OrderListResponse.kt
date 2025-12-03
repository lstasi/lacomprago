package com.lacomprago.data.api.model

/**
 * Response from the API when fetching the list of orders.
 *
 * @property orders List of order summaries
 */
data class OrderListResponse(
    val orders: List<OrderSummary>
)
