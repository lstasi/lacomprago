package com.lacomprago.model

/**
 * Container for all products.
 * Used for serialization/deserialization of the products.json file.
 *
 * @property products List of all products
 * @property lastUpdated Timestamp when the list was last updated (in milliseconds)
 */
data class ProductList(
    val products: List<Product>,
    val lastUpdated: Long = System.currentTimeMillis()
)
