package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for the order lines endpoint.
 * Contains a list of order lines (products).
 */
data class OrderLinesResponse(
    @SerializedName("next_page")
    val nextPage: String?,

    @SerializedName("results")
    val results: List<OrderLine>
)

/**
 * Represents a single line in an order (a product).
 */
data class OrderLine(
    @SerializedName("ordered_quantity")
    val orderedQuantity: Double,

    @SerializedName("prepared_quantity")
    val preparedQuantity: Double,

    @SerializedName("total_prepared_price")
    val totalPreparedPrice: String,

    @SerializedName("product_id")
    val productId: String,

    @SerializedName("product")
    val product: ProductDetails?
)

/**
 * Detailed information about a product in an order line.
 */
data class ProductDetails(
    @SerializedName("id")
    val id: String,

    @SerializedName("display_name")
    val displayName: String?,

    @SerializedName("packaging")
    val packaging: String?,

    @SerializedName("thumbnail")
    val thumbnail: String?,

    @SerializedName("categories")
    val categories: List<Category>?
)

/**
 * Represents a product category.
 */
data class Category(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)
