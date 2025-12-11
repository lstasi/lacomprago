package com.lacomprago.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lacomprago.data.api.ApiClient
import com.lacomprago.data.api.ApiException
import com.lacomprago.data.api.model.OrderDetailsResponse
import com.lacomprago.data.api.model.OrderPreparedLinesResponse
import com.lacomprago.data.api.model.OrderResult
import com.lacomprago.model.CachedOrderList
import com.lacomprago.model.OrderProcessingState
import com.lacomprago.model.ProcessedOrders
import com.lacomprago.model.Product
import com.lacomprago.model.ProductList
import com.lacomprago.storage.JsonStorage
import com.lacomprago.storage.JsonStorageException
import com.lacomprago.storage.TokenStorage
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
    private val jsonStorage: JsonStorage,
    private val tokenStorage: TokenStorage
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
        // Step 1: Get customer ID
        val customerId = tokenStorage.getCustomerId()
            ?: throw ApiException("Customer ID not configured. Please set up your account.", 0)

        // Step 2: Set state to fetching orders
        _processingState.value = OrderProcessingState.FetchingOrders
        
        // Step 3: Try to get orders from cache first, then API if needed
        val orderList = withContext(Dispatchers.IO) {
            val cachedOrderList = jsonStorage.loadCachedOrderList()
            if (cachedOrderList != null) {
                Log.d(TAG, "Using cached order list with ${cachedOrderList.orders.size} orders")
                cachedOrderList.orders
            } else {
                Log.d(TAG, "No cached orders, fetching from API")
                val orderListResponse = apiClient.getOrderList(customerId)
                val orders = orderListResponse.results
                
                // Cache the response for future use
                val newCachedOrderList = CachedOrderList(orders = orders)
                jsonStorage.saveCachedOrderList(newCachedOrderList)
                
                Log.d(TAG, "Fetched and cached ${orders.size} orders from API")
                orders
            }
        }
        
        Log.d(TAG, "Processing with ${orderList.size} total orders")
        
        // Step 4: Load processed orders
        val processedOrders = withContext(Dispatchers.IO) {
            jsonStorage.loadProcessedOrders()
        }
        val processedOrderIds = processedOrders.processedOrderIds.toSet()
        Log.d(TAG, "Already processed ${processedOrderIds.size} orders")
        
        // Step 5: Filter out already processed orders
        val unprocessedOrders = orderList.filter { it.id.toString() !in processedOrderIds }
        Log.d(TAG, "Found ${unprocessedOrders.size} unprocessed orders")
        
        if (unprocessedOrders.isEmpty()) {
            _processingState.value = OrderProcessingState.Completed(
                productsBefore = 0,
                productsFound = 0,
                productsAdded = 0,
                remainingOrders = 0
            )
            return
        }
        
        // Step 6: Process only the FIRST unprocessed order
        val orderToProcess = unprocessedOrders.first()
        val remainingAfterThis = unprocessedOrders.size - 1
        
        // Update progress
        _processingState.value = OrderProcessingState.Processing(
            currentOrderId = orderToProcess.id.toString(),
            remainingOrders = remainingAfterThis
        )
        
        // Get warehouse code from order
        val warehouseCode = orderToProcess.warehouseCode ?: "bcn1" // Default to bcn1 if not available
        
        // Fetch prepared order lines (new endpoint)
        val preparedLines = apiClient.getOrderPreparedLines(
            customerId = customerId,
            orderId = orderToProcess.id.toString(),
            warehouseCode = warehouseCode
        )

        // Update products from order and get statistics
        val stats = updateProductsFromPreparedLines(preparedLines, orderToProcess)
        
        // Mark order as processed
        markOrderAsProcessed(orderToProcess.id.toString())
        
        Log.d(TAG, "Processed order ${orderToProcess.id}: ${stats.productsBefore} products before, ${stats.productsFound} found in order, ${stats.productsAdded} added. $remainingAfterThis remaining")
        
        // Step 7: Complete
        _processingState.value = OrderProcessingState.Completed(
            productsBefore = stats.productsBefore,
            productsFound = stats.productsFound,
            productsAdded = stats.productsAdded,
            remainingOrders = remainingAfterThis
        )
    }
    
    /**
     * Update products from prepared order lines.
     * For each item in the order, increment the product frequency
     * and update the last purchase date.
     *
     * @param preparedLines The prepared order lines response with product items
     * @param orderResult The order result with metadata (dates, etc.)
     * @return Statistics about the processing (products before, found, added)
     */
    private suspend fun updateProductsFromPreparedLines(
        preparedLines: OrderPreparedLinesResponse,
        orderResult: OrderResult
    ): ProcessingStatistics = withContext(Dispatchers.IO) {
        // Load current products
        val productList = jsonStorage.loadProductList() ?: ProductList(emptyList())
        val productsMap = productList.products.associateBy { it.id }.toMutableMap()
        
        // Count products before processing
        val productsBefore = productsMap.size
        
        // Parse order date from OrderResult (use startDate or endDate)
        val orderTimestamp = parseOrderDate(orderResult.startDate)
        
        // Get lines from prepared order response
        val items = preparedLines.results
        if (items.isEmpty()) {
            Log.w(TAG, "Order ${orderResult.id} has no items")
            return@withContext ProcessingStatistics(
                productsBefore = productsBefore,
                productsFound = 0,
                productsAdded = 0
            )
        }
        
        // Count products found in order
        val productsFound = items.size
        var productsAdded = 0
        
        // Update each product from the order
        for (item in items) {
            val product = item.product ?: continue
            val productId = item.productId
            val productName = product.displayName ?: "Unknown Product"
            val quantity = item.preparedQuantity ?: item.orderedQuantity ?: 1.0
            val category = product.categories?.firstOrNull()?.name

            val existingProduct = productsMap[productId]

            if (existingProduct == null) {
                // New product - increment counter
                productsAdded++
            }

            val updatedProduct = if (existingProduct != null) {
                // Update existing product
                existingProduct.copy(
                    frequency = existingProduct.frequency + 1,
                    lastPurchase = maxOf(existingProduct.lastPurchase, orderTimestamp),
                    totalQuantity = existingProduct.totalQuantity + quantity
                )
            } else {
                // Create new product entry
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
        
        // Save updated products
        val updatedProductList = ProductList(
            products = productsMap.values.toList(),
            lastUpdated = System.currentTimeMillis()
        )
        jsonStorage.saveProductList(updatedProductList)
        
        ProcessingStatistics(
            productsBefore = productsBefore,
            productsFound = productsFound,
            productsAdded = productsAdded
        )
    }

    /**
     * Update products from an order.
     * For each item in the order, increment the product frequency
     * and update the last purchase date.
     *
     * @param orderDetails The order details with product items
     * @param orderResult The order result with metadata (dates, etc.)
     * @return The number of products updated
     */
    private suspend fun updateProductsFromOrder(
        orderDetails: OrderDetailsResponse,
        orderResult: OrderResult
    ): Int = withContext(Dispatchers.IO) {
        // Load current products
        val productList = jsonStorage.loadProductList() ?: ProductList(emptyList())
        val productsMap = productList.products.associateBy { it.id }.toMutableMap()
        
        // Parse order date from OrderResult (use startDate or endDate)
        val orderTimestamp = parseOrderDate(orderResult.startDate)
        
        // Get lines from order details (Mercadona order structure)
        val items = orderDetails.lines
        if (items.isNullOrEmpty()) {
            Log.w(TAG, "Order ${orderResult.id} has no items")
            return@withContext 0
        }
        
        var updatedCount = 0
        
        // Update each product from the order
        for (item in items) {
            val product = item.product ?: continue
            val productId = product.id
            val productName = product.displayName ?: "Unknown Product"
            val quantity = item.quantity ?: 1.0
            val category = product.categories?.firstOrNull()?.name

            val existingProduct = productsMap[productId]

            val updatedProduct = if (existingProduct != null) {
                // Update existing product
                existingProduct.copy(
                    frequency = existingProduct.frequency + 1,
                    lastPurchase = maxOf(existingProduct.lastPurchase, orderTimestamp),
                    totalQuantity = existingProduct.totalQuantity + quantity
                )
            } else {
                // Create new product entry
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

/**
 * Statistics from processing an order.
 *
 * @property productsBefore Number of products in the list before processing
 * @property productsFound Number of products found in the order
 * @property productsAdded Number of new products added to the list
 */
private data class ProcessingStatistics(
    val productsBefore: Int,
    val productsFound: Int,
    val productsAdded: Int
)
