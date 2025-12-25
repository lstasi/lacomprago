package com.lacomprago.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.lacomprago.R
import com.lacomprago.data.api.ApiClient
import com.lacomprago.databinding.ActivityOrderListBinding
import com.lacomprago.model.OrderListState
import com.lacomprago.storage.JsonStorage
import com.lacomprago.storage.TokenStorage
import com.lacomprago.viewmodel.OrderListViewModel

/**
 * Activity for displaying order list and processing status.
 * Shows total orders, downloaded count, processed count, and product statistics.
 * 
 * Features three separate actions:
 * - Get List Orders: Fetch order list from API
 * - Get Orders: Download one order's details
 * - Process Order: Process one downloaded order to extract products
 */
class OrderListActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOrderListBinding
    private lateinit var viewModel: OrderListViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewModel
        val tokenStorage = TokenStorage(applicationContext)
        val apiClient = ApiClient.create(tokenStorage)
        val jsonStorage = JsonStorage(applicationContext)
        viewModel = OrderListViewModel(apiClient, jsonStorage, tokenStorage)
        
        setupToolbar()
        setupListeners()
        observeOrderListState()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupListeners() {
        // Get List Orders button - fetch order list from API
        binding.getListOrdersButton.setOnClickListener {
            viewModel.fetchOrderListFromApi()
        }
        
        // Get Orders button - download one order
        binding.getOrdersButton.setOnClickListener {
            viewModel.downloadNextOrder()
        }
        
        // Process Order button - process one downloaded order
        binding.processOrderButton.setOnClickListener {
            viewModel.processNextOrder()
        }
        
        // Clear processed orders button
        binding.clearProcessedOrdersButton.setOnClickListener {
            showClearProcessedOrdersConfirmation()
        }
        
        // Clear orders button
        binding.clearOrdersButton.setOnClickListener {
            showClearOrdersConfirmation()
        }
        
        // Retry button (error state)
        binding.retryButton.setOnClickListener {
            viewModel.loadFromLocalCache()
        }
    }
    
    private fun observeOrderListState() {
        viewModel.orderListState.observe(this) { state ->
            when (state) {
                is OrderListState.Loading -> showLoading()
                is OrderListState.Success -> showSuccess(state)
                is OrderListState.Error -> showError(state.message)
                null -> showLoading()
            }
        }
    }
    
    private fun showLoading() {
        binding.loadingLayout.visibility = View.VISIBLE
        binding.contentScrollView.visibility = View.GONE
        binding.errorLayout.visibility = View.GONE
    }
    
    private fun showSuccess(state: OrderListState.Success) {
        binding.loadingLayout.visibility = View.GONE
        binding.contentScrollView.visibility = View.VISIBLE
        binding.errorLayout.visibility = View.GONE
        
        // Update order statistics
        binding.totalOrdersText.text = getString(R.string.orders_total, state.totalOrders)
        binding.downloadedOrdersText.text = getString(R.string.orders_downloaded, state.downloadedCount)
        binding.processedOrdersText.text = getString(R.string.orders_processed, state.processedCount)
        binding.remainingOrdersText.text = getString(R.string.orders_remaining, state.unprocessedCount)
        
        // Show last order date if available
        if (state.lastOrderDate != null) {
            binding.lastOrderDateText.text = getString(R.string.orders_last_date, state.lastOrderDate)
            binding.lastOrderDateText.visibility = View.VISIBLE
        } else {
            binding.lastOrderDateText.visibility = View.GONE
        }
        
        // Show cache status if loaded from cache
        if (state.fromCache) {
            if (state.totalOrders == 0) {
                binding.cacheStatusText.text = getString(R.string.orders_no_cache)
                binding.cacheStatusText.visibility = View.VISIBLE
            } else {
                binding.cacheStatusText.text = getString(R.string.orders_from_cache)
                binding.cacheStatusText.visibility = View.VISIBLE
            }
        } else {
            binding.cacheStatusText.visibility = View.GONE
        }
        
        // Update product statistics
        if (state.totalProducts > 0) {
            binding.productStatsCard.visibility = View.VISIBLE
            binding.totalProductsText.text = getString(R.string.total_products, state.totalProducts)
            binding.totalQuantityText.text = getString(R.string.total_quantity, state.totalQuantity)
            binding.avgFrequencyText.text = getString(R.string.avg_frequency, state.avgFrequency)
        } else {
            binding.productStatsCard.visibility = View.GONE
        }
        
        // Enable/disable buttons based on state
        val hasOrderList = state.totalOrders > 0
        val hasUndownloadedOrders = state.totalOrders > state.downloadedCount
        val hasUnprocessedOrders = state.unprocessedCount > 0
        
        // Get Orders button enabled only if there are orders to download
        binding.getOrdersButton.isEnabled = hasUndownloadedOrders
        if (hasUndownloadedOrders) {
            binding.getOrdersButton.text = getString(R.string.orders_get_orders)
        } else if (hasOrderList) {
            binding.getOrdersButton.text = "All Orders Downloaded"
        } else {
            binding.getOrdersButton.text = getString(R.string.orders_get_orders)
        }
        
        // Process Order button enabled only if there are downloaded but unprocessed orders
        binding.processOrderButton.isEnabled = hasUnprocessedOrders
        if (hasUnprocessedOrders) {
            binding.processOrderButton.text = getString(R.string.orders_process_order)
        } else if (state.downloadedCount > 0) {
            binding.processOrderButton.text = "All Orders Processed"
        } else {
            binding.processOrderButton.text = getString(R.string.orders_process_order)
        }
    }
    
    private fun showError(message: String) {
        binding.loadingLayout.visibility = View.GONE
        binding.contentScrollView.visibility = View.GONE
        binding.errorLayout.visibility = View.VISIBLE
        binding.errorText.text = message
    }
    
    private fun showClearOrdersConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.orders_clear_confirm_title)
            .setMessage(R.string.orders_clear_confirm_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                clearAllData()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    private fun showClearProcessedOrdersConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.orders_clear_processed_confirm_title)
            .setMessage(R.string.orders_clear_processed_confirm_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                clearProcessedOrders()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    private fun clearAllData() {
        val jsonStorage = JsonStorage(applicationContext)
        jsonStorage.deleteCachedOrderList()
        jsonStorage.deleteDownloadedOrders()
        jsonStorage.deleteProcessedOrders()
        jsonStorage.deleteProductList()
        
        // Show toast
        Toast.makeText(
            this,
            R.string.orders_cleared,
            Toast.LENGTH_SHORT
        ).show()
        
        // Reload from cache to show empty state
        viewModel.loadFromLocalCache()
    }
    
    private fun clearProcessedOrders() {
        val jsonStorage = JsonStorage(applicationContext)
        jsonStorage.deleteProcessedOrders()
        
        // Show toast
        Toast.makeText(
            this,
            R.string.orders_processed_cleared,
            Toast.LENGTH_SHORT
        ).show()
        
        // Reload from cache to reflect the change
        viewModel.loadFromLocalCache()
    }
}
