package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Payment method details for an order.
 *
 * Based on actual API response structure (see fixtures/orders.json).
 *
 * @property id Unique identifier for the payment method
 * @property creditCardType Type of credit card (1 = Visa, etc.)
 * @property creditCardNumber Last 4 digits of the card
 * @property expiresMonth Card expiration month (MM format)
 * @property expiresYear Card expiration year (YYYY format)
 * @property defaultCard Whether this is the default payment method
 * @property expirationStatus Status of card expiration ("valid", "expired", etc.)
 */
data class PaymentMethod(
    val id: Long,
    @SerializedName("credit_card_type")
    val creditCardType: Int,
    @SerializedName("credit_card_number")
    val creditCardNumber: String,
    @SerializedName("expires_month")
    val expiresMonth: String,
    @SerializedName("expires_year")
    val expiresYear: String,
    @SerializedName("default_card")
    val defaultCard: Boolean,
    @SerializedName("expiration_status")
    val expirationStatus: String
)
