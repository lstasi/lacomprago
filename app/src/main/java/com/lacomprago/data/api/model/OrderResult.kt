package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Full order result from the API orders list response.
 *
 * Based on actual API response structure (see fixtures/orders.json).
 *
 * @property id Unique identifier for the order
 * @property orderId The order ID (same as id)
 * @property address Delivery address details
 * @property changesUntil Deadline for making changes to the order
 * @property customerPhone Full phone number with country code
 * @property startDate Delivery window start time
 * @property endDate Delivery window end time
 * @property finalPrice Whether the price is final
 * @property paymentMethod Payment method details
 * @property paymentStatus Payment status code
 * @property phoneCountryCode Country code for phone
 * @property phoneNationalNumber National phone number
 * @property price Total price of products
 * @property productsCount Number of products in the order
 * @property slot Delivery slot details
 * @property slotSize Size of the delivery slot
 * @property status Numeric status code
 * @property statusUi Human-readable status (e.g., "delivered")
 * @property summary Order summary with totals and taxes
 * @property serviceRatingToken Token for rating the service
 * @property clickAndCollect Whether order is click and collect
 * @property warehouseCode Warehouse identifier
 * @property lastEditMessage Last edit message, if any
 * @property timezone Timezone for the order
 */
data class OrderResult(
    val id: Long,
    @SerializedName("order_id")
    val orderId: Long,
    val address: OrderAddress,
    @SerializedName("changes_until")
    val changesUntil: String,
    @SerializedName("customer_phone")
    val customerPhone: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("final_price")
    val finalPrice: Boolean,
    @SerializedName("payment_method")
    val paymentMethod: PaymentMethod,
    @SerializedName("payment_status")
    val paymentStatus: Int,
    @SerializedName("phone_country_code")
    val phoneCountryCode: String,
    @SerializedName("phone_national_number")
    val phoneNationalNumber: String,
    val price: String,
    @SerializedName("products_count")
    val productsCount: Int,
    val slot: DeliverySlot,
    @SerializedName("slot_size")
    val slotSize: Int,
    val status: Int,
    @SerializedName("status_ui")
    val statusUi: String,
    val summary: OrderSummaryDetails,
    @SerializedName("service_rating_token")
    val serviceRatingToken: String,
    @SerializedName("click_and_collect")
    val clickAndCollect: Boolean,
    @SerializedName("warehouse_code")
    val warehouseCode: String,
    @SerializedName("last_edit_message")
    val lastEditMessage: String?,
    val timezone: String
)
