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
        
        // Update statistics
        binding.totalOrdersText.text = getString(R.string.orders_total, state.totalOrders)
        binding.processedOrdersText.text = getString(R.string.orders_processed, state.processedCount)
        binding.remainingOrdersText.text = getString(R.string.orders_remaining, state.unprocessedCount)
        
        // Show cache status if loaded from cache
        if (state.fromCache) {
            binding.cacheStatusText.visibility = View.VISIBLE
        } else {
            binding.cacheStatusText.visibility = View.GONE
        }
        
        // Show/hide process button and all-processed message
        if (state.unprocessedCount > 0) {
            binding.processNextButton.visibility = View.VISIBLE
            binding.allProcessedText.visibility = View.GONE
        } else {
            binding.processNextButton.visibility = View.GONE
            binding.allProcessedText.visibility = View.VISIBLE
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
}
