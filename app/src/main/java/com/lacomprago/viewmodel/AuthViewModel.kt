package com.lacomprago.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lacomprago.data.api.ApiClient
import com.lacomprago.data.api.ApiException
import com.lacomprago.data.api.model.CustomerResponse
import com.lacomprago.model.AuthState
import com.lacomprago.storage.TokenStorage
import com.lacomprago.storage.TokenValidationResult
import com.lacomprago.storage.TokenValidator
import com.lacomprago.util.JwtDecoder
import kotlinx.coroutines.launch

/**
 * ViewModel for handling authentication state.
 * Manages token input, validation, storage, and clearing.
 */
class AuthViewModel(
    private val tokenStorage: TokenStorage,
    private val apiClient: ApiClient,
    private val tokenValidator: TokenValidator = TokenValidator()
) : ViewModel() {
    
    private val _authState = MutableLiveData<AuthState>(AuthState.NoToken)
    val authState: LiveData<AuthState> = _authState
    
    init {
        validateStoredToken()
    }
    
    /**
     * Validate an already stored token when the app starts.
     * Uses the customer info endpoint to ensure the token is still valid.
     */
    private fun validateStoredToken() {
        val storedToken = tokenStorage.getToken()
        if (storedToken.isNullOrBlank()) {
            _authState.value = AuthState.NoToken
            return
        }

        _authState.value = AuthState.ValidatingToken

        viewModelScope.launch {
            val customerId = resolveCustomerId(storedToken, preferStored = true)
            if (customerId.isNullOrBlank()) {
                tokenStorage.clearToken()
                _authState.value = AuthState.TokenInvalid("Customer ID missing. Please re-enter your token.")
                return@launch
            }

            try {
                val response = apiClient.getCustomerInfo(customerId)
                persistCustomerData(response, customerId)
                _authState.value = AuthState.TokenValid(storedToken)
            } catch (e: Exception) {
                handleValidationFailure(e)
            }
        }
    }
    
    /**
     * Submit a token for validation and storage.
     * Extracts customer UUID from the JWT, validates the token via API,
     * then stores both token and customer ID.
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
        val customerId = resolveCustomerId(trimmedToken, preferStored = false)
        if (customerId.isNullOrEmpty()) {
            _authState.value = AuthState.TokenInvalid("Could not extract customer ID from token")
            return
        }

        // Set validating state
        _authState.value = AuthState.ValidatingToken

        // Temporarily store the token so the interceptor can attach it
        tokenStorage.saveToken(trimmedToken)

        viewModelScope.launch {
            try {
                val response = apiClient.getCustomerInfo(customerId)
                persistCustomerData(response, customerId)
                _authState.value = AuthState.TokenValid(trimmedToken)
            } catch (e: Exception) {
                handleValidationFailure(e)
            }
        }
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

    private fun resolveCustomerId(token: String, preferStored: Boolean): String? {
        val storedId = tokenStorage.getCustomerId()?.takeIf { it.isNotBlank() }
        if (preferStored && storedId != null) {
            return storedId
        }

        val decoded = JwtDecoder.extractCustomerId(token)
        return decoded ?: if (preferStored) storedId else null
    }

    private fun persistCustomerData(response: CustomerResponse, fallbackCustomerId: String) {
        // Mercadona identifiers for downstream calls are the customer_uuid; do not fall back to numeric ids
        val resolvedCustomerId = when {
            response.uuid.isNotBlank() -> response.uuid
            else -> fallbackCustomerId
        }
        tokenStorage.saveCustomerId(resolvedCustomerId)
    }

    private fun handleValidationFailure(e: Exception) {
        val (message, shouldClearToken) = when (e) {
            is ApiException -> when (e.httpCode) {
                401, 403 -> "Token is invalid or expired" to true
                else -> (e.message ?: "Token validation failed") to false
            }
            else -> {
                val fallback = e.message?.let { "Token validation failed: $it" } ?: "Token validation failed"
                fallback to false
            }
        }

        if (shouldClearToken) {
            tokenStorage.clearToken()
        }
        _authState.value = AuthState.TokenInvalid(message)
    }
}
