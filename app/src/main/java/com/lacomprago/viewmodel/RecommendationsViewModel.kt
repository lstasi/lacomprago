package com.lacomprago.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lacomprago.data.api.ApiClient
import com.lacomprago.data.api.model.RecommendationItem
import com.lacomprago.model.Product
import com.lacomprago.model.RecommendationsState
import com.lacomprago.storage.JsonStorage
import kotlinx.coroutines.launch

/**
 * ViewModel for managing recommendations data and state.
 *
 * Handles:
 * - Fetching recommendations from API (both precision and recall types)
 * - Loading local products for comparison
 * - Managing UI state
 *
 * @property jsonStorage Storage for reading local product data
 * @property apiClient API client for fetching recommendations
 */
class RecommendationsViewModel(
    private val jsonStorage: JsonStorage,
    private val apiClient: ApiClient
) : ViewModel() {

    private val _recommendationsState = MutableLiveData<RecommendationsState>(RecommendationsState.Loading)
    val recommendationsState: LiveData<RecommendationsState> = _recommendationsState

    /**
     * Load recommendations from API and local products.
     *
     * Fetches both precision and recall recommendations in parallel
     * and loads local products for comparison.
     *
     * @param customerId Customer ID for API calls
     */
    fun loadRecommendations(customerId: String) {
        viewModelScope.launch {
            try {
                _recommendationsState.value = RecommendationsState.Loading

                // Fetch precision recommendations (what I most buy)
                val precisionResponse = apiClient.getRecommendations(customerId, "precision")
                Log.d(TAG, "Fetched ${precisionResponse.results.size} precision recommendations")

                // Fetch recall recommendations (I also buy)
                val recallResponse = apiClient.getRecommendations(customerId, "recall")
                Log.d(TAG, "Fetched ${recallResponse.results.size} recall recommendations")

                // Load local products for comparison
                val productList = jsonStorage.loadProductList()
                val localProducts = productList?.products ?: emptyList()
                Log.d(TAG, "Loaded ${localProducts.size} local products")

                // Check if we have any recommendations
                if (precisionResponse.results.isEmpty() && recallResponse.results.isEmpty()) {
                    _recommendationsState.value = RecommendationsState.Empty
                } else {
                    _recommendationsState.value = RecommendationsState.Success(
                        precisionItems = precisionResponse.results,
                        recallItems = recallResponse.results,
                        localProducts = localProducts
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recommendations", e)
                _recommendationsState.value = RecommendationsState.Error(
                    e.message ?: "Failed to load recommendations"
                )
            }
        }
    }

    /**
     * Check if a recommended product exists in local products.
     *
     * @param productId Product ID to check
     * @param localProducts List of local products
     * @return Product if found, null otherwise
     */
    fun findLocalProduct(productId: String, localProducts: List<Product>): Product? {
        return localProducts.find { it.id == productId }
    }

    /**
     * Get frequency comparison between recommended and local products.
     *
     * @param recommendationItem Recommendation item from API
     * @param localProducts List of local products
     * @return Pair of (recommended quantity, local frequency) or null if not found
     */
    fun getFrequencyComparison(
        recommendationItem: RecommendationItem,
        localProducts: List<Product>
    ): Pair<Int, Int>? {
        val localProduct = findLocalProduct(recommendationItem.product.id, localProducts)
        return if (localProduct != null) {
            Pair(recommendationItem.recommendedQuantity, localProduct.frequency)
        } else {
            null
        }
    }

    companion object {
        private const val TAG = "RecommendationsViewModel"
    }
}
