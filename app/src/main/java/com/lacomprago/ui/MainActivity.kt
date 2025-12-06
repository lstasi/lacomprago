package com.lacomprago.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.lacomprago.data.api.ApiClient
import com.lacomprago.BuildConfig
import com.lacomprago.R
import com.lacomprago.databinding.ActivityMainBinding
import com.lacomprago.model.AuthState
import com.lacomprago.storage.TokenStorage
import com.lacomprago.ui.debug.DebugActivity
import com.lacomprago.viewmodel.AuthViewModel

/**
 * Main Activity - Entry point of the application
 * Handles token input and validation
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: AuthViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewModel with TokenStorage
        val tokenStorage = TokenStorage(applicationContext)
        val apiClient = ApiClient.create(tokenStorage)
        viewModel = AuthViewModel(tokenStorage, apiClient)
        
        setupListeners()
        observeAuthState()

        if (BuildConfig.DEBUG) {
            setupDebugAccess()
        }
    }
    
    private fun setupListeners() {
        // Submit button click
        binding.submitButton.setOnClickListener {
            val token = binding.tokenEditText.text.toString()
            viewModel.submitToken(token)
        }
        
        // Clear token button click
        binding.clearTokenButton.setOnClickListener {
            viewModel.clearToken()
            binding.tokenEditText.text?.clear()
            binding.tokenInputLayout.error = null
        }
        
        // Handle keyboard submit action
        binding.tokenEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val token = binding.tokenEditText.text.toString()
                viewModel.submitToken(token)
                true
            } else {
                false
            }
        }
        
        // Continue to products button click
        binding.continueButton.setOnClickListener {
            navigateToProductList()
        }
    }
    
    private fun observeAuthState() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.NoToken -> showNoTokenState()
                is AuthState.ValidatingToken -> showValidatingState()
                is AuthState.TokenValid -> showTokenValidState()
                is AuthState.TokenInvalid -> showTokenInvalidState(state.message)
            }
        }
    }
    
    private fun showNoTokenState() {
        binding.apply {
            tokenInputLayout.isEnabled = true
            submitButton.isEnabled = true
            submitButton.visibility = View.VISIBLE
            clearTokenButton.visibility = View.GONE
            continueButton.visibility = View.GONE
            progressIndicator.visibility = View.GONE
            statusText.visibility = View.GONE
            tokenInputLayout.error = null
        }
    }
    
    private fun showValidatingState() {
        binding.apply {
            tokenInputLayout.isEnabled = false
            submitButton.isEnabled = false
            progressIndicator.visibility = View.VISIBLE
            statusText.visibility = View.VISIBLE
            statusText.text = getString(R.string.token_validating)
            statusText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_secondary))
        }
    }
    
    private fun showTokenValidState() {
        binding.apply {
            tokenInputLayout.isEnabled = false
            submitButton.visibility = View.GONE
            clearTokenButton.visibility = View.VISIBLE
            continueButton.visibility = View.VISIBLE
            progressIndicator.visibility = View.GONE
            statusText.visibility = View.VISIBLE
            statusText.text = getString(R.string.token_stored)
            statusText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.success_green))
            tokenInputLayout.error = null
        }
    }
    
    private fun showTokenInvalidState(message: String) {
        binding.apply {
            tokenInputLayout.isEnabled = true
            submitButton.isEnabled = true
            progressIndicator.visibility = View.GONE
            statusText.visibility = View.GONE
            tokenInputLayout.error = message
        }
    }
    
    private fun navigateToProductList() {
        val intent = Intent(this, ProductListActivity::class.java)
        startActivity(intent)
    }

    private fun setupDebugAccess() {
        val versionLabel = getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME
        binding.versionText.apply {
            text = versionLabel
            visibility = View.VISIBLE
            setOnLongClickListener {
                startActivity(Intent(this@MainActivity, DebugActivity::class.java))
                true
            }
        }
    }
}
