package com.lacomprago.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lacomprago.data.api.ApiClient
import com.lacomprago.data.api.ApiException
import com.lacomprago.data.api.model.OrderResult
import com.lacomprago.model.CachedOrderList
import com.lacomprago.model.DownloadedOrders
import com.lacomprago.model.OrderListState
import com.lacomprago.model.ProcessedOrders
import com.lacomprago.model.Product
import com.lacomprago.model.ProductList
import com.lacomprago.storage.JsonStorage
import com.lacomprago.storage.JsonStorageException
import com.lacomprago.storage.TokenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * ViewModel for managing the order list screen.
 * Handles fetching order list from cache or API, downloading order details,
 * and processing orders to extract products.
 * 
 * The screen does NOT auto-load from API on init - users must press buttons to trigger actions.
 */
class OrderListViewModel(
    private val apiClient: ApiClient,
    private val jsonStorage: JsonStorage,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _orderListState = MutableLiveData<OrderListState>()
    val orderListState: LiveData<OrderListState> = _orderListState

    private val _processingResult = MutableLiveData<Event<String>>()
    val processingResult: LiveData<Event<String>> = _processingResult

    init {
        // Load from local cache only - do NOT call API on page load
        loadFromLocalCache()
    }
    
    /**
     * Load current status from local cache only.
     * Does NOT call any API - just reads from local storage.
     */
    fun loadFromLocalCache() {
        viewModelScope.launch {
            try {
                _orderListState.value = OrderListState.Loading
                
                withContext(Dispatchers.IO) {
                    val cachedOrderList = jsonStorage.loadCachedOrderList()
                    val downloadedOrders = jsonStorage.loadDownloadedOrders()
                    val processedOrders = jsonStorage.loadProcessedOrders()
                    val productList = jsonStorage.loadProductList()
                    
                    updateStateWithData(
                        orders = cachedOrderList?.orders ?: emptyList(),
                        processedOrders = processedOrders,
                        productList = productList,
                        fromCache = true
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading from cache", e)
                _orderListState.postValue(OrderListState.Error(e.message ?: "Error loading data"))
            }
        }
    }
    
    /**
     * Fetch order list from API.
     * Called when user presses "Get List Orders" button.
     */
    fun fetchOrderListFromApi() {
        viewModelScope.launch {
            try {
                _orderListState.value = OrderListState.Loading
                
                val customerId = tokenStorage.getCustomerId()
                    ?: throw ApiException("Customer ID not configured. Please set up your account.", 0)
                
                Log.d(TAG, "Fetching orders from API")
                val orderListResponse = apiClient.getOrderList(customerId)
                val orders = orderListResponse.results
                
                // Cache the response
                withContext(Dispatchers.IO) {
                    val cachedOrderList = CachedOrderList(orders = orders)
                    jsonStorage.saveCachedOrderList(cachedOrderList)
                    
                    val downloadedOrders = jsonStorage.loadDownloadedOrders()
                    val processedOrders = jsonStorage.loadProcessedOrders()
                    val productList = jsonStorage.loadProductList()
                    
                    updateStateWithData(
                        orders = orders,
                        processedOrders = processedOrders,
                        productList = productList,
                        fromCache = false
                    )
                }
                
                Log.d(TAG, "Fetched and cached ${orders.size} orders from API")
                
            } catch (e: ApiException) {
                Log.e(TAG, "API error loading orders", e)
                _orderListState.postValue(OrderListState.Error(e.message ?: "Failed to load orders"))
            } catch (e: IOException) {
                Log.e(TAG, "Network error loading orders", e)
                _orderListState.postValue(OrderListState.Error("Network error: ${e.message}"))
            } catch (e: Exception) {
                Log.e(TAG, "Error loading orders", e)
                _orderListState.postValue(OrderListState.Error(e.message ?: "Unknown error"))
            }
        }
    }
    
    /**
     * Process ONE downloaded order that hasn't been processed yet.
     * Called when user presses "Process Order" button.
     * Extracts products from the order and updates the product list.
     */
    fun processNextOrder() {
        viewModelScope.launch {
            try {
                _orderListState.value = OrderListState.Loading

                val customerId = tokenStorage.getCustomerId()
                    ?: throw ApiException("Customer ID not configured. Please set up your account.", 0)

                withContext(Dispatchers.IO) {
                    val cachedOrderList = jsonStorage.loadCachedOrderList()
                    val processedOrders = jsonStorage.loadProcessedOrders()

                    if (cachedOrderList == null || cachedOrderList.orders.isEmpty()) {
                        throw JsonStorageException("No orders available. Please fetch the order list first.")
                    }

                    val processedOrderIds = processedOrders.processedOrderIds.toSet()
                    val orderToProcess = cachedOrderList.orders.find { it.id.toString() !in processedOrderIds }

                    if (orderToProcess == null) {
                        _processingResult.postValue(Event("All orders have been processed."))
                        val productList = jsonStorage.loadProductList()
                        updateStateWithData(
                            orders = cachedOrderList.orders,
                            processedOrders = processedOrders,
                            productList = productList,
                            fromCache = true
                        )
                        return@withContext
                    }

                    val orderIdToProcess = orderToProcess.id.toString()
                    Log.d(TAG, "Processing order $orderIdToProcess")

                    val orderLinesResponse = apiClient.getOrderLines(customerId, orderIdToProcess)
                    val items = orderLinesResponse.results

                    if (items.isNullOrEmpty()) {
                        Log.w(TAG, "Order $orderIdToProcess has no product lines. Skipping.")
                        val updatedProcessedOrders = processedOrders.copy(
                            processedOrderIds = processedOrders.processedOrderIds + orderIdToProcess
                        )
                        jsonStorage.saveProcessedOrders(updatedProcessedOrders)
                        val productList = jsonStorage.loadProductList()
                        updateStateWithData(
                            orders = cachedOrderList.orders,
                            processedOrders = updatedProcessedOrders,
                            productList = productList,
                            fromCache = true
                        )
                        _processingResult.postValue(Event("Order contained no products. Marked as processed."))
                        return@withContext
                    }

                    val orderTimestamp = parseOrderDate(orderToProcess.startDate)
                    val productList = jsonStorage.loadProductList() ?: ProductList(emptyList())
                    val productsMap = productList.products.associateBy { it.id }.toMutableMap()
                    val productsBefore = productsMap.size
                    var newProductsAdded = 0

                    for (item in items) {
                        val product = item.product ?: continue
                        val productId = product.id
                        val productName = product.displayName ?: "Unknown Product"
                        val quantity = item.orderedQuantity
                        val category = product.categories?.firstOrNull()?.name
                        val existingProduct = productsMap[productId]

                        if (existingProduct == null) {
                            newProductsAdded++
                        }

                        val updatedProduct = existingProduct?.copy(
                            frequency = existingProduct.frequency + 1,
                            lastPurchase = maxOf(existingProduct.lastPurchase, orderTimestamp),
                            totalQuantity = existingProduct.totalQuantity + quantity
                        ) ?: Product(
                            id = productId,
                            name = productName,
                            frequency = 1,
                            lastPurchase = orderTimestamp,
                            category = category,
                            totalQuantity = quantity
                        )
                        productsMap[productId] = updatedProduct
                    }

                    val updatedProductList = ProductList(
                        products = productsMap.values.toList(),
                        lastUpdated = System.currentTimeMillis()
                    )
                    jsonStorage.saveProductList(updatedProductList)

                    val updatedProcessedOrders = ProcessedOrders(
                        processedOrderIds = processedOrders.processedOrderIds + orderIdToProcess,
                        lastProcessedAt = System.currentTimeMillis()
                    )
                    jsonStorage.saveProcessedOrders(updatedProcessedOrders)

                    val summary = "Processed order. Products before: $productsBefore, Found in order: ${items.size}, New products added: $newProductsAdded."
                    _processingResult.postValue(Event(summary))
                    Log.d(TAG, summary)

                    updateStateWithData(
                        orders = cachedOrderList.orders,
                        processedOrders = updatedProcessedOrders,
                        productList = updatedProductList,
                        fromCache = true
                    )
                }

            } catch (e: ApiException) {
                Log.e(TAG, "API error processing order", e)
                _orderListState.postValue(OrderListState.Error(e.message ?: "Failed to process order"))
            } catch (e: JsonStorageException) {
                Log.e(TAG, "Storage error processing order", e)
                _orderListState.postValue(OrderListState.Error(e.message ?: "Error processing order"))
            } catch (e: Exception) {
                Log.e(TAG, "Error processing order", e)
                _orderListState.postValue(OrderListState.Error(e.message ?: "Unknown error"))
            }
        }
    }
    
    /**
     * Update state with current data.
     */
    private fun updateStateWithData(
        orders: List<OrderResult>,
        processedOrders: ProcessedOrders,
        productList: ProductList?,
        fromCache: Boolean
    ) {
        val totalOrders = orders.size
        val downloadedCount = processedOrders.processedOrderIds.size // Simplified for consistency
        val processedCount = processedOrders.processedOrderIds.size
        val unprocessedCount = totalOrders - processedCount
        
        // Calculate product statistics
        val totalProducts = productList?.products?.size ?: 0
        val totalQuantity = productList?.products?.sumOf { it.totalQuantity } ?: 0.0
        val avgFrequency = if (totalProducts > 0) {
            productList?.products?.map { it.frequency }?.average() ?: 0.0
        } else {
            0.0
        }
        
        // Get last order date
        val lastOrderDate = orders.maxByOrNull { parseOrderDate(it.startDate) }?.let { 
            formatDateForDisplay(it.startDate) 
        }
        
        _orderListState.postValue(
            OrderListState.Success(
                totalOrders = totalOrders,
                downloadedCount = downloadedCount,
                processedCount = processedCount,
                unprocessedCount = unprocessedCount,
                fromCache = fromCache,
                totalProducts = totalProducts,
                totalQuantity = totalQuantity,
                avgFrequency = avgFrequency,
                lastOrderDate = lastOrderDate
            )
        )
    }
    
    /**
     * Parse ISO 8601 date string to timestamp.
     */
    private fun parseOrderDate(dateString: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse date: $dateString", e)
            System.currentTimeMillis()
        }
    }
    
    /**
     * Format date for display.
     */
    private fun formatDateForDisplay(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)
            
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            outputFormat.format(date ?: return dateString)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to format date: $dateString", e)
            dateString
        }
    }
    
    companion object {
        private const val TAG = "OrderListViewModel"
    }
}

/**
 * Used as a wrapper for data that represents an event.
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}
