package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Response from the Mercadona API when fetching or updating cart.
 *
 * Endpoint: GET/PUT /api/customers/{customer_id}/cart/
 *
 * @property id Cart ID
 * @property lines List of cart lines (products)
 * @property openOrderId ID of open order if any
 * @property productsCount Number of products in cart
 * @property summary Cart summary with total
 * @property version Cart version (needed for updates)
 */
data class MercadonaCartResponse(
    val id: String,
    val lines: List<CartLine>,
    @SerializedName("open_order_id")
    val openOrderId: Long?,
    @SerializedName("products_count")
    val productsCount: Int,
    val summary: CartSummary,
    val version: Int
)

/**
 * Individual line item in the cart.
 *
 * @property product Product details
 * @property quantity Quantity of the product
 * @property sources Source identifiers for the product
 * @property version Line version
 */
data class CartLine(
    val product: CartProduct,
    val quantity: Double,
    val sources: List<String>,
    val version: Int
)

/**
 * Product information in cart line.
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
 * @property priceInstructions Pricing details
 * @property badges Product badges (water, age check, etc.)
 */
data class CartProduct(
    val id: String,
    @SerializedName("display_name")
    val displayName: String,
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
    val badges: ProductBadges?
)

/**
 * Product category information.
 *
 * @property id Category ID
 * @property name Category name
 * @property level Category level (0 = top level)
 * @property order Display order
 */
data class ProductCategory(
    val id: Int,
    val name: String,
    val level: Int,
    val order: Int
)

/**
 * Pricing information for a product.
 *
 * @property unitPrice Price per unit
 * @property bulkPrice Bulk price
 * @property referencePrice Reference price
 * @property referenceFormat Reference format (e.g., "kg")
 * @property unitSize Unit size
 * @property sizeFormat Size format
 * @property sellingMethod Selling method identifier
 * @property iva VAT percentage
 * @property isNew Whether product is new
 * @property isPack Whether product is a pack
 * @property priceDecreased Whether price has decreased
 * @property unitSelector Whether unit selector is available
 * @property bunchSelector Whether bunch selector is available
 * @property approxSize Whether size is approximate
 * @property minBunchAmount Minimum bunch amount
 * @property incrementBunchAmount Bunch increment amount
 * @property packSize Pack size if applicable
 * @property totalUnits Total units if applicable
 * @property unitName Unit name if applicable
 * @property drainedWeight Drained weight if applicable
 */
data class PriceInstructions(
    @SerializedName("unit_price")
    val unitPrice: String?,
    @SerializedName("bulk_price")
    val bulkPrice: String?,
    @SerializedName("reference_price")
    val referencePrice: String?,
    @SerializedName("reference_format")
    val referenceFormat: String?,
    @SerializedName("unit_size")
    val unitSize: Double?,
    @SerializedName("size_format")
    val sizeFormat: String?,
    @SerializedName("selling_method")
    val sellingMethod: Int?,
    val iva: Int?,
    @SerializedName("is_new")
    val isNew: Boolean = false,
    @SerializedName("is_pack")
    val isPack: Boolean = false,
    @SerializedName("price_decreased")
    val priceDecreased: Boolean = false,
    @SerializedName("unit_selector")
    val unitSelector: Boolean = true,
    @SerializedName("bunch_selector")
    val bunchSelector: Boolean = false,
    @SerializedName("approx_size")
    val approxSize: Boolean = false,
    @SerializedName("min_bunch_amount")
    val minBunchAmount: Double?,
    @SerializedName("increment_bunch_amount")
    val incrementBunchAmount: Double?,
    @SerializedName("pack_size")
    val packSize: Int?,
    @SerializedName("total_units")
    val totalUnits: Int?,
    @SerializedName("unit_name")
    val unitName: String?,
    @SerializedName("drained_weight")
    val drainedWeight: Double?
)

/**
 * Product badges for special properties.
 *
 * @property isWater Whether product is water (for volume calculations)
 * @property requiresAgeCheck Whether product requires age verification
 */
data class ProductBadges(
    @SerializedName("is_water")
    val isWater: Boolean = false,
    @SerializedName("requires_age_check")
    val requiresAgeCheck: Boolean = false
)

/**
 * Cart summary information.
 *
 * @property total Total price formatted as string
 */
data class CartSummary(
    val total: String
)
