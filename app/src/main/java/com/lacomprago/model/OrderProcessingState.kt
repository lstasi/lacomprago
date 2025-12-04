package com.lacomprago.model

/**
 * Sealed class representing the possible states of order processing.
 * Used for state management in OrderProcessingViewModel.
 * 
 * Note: To avoid API rate limiting, only ONE order is processed per sync.
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
     * Processing state - actively processing a single order.
     * @property currentOrderId ID of the order being processed
     * @property remainingOrders Number of orders still to be processed (for info display)
     */
    data class Processing(
        val currentOrderId: String,
        val remainingOrders: Int
    ) : OrderProcessingState()
    
    /**
     * Completed state - order processed successfully.
     * @property updatedProductCount Number of products that were updated
     * @property remainingOrders Number of orders still to be processed
     */
    data class Completed(
        val updatedProductCount: Int = 0,
        val remainingOrders: Int = 0
    ) : OrderProcessingState()
    
    /**
     * Cancelled state - processing was cancelled by user.
     */
    object Cancelled : OrderProcessingState()
    
    /**
     * Error state - an error occurred during processing.
     * @property message Error message to display
     */
    data class Error(val message: String) : OrderProcessingState()
}
