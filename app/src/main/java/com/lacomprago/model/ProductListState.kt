package com.lacomprago.model

/**
 * Sealed class representing the possible states of the product list UI.
 * Used for state management in ProductViewModel.
 */
sealed class ProductListState {
    /**
     * Loading state - shown while loading products from storage.
     */
    object Loading : ProductListState()
    
    /**
     * Success state - products loaded successfully.
     * @property products List of products sorted by frequency
     */
    data class Success(val products: List<Product>) : ProductListState()
    
    /**
     * Empty state - no products available.
     */
    object Empty : ProductListState()
    
    /**
     * Error state - an error occurred while loading products.
     * @property message Error message to display
     */
    data class Error(val message: String) : ProductListState()
}
