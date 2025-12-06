package com.lacomprago.model

import com.lacomprago.data.api.model.RecommendationItem

/**
 * Sealed class representing the different states of recommendations loading.
 */
sealed class RecommendationsState {
    /**
     * Initial state or when recommendations are being loaded.
     */
    object Loading : RecommendationsState()
    
    /**
     * Recommendations loaded successfully.
     *
     * @property precisionItems Recommendations of type "precision" (what I most buy)
     * @property recallItems Recommendations of type "recall" (I also buy)
     * @property localProducts Local products for comparison
     */
    data class Success(
        val precisionItems: List<RecommendationItem>,
        val recallItems: List<RecommendationItem>,
        val localProducts: List<Product>
    ) : RecommendationsState()
    
    /**
     * No recommendations available.
     */
    object Empty : RecommendationsState()
    
    /**
     * Error loading recommendations.
     *
     * @property message Error message
     */
    data class Error(val message: String) : RecommendationsState()
}
