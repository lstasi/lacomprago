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
     * @property processedCount Number of orders already processed
     * @property unprocessedCount Number of orders remaining to process
     * @property fromCache Whether the data was loaded from cache
     */
    data class Success(
        val totalOrders: Int,
        val processedCount: Int,
        val unprocessedCount: Int,
        val fromCache: Boolean
    ) : OrderListState()
    
    /**
     * Error state - an error occurred.
     * @property message Error message to display
     */
    data class Error(val message: String) : OrderListState()
}
