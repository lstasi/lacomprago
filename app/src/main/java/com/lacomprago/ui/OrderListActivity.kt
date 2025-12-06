package com.lacomprago.ui

import android.os.Bundle
import android.view.View
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
 * Shows total orders, processed count, and remaining count.
 * Allows refreshing from API or processing next order.
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
        // Refresh button - fetch from API
        binding.refreshButton.setOnClickListener {
            viewModel.refreshOrderList()
        }
        
        // Process next order button
        binding.processNextButton.setOnClickListener {
            showOrderProcessingDialog()
        }
        
        // Clear orders button
        binding.clearOrdersButton.setOnClickListener {
            showClearOrdersConfirmation()
        }
        
        // Retry button (error state)
        binding.retryButton.setOnClickListener {
            viewModel.loadOrderList(fromCache = true)
        }
    }
    
    private fun observeOrderListState() {
        viewModel.orderListState.observe(this) { state ->
            when (state) {
                is OrderListState.Loading -> showLoading()
                is OrderListState.Success -> showSuccess(state)
                is OrderListState.Error -> showError(state.message)
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
        binding.processedOrdersText.text = getString(R.string.orders_processed, state.processedCount)
        binding.remainingOrdersText.text = getString(R.string.orders_remaining, state.unprocessedCount)
        
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
        
        // Update button text based on whether there are unprocessed orders
        if (state.unprocessedCount > 0) {
            binding.processNextButton.text = getString(R.string.orders_process_next)
        } else {
            binding.processNextButton.text = getString(R.string.orders_reprocess)
        }
    }
    
    private fun showError(message: String) {
        binding.loadingLayout.visibility = View.GONE
        binding.contentScrollView.visibility = View.GONE
        binding.errorLayout.visibility = View.VISIBLE
        binding.errorText.text = message
    }
    
    private fun showOrderProcessingDialog() {
        val dialog = OrderProcessingDialogFragment.newInstance()
        dialog.setOnProcessingCompleteListener {
            // Reload order list from cache to update counts
            viewModel.loadOrderList(fromCache = true)
        }
        dialog.show(supportFragmentManager, OrderProcessingDialogFragment.TAG)
    }
    
    private fun showClearOrdersConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.orders_clear_confirm_title)
            .setMessage(R.string.orders_clear_confirm_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                clearOrders()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    private fun clearOrders() {
        val jsonStorage = com.lacomprago.storage.JsonStorage(applicationContext)
        jsonStorage.deleteCachedOrderList()
        
        // Show toast
        android.widget.Toast.makeText(
            this,
            R.string.orders_cleared,
            android.widget.Toast.LENGTH_SHORT
        ).show()
        
        // Reload from cache to show error state (no cache)
        viewModel.loadOrderList(fromCache = true)
    }
}
