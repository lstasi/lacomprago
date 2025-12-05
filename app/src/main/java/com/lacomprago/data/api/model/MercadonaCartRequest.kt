package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Request body for updating the Mercadona cart.
 *
 * Endpoint: PUT /api/customers/{customer_id}/cart/
 *
 * Important: The version must match the current cart version.
 * Sending an empty lines array clears the cart.
 *
 * @property id Cart ID
 * @property version Cart version (must match current version)
 * @property lines List of cart lines to set
 */
data class MercadonaCartRequest(
    val id: String,
    val version: Int,
    val lines: List<CartLineRequest>
)

/**
 * Individual line item for cart update request.
 *
 * @property productId Product ID to add
 * @property quantity Quantity to set
 * @property sources Source identifiers (usually empty)
 */
data class CartLineRequest(
    @SerializedName("product_id")
    val productId: String,
    val quantity: Double,
    val sources: List<String> = emptyList()
)
