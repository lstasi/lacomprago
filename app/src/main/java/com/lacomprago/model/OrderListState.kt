package com.lacomprago.model

/**
 * Sealed class representing the possible states of the order list screen.
 */
sealed class OrderListState {
    /**
     * Loading state - fetching order list from cache or API.
     */
    object Loading : OrderListState()
    
    /**
     * Success state - order list loaded successfully.
     * @property totalOrders Total number of orders from API
     * @property downloadedCount Number of orders that have been downloaded
     * @property processedCount Number of orders already processed
     * @property unprocessedCount Number of orders remaining to process
     * @property fromCache Whether the data was loaded from cache
     * @property totalProducts Total number of unique products stored
     * @property totalQuantity Total quantity of all products
     * @property avgFrequency Average frequency of products
     * @property lastOrderDate Date string of the most recent order
     */
    data class Success(
        val totalOrders: Int,
        val downloadedCount: Int,
        val processedCount: Int,
        val unprocessedCount: Int,
        val fromCache: Boolean,
        val totalProducts: Int,
        val totalQuantity: Double,
        val avgFrequency: Double,
        val lastOrderDate: String? = null
    ) : OrderListState()
    
    /**
     * Error state - an error occurred.
     * @property message Error message to display
     */
    data class Error(val message: String) : OrderListState()
}
