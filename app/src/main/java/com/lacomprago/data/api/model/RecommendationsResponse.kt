package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Response from the Mercadona API when fetching recommendations.
 *
 * Endpoint: GET /api/customers/{customer_id}/recommendations/myregulars/{type}/
 * where type is "precision" (what I most buy) or "recall" (I also buy)
 *
 * @property nextPage URL to fetch more results, null if no more pages
 * @property results List of recommended products
 */
data class RecommendationsResponse(
    @SerializedName("next_page")
    val nextPage: String?,
    val results: List<RecommendationItem>
)

/**
 * Individual recommendation item.
 *
 * @property product Product details
 * @property recommendedQuantity Suggested quantity to purchase
 * @property sellingMethod Selling method identifier
 */
data class RecommendationItem(
    val product: CartProduct,
    @SerializedName("recommended_quantity")
    val recommendedQuantity: Int,
    @SerializedName("selling_method")
    val sellingMethod: Int
)
