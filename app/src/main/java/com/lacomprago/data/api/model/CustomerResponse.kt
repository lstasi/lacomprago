package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Response from the Mercadona API when fetching customer information.
 * Used to validate token and retrieve customer details.
 *
 * Endpoint: GET /api/customers/{customer_id}/
 *
 * @property id Numeric customer ID
 * @property uuid UUID of the customer
 * @property cartId Cart ID associated with this customer
 * @property currentPostalCode Currently set postal code
 * @property email Customer email address
 * @property name Customer first name
 * @property lastName Customer last name
 * @property sendOffers Whether customer opted in for offers
 */
data class CustomerResponse(
    val id: Long,
    val uuid: String,
    @SerializedName("cart_id")
    val cartId: String?,
    @SerializedName("current_postal_code")
    val currentPostalCode: String?,
    val email: String?,
    val name: String?,
    @SerializedName("last_name")
    val lastName: String?,
    @SerializedName("send_offers")
    val sendOffers: Boolean = false
)
