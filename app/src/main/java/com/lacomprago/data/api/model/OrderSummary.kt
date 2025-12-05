package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Summary of an order from the API order list.
 *
 * Based on actual API response structure (see fixtures/orders.json).
 *
 * @property id Unique identifier for the order
 * @property orderId The order ID (same as id in current API)
 * @property price Total price of products (as String)
 * @property productsCount Number of products in the order
 * @property status Numeric status code
 * @property statusUi Human-readable status (e.g., "delivered")
 * @property startDate ISO 8601 formatted start date
 * @property endDate ISO 8601 formatted end date
 * @property warehouseCode Warehouse identifier
 * @property clickAndCollect Whether order is click and collect
 */
data class OrderSummary(
    val id: Long,
    @SerializedName("order_id")
    val orderId: Long,
    val price: String,
    @SerializedName("products_count")
    val productsCount: Int,
    val status: Int,
    @SerializedName("status_ui")
    val statusUi: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("warehouse_code")
    val warehouseCode: String,
    @SerializedName("click_and_collect")
    val clickAndCollect: Boolean
)
