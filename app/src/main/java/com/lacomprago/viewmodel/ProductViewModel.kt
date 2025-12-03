package com.lacomprago.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lacomprago.model.Product
import com.lacomprago.model.ProductList
import com.lacomprago.model.ProductListState
import com.lacomprago.storage.JsonStorage
import com.lacomprago.storage.JsonStorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for managing the product list screen.
 * Handles loading, sorting, and displaying products.
 */
class ProductViewModel(
    private val jsonStorage: JsonStorage
) : ViewModel() {
    
    private val _productListState = MutableLiveData<ProductListState>()
    val productListState: LiveData<ProductListState> = _productListState
    
    private val _productCount = MutableLiveData<Int>()
    val productCount: LiveData<Int> = _productCount
    
    init {
        loadProducts()
    }
    
    /**
     * Load products from JSON storage.
     * Products are sorted by frequency (highest first) by default.
     */
    fun loadProducts() {
        _productListState.value = ProductListState.Loading
        
        viewModelScope.launch {
            try {
                val productList = withContext(Dispatchers.IO) {
                    jsonStorage.loadProductList()
                }
                
                if (productList == null || productList.products.isEmpty()) {
                    _productListState.value = ProductListState.Empty
                    _productCount.value = 0
                } else {
                    // Sort products by frequency (highest first)
                    val sortedProducts = productList.products
                        .sortedByDescending { it.frequency }
                    
                    _productListState.value = ProductListState.Success(sortedProducts)
                    _productCount.value = sortedProducts.size
                }
            } catch (e: JsonStorageException) {
                Log.e(TAG, "Error loading products", e)
                _productListState.value = ProductListState.Error(
                    e.message ?: "Failed to load products"
                )
                _productCount.value = 0
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading products", e)
                _productListState.value = ProductListState.Error(
                    "An unexpected error occurred"
                )
                _productCount.value = 0
            }
        }
    }
    
    /**
     * Refresh products by reloading from storage.
     * This is called when the user taps the refresh button.
     */
    fun refreshProducts() {
        loadProducts()
    }
    
    companion object {
        private const val TAG = "ProductViewModel"
    }
}
