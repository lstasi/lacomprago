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
                        downloadedOrders = downloadedOrders,
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
                        downloadedOrders = downloadedOrders,
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
     * Download ONE order that hasn't been downloaded yet.
     * Called when user presses "Get Orders" button.
     * Only downloads one order per button press to avoid API rate limiting.
     */
    fun downloadNextOrder() {
        viewModelScope.launch {
            try {
                _orderListState.value = OrderListState.Loading
                
                val customerId = tokenStorage.getCustomerId()
                    ?: throw ApiException("Customer ID not configured. Please set up your account.", 0)
                
                withContext(Dispatchers.IO) {
                    val cachedOrderList = jsonStorage.loadCachedOrderList()
                    if (cachedOrderList == null || cachedOrderList.orders.isEmpty()) {
                        throw ApiException("No order list available. Please fetch order list first.", 0)
                    }
                    
                    val downloadedOrders = jsonStorage.loadDownloadedOrders()
                    val downloadedOrderIds = downloadedOrders.downloadedOrderIds
                    
                    // Find first order that hasn't been downloaded
                    val orderToDownload = cachedOrderList.orders.find { 
                        it.id.toString() !in downloadedOrderIds 
                    }
                    
                    if (orderToDownload == null) {
                        // All orders already downloaded
                        Log.d(TAG, "All orders already downloaded")
                        val processedOrders = jsonStorage.loadProcessedOrders()
                        val productList = jsonStorage.loadProductList()
                        updateStateWithData(
                            orders = cachedOrderList.orders,
                            downloadedOrders = downloadedOrders,
                            processedOrders = processedOrders,
                            productList = productList,
                            fromCache = true
                        )
                        return@withContext
                    }
                    
                    // Download the order details
                    Log.d(TAG, "Downloading order ${orderToDownload.id}")
                    val orderDetails = apiClient.getOrderDetails(customerId, orderToDownload.id.toString())
                    
                    // Save to downloaded orders
                    val newDownloadedOrders = DownloadedOrders(
                        downloadedOrderIds = downloadedOrderIds + orderToDownload.id.toString(),
                        orderDetails = downloadedOrders.orderDetails + (orderToDownload.id.toString() to orderDetails),
                        lastDownloadedAt = System.currentTimeMillis()
                    )
                    jsonStorage.saveDownloadedOrders(newDownloadedOrders)
                    
                    Log.d(TAG, "Downloaded and saved order ${orderToDownload.id}")
                    
                    val processedOrders = jsonStorage.loadProcessedOrders()
                    val productList = jsonStorage.loadProductList()
                    updateStateWithData(
                        orders = cachedOrderList.orders,
                        downloadedOrders = newDownloadedOrders,
                        processedOrders = processedOrders,
                        productList = productList,
                        fromCache = true
                    )
                }
                
            } catch (e: ApiException) {
                Log.e(TAG, "API error downloading order", e)
                _orderListState.postValue(OrderListState.Error(e.message ?: "Failed to download order"))
            } catch (e: IOException) {
                Log.e(TAG, "Network error downloading order", e)
                _orderListState.postValue(OrderListState.Error("Network error: ${e.message}"))
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading order", e)
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
                
                withContext(Dispatchers.IO) {
                    val cachedOrderList = jsonStorage.loadCachedOrderList()
                    val downloadedOrders = jsonStorage.loadDownloadedOrders()
                    val processedOrders = jsonStorage.loadProcessedOrders()
                    
                    if (downloadedOrders.downloadedOrderIds.isEmpty()) {
                        throw JsonStorageException("No downloaded orders available. Please download orders first.")
                    }
                    
                    val processedOrderIds = processedOrders.processedOrderIds.toSet()
                    
                    // Find first downloaded order that hasn't been processed
                    val orderIdToProcess = downloadedOrders.downloadedOrderIds.find { 
                        it !in processedOrderIds 
                    }
                    
                    if (orderIdToProcess == null) {
                        // All downloaded orders have been processed
                        Log.d(TAG, "All downloaded orders have been processed")
                        val productList = jsonStorage.loadProductList()
                        updateStateWithData(
                            orders = cachedOrderList?.orders ?: emptyList(),
                            downloadedOrders = downloadedOrders,
                            processedOrders = processedOrders,
                            productList = productList,
                            fromCache = true
                        )
                        return@withContext
                    }
                    
                    val orderDetails = downloadedOrders.orderDetails[orderIdToProcess]
                    if (orderDetails == null) {
                        throw JsonStorageException("Order details not found for order $orderIdToProcess")
                    }
                    
                    // Find the order result to get the date
                    val orderResult = cachedOrderList?.orders?.find { it.id.toString() == orderIdToProcess }
                    val orderTimestamp = orderResult?.startDate?.let { parseOrderDate(it) } 
                        ?: System.currentTimeMillis()
                    
                    // Process the order - extract products
                    Log.d(TAG, "Processing order $orderIdToProcess")
                    val productList = jsonStorage.loadProductList() ?: ProductList(emptyList())
                    val productsMap = productList.products.associateBy { it.id }.toMutableMap()
                    
                    val items = orderDetails.lines
                    if (!items.isNullOrEmpty()) {
                        for (item in items) {
                            val product = item.product ?: continue
                            val productId = product.id
                            val productName = product.displayName ?: "Unknown Product"
                            val quantity = item.quantity ?: 1.0
                            val category = product.categories?.firstOrNull()?.name

                            val existingProduct = productsMap[productId]

                            val updatedProduct = if (existingProduct != null) {
                                existingProduct.copy(
                                    frequency = existingProduct.frequency + 1,
                                    lastPurchase = maxOf(existingProduct.lastPurchase, orderTimestamp),
                                    totalQuantity = existingProduct.totalQuantity + quantity
                                )
                            } else {
                                Product(
                                    id = productId,
                                    name = productName,
                                    frequency = 1,
                                    lastPurchase = orderTimestamp,
                                    category = category,
                                    totalQuantity = quantity
                                )
                            }

                            productsMap[productId] = updatedProduct
                        }
                    }
                    
                    // Save updated products
                    val updatedProductList = ProductList(
                        products = productsMap.values.toList(),
                        lastUpdated = System.currentTimeMillis()
                    )
                    jsonStorage.saveProductList(updatedProductList)
                    
                    // Mark order as processed
                    val updatedProcessedOrders = ProcessedOrders(
                        processedOrderIds = processedOrders.processedOrderIds + orderIdToProcess,
                        lastProcessedAt = System.currentTimeMillis()
                    )
                    jsonStorage.saveProcessedOrders(updatedProcessedOrders)
                    
                    Log.d(TAG, "Processed order $orderIdToProcess")
                    
                    updateStateWithData(
                        orders = cachedOrderList?.orders ?: emptyList(),
                        downloadedOrders = downloadedOrders,
                        processedOrders = updatedProcessedOrders,
                        productList = updatedProductList,
                        fromCache = true
                    )
                }
                
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
        downloadedOrders: DownloadedOrders,
        processedOrders: ProcessedOrders,
        productList: ProductList?,
        fromCache: Boolean
    ) {
        val totalOrders = orders.size
        val downloadedCount = downloadedOrders.downloadedOrderIds.size
        val processedCount = processedOrders.processedOrderIds.size
        val unprocessedCount = downloadedCount - processedCount
        
        // Calculate product statistics
        val totalProducts = productList?.products?.size ?: 0
        val totalQuantity = productList?.products?.sumOf { it.totalQuantity } ?: 0.0
        val avgFrequency = if (totalProducts > 0) {
            productList?.products?.map { it.frequency }?.average() ?: 0.0
        } else {
            0.0
        }
        
        // Get last order date
        val lastOrderDate = orders.maxByOrNull { parseOrderDate(it.startDate) }?.startDate?.let { 
            formatDateForDisplay(it) 
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
