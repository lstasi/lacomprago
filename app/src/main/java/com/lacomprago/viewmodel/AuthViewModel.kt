package com.lacomprago.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lacomprago.model.AuthState
import com.lacomprago.storage.TokenStorage
import com.lacomprago.storage.TokenValidationResult
import com.lacomprago.storage.TokenValidator
import com.lacomprago.util.JwtDecoder

/**
 * ViewModel for handling authentication state.
 * Manages token input, validation, storage, and clearing.
 */
class AuthViewModel(
    private val tokenStorage: TokenStorage,
    private val tokenValidator: TokenValidator = TokenValidator()
) : ViewModel() {
    
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    
    init {
        checkExistingToken()
    }
    
    /**
     * Check if a token already exists in storage.
     * Updates auth state accordingly.
     * Also extracts and saves customer ID from JWT if not already stored.
     */
    private fun checkExistingToken() {
        val token = tokenStorage.getToken()
        _authState.value = if (token != null) {
            // Ensure customer ID is extracted and saved (for tokens stored before JWT decoding was added)
            if (tokenStorage.getCustomerId() == null) {
                val customerId = JwtDecoder.extractCustomerId(token)
                if (customerId != null) {
                    tokenStorage.saveCustomerId(customerId)
                }
            }
            AuthState.TokenValid(token)
        } else {
            AuthState.NoToken
        }
    }
    
    /**
     * Submit a token for validation and storage.
     * Extracts customer_uuid from JWT token claims automatically.
     * @param token The API token (JWT) to validate and store
     */
    fun submitToken(token: String) {
        val trimmedToken = token.trim()

        // Validate token format
        when (val validationResult = tokenValidator.validate(trimmedToken)) {
            is TokenValidationResult.Invalid -> {
                _authState.value = AuthState.TokenInvalid(validationResult.message)
                return
            }
            is TokenValidationResult.Valid -> {
                // Token format is valid, proceed
            }
        }
        
        // Extract customer_uuid from JWT token
        val customerId = JwtDecoder.extractCustomerId(trimmedToken)
        if (customerId.isNullOrEmpty()) {
            _authState.value = AuthState.TokenInvalid("Could not extract customer ID from token")
            return
        }

        // Set validating state
        _authState.value = AuthState.ValidatingToken
        
        // Store the token and customer ID (actual API validation will happen on first API call)
        tokenStorage.saveToken(trimmedToken)
        tokenStorage.saveCustomerId(customerId)

        // Mark as valid
        _authState.value = AuthState.TokenValid(trimmedToken)
    }
    
    /**
     * Clear the stored token and reset to NoToken state.
     */
    fun clearToken() {
        tokenStorage.clearToken()
        _authState.value = AuthState.NoToken
    }
    
    /**
     * Check if a token exists.
     * @return true if a token is stored, false otherwise
     */
    fun hasToken(): Boolean {
        return tokenStorage.hasToken()
    }
}
