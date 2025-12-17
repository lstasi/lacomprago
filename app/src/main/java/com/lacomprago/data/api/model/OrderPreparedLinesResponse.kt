package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Response from the Mercadona API when fetching prepared order lines.
 *
 * Endpoint: GET /api/customers/{customer_id}/orders/{order_id}/lines/prepared/?lang=es&wh={warehouse}
 *
 * This endpoint returns the actual prepared products from an order, including
 * quantity information and product details.
 *
 * @property nextPage URL to next page if paginated (usually null)
 * @property results List of prepared line items from the order
 */
data class OrderPreparedLinesResponse(
    @SerializedName("next_page")
    val nextPage: String?,
    val results: List<PreparedOrderLine>
)

/**
 * Individual prepared line item in an order.
 *
 * Contains information about what was ordered vs what was prepared,
 * plus full product details.
 *
 * @property orderedQuantity Quantity originally ordered
 * @property preparedQuantity Quantity actually prepared
 * @property preparationResult Result of preparation ("complete", "incomplete", etc.)
 * @property totalPreparedPrice Total price for prepared quantity
 * @property productId Product ID as string
 * @property product Full product details
 * @property originalPriceInstructions Original price information when ordered
 */
data class PreparedOrderLine(
    @SerializedName("ordered_quantity")
    val orderedQuantity: Double?,
    @SerializedName("prepared_quantity")
    val preparedQuantity: Double?,
    @SerializedName("preparation_result")
    val preparationResult: String?,
    @SerializedName("total_prepared_price")
    val totalPreparedPrice: String?,
    @SerializedName("product_id")
    val productId: String,
    val product: PreparedProductDetails?,
    @SerializedName("original_price_instructions")
    val originalPriceInstructions: PriceInstructions?
)

/**
 * Product details in prepared order line.
 *
 * Similar to CartProduct but with additional fields specific to prepared orders.
 *
 * @property id Product ID
 * @property displayName Display name of the product
 * @property slug Product slug for URLs
 * @property thumbnail URL to product thumbnail image
 * @property packaging Packaging type (e.g., "Caja", "Pieza")
 * @property published Whether product is published
 * @property limit Maximum quantity allowed
 * @property shareUrl URL to share product
 * @property categories Product categories
 * @property priceInstructions Current pricing details
 * @property badges Product badges (water, age check, etc.)
 * @property status Product status (null, "unavailable", etc.)
 * @property unavailableFrom Date from which product is unavailable
 * @property unavailableWeekdays Days of week product is unavailable
 */
data class PreparedProductDetails(
    val id: String,
    @SerializedName("display_name")
    val displayName: String?,
    val slug: String?,
    val thumbnail: String?,
    val packaging: String?,
    val published: Boolean = true,
    val limit: Int = 999,
    @SerializedName("share_url")
    val shareUrl: String?,
    val categories: List<ProductCategory>?,
    @SerializedName("price_instructions")
    val priceInstructions: PriceInstructions?,
    val badges: ProductBadges?,
    val status: String?,
    @SerializedName("unavailable_from")
    val unavailableFrom: String?,
    @SerializedName("unavailable_weekdays")
    val unavailableWeekdays: List<Int>?
)
