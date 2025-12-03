package com.lacomprago.data.api.model

/**
 * Request body for creating a shopping cart.
 *
 * @property items List of items to add to the cart
 */
data class CartRequest(
    val items: List<CartItemRequest>
)
