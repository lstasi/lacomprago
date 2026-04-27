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
 * Main Activity - Entry point of the application.
 * Handles token input and validation. Automatically navigates to
 * ProductListActivity on successful login, and finishes itself so
 * the login screen is not in the back stack while the user is authenticated.
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
    }
    
    private fun observeAuthState() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.NoToken -> showNoTokenState()
                is AuthState.ValidatingToken -> showValidatingState()
                is AuthState.TokenValid -> navigateToProductList()
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
    
    private fun showTokenInvalidState(message: String) {
        binding.apply {
            tokenInputLayout.isEnabled = true
            submitButton.isEnabled = true
            progressIndicator.visibility = View.GONE
            statusText.visibility = View.GONE
            tokenInputLayout.error = message
        }
    }
    
    /**
     * Navigate to ProductListActivity after successful login.
     * Clears the activity back stack so the user cannot navigate back
     * to the login screen while authenticated.
     */
    private fun navigateToProductList() {
        val intent = Intent(this, ProductListActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
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
