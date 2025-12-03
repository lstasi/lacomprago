package com.lacomprago.model

/**
 * Sealed class representing the possible states of order processing.
 * Used for state management in OrderProcessingViewModel.
 */
sealed class OrderProcessingState {
    /**
     * Idle state - no processing in progress.
     */
    object Idle : OrderProcessingState()
    
    /**
     * Fetching state - loading order list from API.
     */
    object FetchingOrders : OrderProcessingState()
    
    /**
     * Processing state - actively processing orders.
     * @property currentOrder Current order number being processed (1-based)
     * @property totalOrders Total number of orders to process
     * @property currentOrderId ID of the current order being processed
     */
    data class Processing(
        val currentOrder: Int,
        val totalOrders: Int,
        val currentOrderId: String
    ) : OrderProcessingState()
    
    /**
     * Completed state - all orders processed successfully.
     * @property processedCount Number of orders that were processed
     * @property updatedProductCount Number of products that were updated
     */
    data class Completed(
        val processedCount: Int,
        val updatedProductCount: Int = 0
    ) : OrderProcessingState()
    
    /**
     * Cancelled state - processing was cancelled by user.
     * @property processedCount Number of orders processed before cancellation
     */
    data class Cancelled(val processedCount: Int) : OrderProcessingState()
    
    /**
     * Error state - an error occurred during processing.
     * @property message Error message to display
     * @property processedCount Number of orders processed before the error
     */
    data class Error(
        val message: String,
        val processedCount: Int
    ) : OrderProcessingState()
}
