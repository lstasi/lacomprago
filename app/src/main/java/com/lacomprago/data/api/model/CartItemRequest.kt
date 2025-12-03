package com.lacomprago.data.api.model

/**
 * Item in a shopping cart request.
 *
 * @property productId Unique identifier for the product
 * @property quantity Number of items to add
 */
data class CartItemRequest(
    val productId: String,
    val quantity: Int
)
