package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Delivery address for an order.
 *
 * Based on actual API response structure (see fixtures/orders.json).
 *
 * @property id Unique identifier for the address
 * @property address Street address
 * @property addressDetail Additional address details (apartment, floor, etc.)
 * @property town City/town name
 * @property comments Delivery instructions or comments
 * @property enteredManually Whether address was entered manually
 * @property latitude GPS latitude coordinate
 * @property longitude GPS longitude coordinate
 * @property permanentAddress Whether this is a permanent/saved address
 * @property postalCode Postal/ZIP code
 */
data class OrderAddress(
    val id: Long,
    val address: String,
    @SerializedName("address_detail")
    val addressDetail: String?,
    val town: String,
    val comments: String?,
    @SerializedName("entered_manually")
    val enteredManually: Boolean,
    val latitude: String,
    val longitude: String,
    @SerializedName("permanent_address")
    val permanentAddress: Boolean,
    @SerializedName("postal_code")
    val postalCode: String
)
