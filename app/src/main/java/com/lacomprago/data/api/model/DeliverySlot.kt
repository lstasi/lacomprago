package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Delivery time slot details for an order.
 *
 * Based on actual API response structure (see fixtures/orders.json).
 *
 * @property id Unique identifier for the slot
 * @property start Slot start time in ISO 8601 format
 * @property end Slot end time in ISO 8601 format
 * @property price Delivery price for this slot
 * @property available Whether the slot is available
 * @property cutoffTime Deadline to select this slot
 * @property timezone Timezone for the slot times
 */
data class DeliverySlot(
    val id: String,
    val start: String,
    val end: String,
    val price: String,
    val available: Boolean,
    @SerializedName("cutoff_time")
    val cutoffTime: String,
    val timezone: String
)
