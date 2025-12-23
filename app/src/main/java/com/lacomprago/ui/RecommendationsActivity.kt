package com.lacomprago.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.lacomprago.databinding.ActivityRecommendationsBinding
import com.lacomprago.data.api.ApiClient
import com.lacomprago.model.RecommendationsState
import com.lacomprago.storage.JsonStorage
import com.lacomprago.storage.TokenStorage
import com.lacomprago.viewmodel.RecommendationsViewModel

/**
 * Activity for displaying product recommendations from the Mercadona API.
 *
 * Shows two types of recommendations:
 * - Precision: Products the user buys most frequently
 * - Recall: Products the user also buys
 *
 * Compares recommendations with local product frequency data.
 */
class RecommendationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecommendationsBinding
    private lateinit var viewModel: RecommendationsViewModel
    private lateinit var precisionAdapter: RecommendationAdapter
    private lateinit var recallAdapter: RecommendationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecommendationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        setupToolbar()
        setupRecyclerViews()
        setupListeners()
        observeState()

        // Load recommendations
        loadRecommendations()
    }

    private fun initViewModel() {
        val jsonStorage = JsonStorage(applicationContext)
        val tokenStorage = TokenStorage(applicationContext)
        val apiClient = ApiClient.create(tokenStorage)
        viewModel = RecommendationsViewModel(jsonStorage, apiClient)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerViews() {
        // Precision recommendations
        precisionAdapter = RecommendationAdapter(emptyList())
        binding.precisionRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecommendationsActivity)
            adapter = precisionAdapter
        }

        // Recall recommendations
        recallAdapter = RecommendationAdapter(emptyList())
        binding.recallRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecommendationsActivity)
            adapter = recallAdapter
        }
    }

    private fun setupListeners() {
        binding.retryButton.setOnClickListener {
            loadRecommendations()
        }
    }

    private fun loadRecommendations() {
        val tokenStorage = TokenStorage(applicationContext)
        val customerId = tokenStorage.getCustomerId()

        if (customerId != null) {
            viewModel.loadRecommendations(customerId)
        } else {
            showError("Customer ID not found. Please sign in again.")
        }
    }

    private fun observeState() {
        viewModel.recommendationsState.observe(this) { state ->
            when (state) {
                is RecommendationsState.Loading -> showLoading()
                is RecommendationsState.Success -> showSuccess(state)
                is RecommendationsState.Empty -> showEmpty()
                is RecommendationsState.Error -> showError(state.message)
            }
        }
    }

    private fun showLoading() {
        binding.loadingStateLayout.isVisible = true
        binding.emptyStateLayout.isVisible = false
        binding.errorStateLayout.isVisible = false
        binding.successStateLayout.isVisible = false
    }

    private fun showSuccess(state: RecommendationsState.Success) {
        binding.loadingStateLayout.isVisible = false
        binding.emptyStateLayout.isVisible = false
        binding.errorStateLayout.isVisible = false
        binding.successStateLayout.isVisible = true

        // Update adapters with local products for comparison
        precisionAdapter = RecommendationAdapter(state.localProducts)
        binding.precisionRecyclerView.adapter = precisionAdapter
        precisionAdapter.submitList(state.precisionItems)

        recallAdapter = RecommendationAdapter(state.localProducts)
        binding.recallRecyclerView.adapter = recallAdapter
        recallAdapter.submitList(state.recallItems)

        // Hide sections if empty
        if (state.precisionItems.isEmpty()) {
            binding.precisionRecyclerView.isVisible = false
        }

        if (state.recallItems.isEmpty()) {
            binding.recallRecyclerView.isVisible = false
        }
    }

    private fun showEmpty() {
        binding.loadingStateLayout.isVisible = false
        binding.emptyStateLayout.isVisible = true
        binding.errorStateLayout.isVisible = false
        binding.successStateLayout.isVisible = false
    }

    private fun showError(message: String) {
        binding.loadingStateLayout.isVisible = false
        binding.emptyStateLayout.isVisible = false
        binding.errorStateLayout.isVisible = true
        binding.successStateLayout.isVisible = false
        binding.errorText.text = message
    }
}
