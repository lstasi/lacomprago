package com.lacomprago.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lacomprago.data.api.ApiClient
import com.lacomprago.data.api.ApiException
import com.lacomprago.model.CachedOrderList
import com.lacomprago.model.OrderListState
import com.lacomprago.storage.JsonStorage
import com.lacomprago.storage.TokenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * ViewModel for managing the order list screen.
 * Handles fetching order list from cache or API, and tracking processing progress.
 */
class OrderListViewModel(
    private val apiClient: ApiClient,
    private val jsonStorage: JsonStorage,
    private val tokenStorage: TokenStorage
) : ViewModel() {
    
    private val _orderListState = MutableLiveData<OrderListState>(OrderListState.Loading)
    val orderListState: LiveData<OrderListState> = _orderListState
    
    init {
        loadOrderList(fromCache = true)
    }
    
    /**
     * Load order list from cache or API.
     * @param fromCache If true, load from cache first. If false, force refresh from API.
     */
    fun loadOrderList(fromCache: Boolean = true) {
        viewModelScope.launch {
            try {
                _orderListState.value = OrderListState.Loading
                
                val customerId = tokenStorage.getCustomerId()
                    ?: throw ApiException("Customer ID not configured. Please set up your account.", 0)
                
                // Try to load from cache first if requested
                if (fromCache) {
                    val cachedOrderList = withContext(Dispatchers.IO) {
                        jsonStorage.loadCachedOrderList()
                    }
                    
                    if (cachedOrderList != null) {
                        Log.d(TAG, "Loaded ${cachedOrderList.orders.size} orders from cache")
                        updateStateWithOrderList(cachedOrderList.orders, fromCache = true)
                        return@launch
                    } else {
                        // No cache exists, show success with 0 orders
                        Log.d(TAG, "No cached orders found")
                        updateStateWithOrderList(emptyList(), fromCache = true)
                        return@launch
                    }
                }
                
                // Fetch from API if no cache or refresh requested
                Log.d(TAG, "Fetching orders from API")
                val orderListResponse = apiClient.getOrderList(customerId)
                val orders = orderListResponse.results
                
                // Cache the response
                withContext(Dispatchers.IO) {
                    val cachedOrderList = CachedOrderList(orders = orders)
                    jsonStorage.saveCachedOrderList(cachedOrderList)
                }
                
                Log.d(TAG, "Fetched and cached ${orders.size} orders from API")
                updateStateWithOrderList(orders, fromCache = false)
                
            } catch (e: ApiException) {
                Log.e(TAG, "API error loading orders", e)
                _orderListState.value = OrderListState.Error(e.message ?: "Failed to load orders")
            } catch (e: IOException) {
                Log.e(TAG, "Network error loading orders", e)
                _orderListState.value = OrderListState.Error("Network error: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading orders", e)
                _orderListState.value = OrderListState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Refresh order list from API (bypass cache).
     */
    fun refreshOrderList() {
        loadOrderList(fromCache = false)
    }
    
    /**
     * Update state with order list data and processing status.
     */
    private suspend fun updateStateWithOrderList(orders: List<com.lacomprago.data.api.model.OrderResult>, fromCache: Boolean) {
        withContext(Dispatchers.IO) {
            val processedOrders = jsonStorage.loadProcessedOrders()
            val processedOrderIds = processedOrders.processedOrderIds.toSet()
            
            val totalOrders = orders.size
            val processedCount = orders.count { it.id.toString() in processedOrderIds }
            val unprocessedCount = totalOrders - processedCount
            
            // Calculate product statistics
            val productList = jsonStorage.loadProductList()
            val totalProducts = productList?.products?.size ?: 0
            val totalQuantity = productList?.products?.sumOf { it.totalQuantity } ?: 0.0
            val avgFrequency = if (totalProducts > 0) {
                productList?.products?.map { it.frequency }?.average() ?: 0.0
            } else {
                0.0
            }
            
            _orderListState.postValue(
                OrderListState.Success(
                    totalOrders = totalOrders,
                    processedCount = processedCount,
                    unprocessedCount = unprocessedCount,
                    fromCache = fromCache,
                    totalProducts = totalProducts,
                    totalQuantity = totalQuantity,
                    avgFrequency = avgFrequency
                )
            )
        }
    }
    
    companion object {
        private const val TAG = "OrderListViewModel"
    }
}
