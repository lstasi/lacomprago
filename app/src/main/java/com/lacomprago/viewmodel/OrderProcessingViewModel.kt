package com.lacomprago.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lacomprago.data.api.ApiClient
import com.lacomprago.data.api.ApiException
import com.lacomprago.data.api.model.OrderResponse
import com.lacomprago.model.OrderProcessingState
import com.lacomprago.model.ProcessedOrders
import com.lacomprago.model.Product
import com.lacomprago.model.ProductList
import com.lacomprago.storage.JsonStorage
import com.lacomprago.storage.JsonStorageException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * ViewModel for managing order processing.
 * 
 * To avoid API rate limiting/banning, this ViewModel processes only ONE order
 * per sync operation. Users need to press sync multiple times to process
 * all pending orders.
 */
class OrderProcessingViewModel(
    private val apiClient: ApiClient,
    private val jsonStorage: JsonStorage
) : ViewModel() {
    
    private val _processingState = MutableLiveData<OrderProcessingState>(OrderProcessingState.Idle)
    val processingState: LiveData<OrderProcessingState> = _processingState
    
    private var processingJob: Job? = null
    
    /**
     * Start processing a single order.
     * Fetches orders from API, filters out already processed ones,
     * and processes only the FIRST unprocessed order to avoid API rate limiting.
     */
    fun startProcessing() {
        if (processingJob?.isActive == true) {
            Log.d(TAG, "Processing already in progress")
            return
        }
        
        processingJob = viewModelScope.launch {
            try {
                processSingleOrder()
            } catch (e: CancellationException) {
                Log.d(TAG, "Processing cancelled")
                _processingState.value = OrderProcessingState.Cancelled
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error during order processing", e)
                val errorMessage = when (e) {
                    is IOException -> "Network error. Please check your connection."
                    is ApiException -> e.message ?: "API error occurred"
                    is JsonStorageException -> "Error saving data: ${e.message}"
                    else -> "An unexpected error occurred"
                }
                _processingState.value = OrderProcessingState.Error(errorMessage)
            }
        }
    }
    
    /**
     * Cancel the current processing operation.
     */
    fun cancelProcessing() {
        processingJob?.cancel()
    }
    
    /**
     * Reset the state to idle.
     */
    fun resetState() {
        _processingState.value = OrderProcessingState.Idle
    }
    
    /**
     * Process a single order to avoid API rate limiting.
     * Only the first unprocessed order is fetched and processed.
     */
    private suspend fun processSingleOrder() {
        // Step 1: Set state to fetching orders
        _processingState.value = OrderProcessingState.FetchingOrders
        
        // Step 2: Fetch order list from API
        val orderList = apiClient.getOrderList()
        Log.d(TAG, "Fetched ${orderList.size} orders from API")
        
        // Step 3: Load processed orders
        val processedOrders = withContext(Dispatchers.IO) {
            jsonStorage.loadProcessedOrders()
        }
        val processedOrderIds = processedOrders.processedOrderIds.toSet()
        Log.d(TAG, "Already processed ${processedOrderIds.size} orders")
        
        // Step 4: Filter out already processed orders
        val unprocessedOrders = orderList.filter { it.id !in processedOrderIds }
        Log.d(TAG, "Found ${unprocessedOrders.size} unprocessed orders")
        
        if (unprocessedOrders.isEmpty()) {
            _processingState.value = OrderProcessingState.Completed(
                updatedProductCount = 0,
                remainingOrders = 0
            )
            return
        }
        
        // Step 5: Process only the FIRST unprocessed order
        val orderToProcess = unprocessedOrders.first()
        val remainingAfterThis = unprocessedOrders.size - 1
        
        // Update progress
        _processingState.value = OrderProcessingState.Processing(
            currentOrderId = orderToProcess.id,
            remainingOrders = remainingAfterThis
        )
        
        // Fetch order details
        val orderDetails = apiClient.getOrderDetails(orderToProcess.id)
        
        // Update products from order
        val updatedCount = updateProductsFromOrder(orderDetails)
        
        // Mark order as processed
        markOrderAsProcessed(orderToProcess.id)
        
        Log.d(TAG, "Processed order ${orderToProcess.id}, updated $updatedCount products, $remainingAfterThis remaining")
        
        // Step 6: Complete
        _processingState.value = OrderProcessingState.Completed(
            updatedProductCount = updatedCount,
            remainingOrders = remainingAfterThis
        )
    }
    
    /**
     * Update products from an order.
     * For each item in the order, increment the product frequency
     * and update the last purchase date.
     *
     * @param order The order to process
     * @return The number of products updated
     */
    private suspend fun updateProductsFromOrder(order: OrderResponse): Int = withContext(Dispatchers.IO) {
        // Load current products
        val productList = jsonStorage.loadProductList() ?: ProductList(emptyList())
        val productsMap = productList.products.associateBy { it.id }.toMutableMap()
        
        // Parse order date
        val orderTimestamp = parseOrderDate(order.orderDate)
        
        var updatedCount = 0
        
        // Update each product from the order
        for (item in order.items) {
            val existingProduct = productsMap[item.productId]
            
            val updatedProduct = if (existingProduct != null) {
                // Update existing product
                existingProduct.copy(
                    frequency = existingProduct.frequency + 1,
                    lastPurchase = maxOf(existingProduct.lastPurchase, orderTimestamp),
                    totalQuantity = existingProduct.totalQuantity + item.quantity
                )
            } else {
                // Create new product entry
                Product(
                    id = item.productId,
                    name = item.productName,
                    frequency = 1,
                    lastPurchase = orderTimestamp,
                    category = item.category,
                    totalQuantity = item.quantity.toDouble()
                )
            }
            
            productsMap[item.productId] = updatedProduct
            updatedCount++
        }
        
        // Save updated products
        val updatedProductList = ProductList(
            products = productsMap.values.toList(),
            lastUpdated = System.currentTimeMillis()
        )
        jsonStorage.saveProductList(updatedProductList)
        
        updatedCount
    }
    
    /**
     * Mark an order as processed.
     * Adds the order ID to the processed orders list and saves it.
     *
     * @param orderId The order ID to mark as processed
     */
    private suspend fun markOrderAsProcessed(orderId: String) = withContext(Dispatchers.IO) {
        val processedOrders = jsonStorage.loadProcessedOrders()
        val updatedProcessedOrders = ProcessedOrders(
            processedOrderIds = processedOrders.processedOrderIds + orderId,
            lastProcessedAt = System.currentTimeMillis()
        )
        jsonStorage.saveProcessedOrders(updatedProcessedOrders)
    }
    
    /**
     * Parse ISO 8601 date string to timestamp.
     *
     * @param dateString ISO 8601 formatted date string
     * @return Timestamp in milliseconds
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
    
    companion object {
        private const val TAG = "OrderProcessingVM"
    }
}
