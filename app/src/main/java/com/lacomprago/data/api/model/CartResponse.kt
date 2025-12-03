package com.lacomprago.data.api.model

/**
 * Response from the API after creating a shopping cart.
 *
 * @property cartId Unique identifier for the created cart
 * @property status Status of the cart (e.g., "CREATED")
 * @property createdAt ISO 8601 formatted timestamp
 */
data class CartResponse(
    val cartId: String,
    val status: String,
    val createdAt: String
)
