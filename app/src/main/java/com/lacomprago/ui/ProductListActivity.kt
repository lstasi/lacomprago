package com.lacomprago.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.lacomprago.R
import com.lacomprago.databinding.ActivityProductListBinding
import com.lacomprago.model.Product
import com.lacomprago.model.ProductListState
import com.lacomprago.storage.JsonStorage
import com.lacomprago.viewmodel.ProductViewModel

/**
 * Activity for displaying the product list.
 * Shows products sorted by frequency with refresh capability.
 */
class ProductListActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProductListBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var adapter: ProductAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewModel with JsonStorage
        val jsonStorage = JsonStorage(applicationContext)
        viewModel = ProductViewModel(jsonStorage)
        
        setupRecyclerView()
        setupListeners()
        observeProductListState()
        observeProductCount()
    }
    
    private fun setupRecyclerView() {
        adapter = ProductAdapter()
        binding.productRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProductListActivity)
            adapter = this@ProductListActivity.adapter
        }
    }
    
    private fun setupListeners() {
        // Refresh FAB click
        binding.refreshFab.setOnClickListener {
            viewModel.refreshProducts()
        }
        
        // Retry button click (in error state)
        binding.retryButton.setOnClickListener {
            viewModel.loadProducts()
        }
    }
    
    private fun observeProductListState() {
        viewModel.productListState.observe(this) { state ->
            when (state) {
                is ProductListState.Loading -> showLoading()
                is ProductListState.Success -> showProducts(state.products)
                is ProductListState.Empty -> showEmpty()
                is ProductListState.Error -> showError(state.message)
            }
        }
    }
    
    private fun observeProductCount() {
        viewModel.productCount.observe(this) { count ->
            if (count > 0) {
                binding.productCountText.text = getString(R.string.product_count, count)
                binding.productCountText.visibility = View.VISIBLE
            } else {
                binding.productCountText.visibility = View.GONE
            }
        }
    }
    
    private fun showLoading() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.productRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        binding.errorStateLayout.visibility = View.GONE
        binding.productCountText.visibility = View.GONE
        binding.refreshFab.isEnabled = false
    }
    
    private fun showProducts(products: List<Product>) {
        binding.loadingIndicator.visibility = View.GONE
        binding.productRecyclerView.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE
        binding.errorStateLayout.visibility = View.GONE
        binding.refreshFab.isEnabled = true
        
        adapter.submitList(products)
    }
    
    private fun showEmpty() {
        binding.loadingIndicator.visibility = View.GONE
        binding.productRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.errorStateLayout.visibility = View.GONE
        binding.productCountText.visibility = View.GONE
        binding.refreshFab.isEnabled = true
    }
    
    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.productRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        binding.errorStateLayout.visibility = View.VISIBLE
        binding.productCountText.visibility = View.GONE
        binding.errorText.text = message
        binding.refreshFab.isEnabled = true
    }
}
