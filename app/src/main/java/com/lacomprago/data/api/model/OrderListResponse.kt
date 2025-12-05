package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Response from the API when fetching the list of orders.
 *
 * Based on actual API response structure (see fixtures/orders.json).
 *
 * @property nextPage URL to fetch the next page of results, null if no more pages
 * @property results List of order details
 */
data class OrderListResponse(
    @SerializedName("next_page")
    val nextPage: String?,
    val results: List<OrderResult>
)
