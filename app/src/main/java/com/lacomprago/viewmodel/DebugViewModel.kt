package com.lacomprago.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lacomprago.data.api.ApiClient
import com.lacomprago.data.api.DebugApiResponse
import com.lacomprago.data.api.debug.ApiEndpoint
import com.lacomprago.data.api.debug.EndpointDefinitions
import com.lacomprago.data.api.debug.HttpMethod
import com.lacomprago.storage.TokenStorage
import kotlinx.coroutines.launch

/**
 * UI State for Debug screen.
 */
sealed class DebugState {
    /** Initial state with no activity */
    object Idle : DebugState()
    
    /** Request is being prepared */
    data class Preparing(val request: DebugRequest) : DebugState()
    
    /** Request is in progress */
    object Loading : DebugState()
    
    /** Request completed successfully */
    data class Success(val request: DebugRequest, val response: DebugApiResponse) : DebugState()
    
    /** Request failed with error */
    data class Error(val request: DebugRequest?, val message: String, val response: DebugApiResponse?) : DebugState()
}

/**
 * Token status for display.
 */
sealed class TokenStatus {
    object None : TokenStatus()
    object Stored : TokenStatus()
    data class Invalid(val reason: String) : TokenStatus()
}

/**
 * Represents a prepared debug request.
 */
data class DebugRequest(
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val body: String?
)

/**
 * ViewModel for the Debug Mode screen.
 *
 * Manages token status, endpoint selection, request configuration,
 * and API request execution for debugging purposes.
 */
class DebugViewModel(
    private val tokenStorage: TokenStorage,
    private val apiClient: ApiClient
) : ViewModel() {

    private val _state = MutableLiveData<DebugState>(DebugState.Idle)
    val state: LiveData<DebugState> = _state

    private val _tokenStatus = MutableLiveData<TokenStatus>()
    val tokenStatus: LiveData<TokenStatus> = _tokenStatus

    private val _selectedEndpoint = MutableLiveData<ApiEndpoint?>()
    val selectedEndpoint: LiveData<ApiEndpoint?> = _selectedEndpoint

    private val _customerId = MutableLiveData<String>()
    val customerId: LiveData<String> = _customerId

    // Path and query parameters for the current request
    private val pathParams = mutableMapOf<String, String>()
    private val queryParams = mutableMapOf<String, String>()
    private var requestBody: String? = null

    /** List of all available endpoints */
    val endpoints: List<ApiEndpoint> = EndpointDefinitions.endpoints

    init {
        loadInitialState()
    }

    /**
     * Load initial state from storage.
     */
    private fun loadInitialState() {
        val token = tokenStorage.getToken()
        val storedCustomerId = tokenStorage.getCustomerId()

        _tokenStatus.value = if (token != null) TokenStatus.Stored else TokenStatus.None
        _customerId.value = storedCustomerId ?: ""
    }

    /**
     * Save token to secure storage.
     */
    fun saveToken(token: String) {
        if (token.isBlank()) {
            _tokenStatus.value = TokenStatus.Invalid("Token cannot be empty")
            return
        }
        
        tokenStorage.saveToken(token)
        _tokenStatus.value = TokenStatus.Stored
    }

    /**
     * Clear stored token.
     */
    fun clearToken() {
        tokenStorage.clearToken()
        _tokenStatus.value = TokenStatus.None
    }

    /**
     * Save customer ID to secure storage.
     */
    fun saveCustomerId(customerId: String) {
        val trimmed = customerId.trim()
        tokenStorage.saveCustomerId(trimmed)
        _customerId.value = trimmed
        // Auto-fill the customer_id path parameter
        if (trimmed.isNotEmpty()) {
            pathParams["customer_id"] = trimmed
        }
    }

    /**
     * Select an endpoint to test.
     */
    fun selectEndpoint(endpoint: ApiEndpoint) {
        _selectedEndpoint.value = endpoint
        
        // Reset parameters
        pathParams.clear()
        queryParams.clear()
        requestBody = endpoint.sampleBody
        
        // Pre-fill customer_id if stored
        val storedCustomerId = tokenStorage.getCustomerId()
        if (!storedCustomerId.isNullOrBlank() && endpoint.pathParams.contains("customer_id")) {
            pathParams["customer_id"] = storedCustomerId
        }

        // Seed other required path params with empty strings for UI placeholders (e.g., order_id)
        endpoint.pathParams.filter { it != "customer_id" }.forEach { param ->
            pathParams.putIfAbsent(param, "")
        }
        
        _state.value = DebugState.Idle
    }

    /**
     * Set a path parameter value.
     */
    fun setPathParam(name: String, value: String) {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            pathParams.remove(name)
        } else {
            pathParams[name] = trimmed
        }
    }

    /**
     * Set a query parameter value.
     */
    fun setQueryParam(name: String, value: String) {
        if (value.isBlank()) {
            queryParams.remove(name)
        } else {
            queryParams[name] = value
        }
    }

    /**
     * Set request body for POST/PUT requests.
     */
    fun setRequestBody(body: String?) {
        requestBody = body
    }

    /**
     * Prepare the request for preview without executing it.
     */
    fun prepareRequest() {
        val endpoint = _selectedEndpoint.value ?: return
        
        val request = buildRequest(endpoint)
        if (request != null) {
            _state.value = DebugState.Preparing(request)
        }
    }

    /**
     * Execute the prepared request.
     */
    fun executeRequest() {
        val endpoint = _selectedEndpoint.value ?: return
        
        viewModelScope.launch {
            _state.value = DebugState.Loading
            
            try {
                val request = buildRequest(endpoint)
                if (request == null) {
                    _state.value = DebugState.Error(null, "Failed to build request", null)
                    return@launch
                }
                
                // Build the path with substituted parameters
                val path = buildPath(endpoint)
                
                val response = apiClient.executeDebugRequest(
                    method = endpoint.method.name,
                    path = path,
                    queryParams = queryParams.toMap(),
                    body = if (endpoint.hasBody) requestBody else null
                )
                
                if (response.error != null) {
                    _state.value = DebugState.Error(request, response.error, response)
                } else if (response.statusCode >= 400) {
                    _state.value = DebugState.Error(
                        request, 
                        "HTTP ${response.statusCode}: ${response.statusMessage}", 
                        response
                    )
                } else {
                    _state.value = DebugState.Success(request, response)
                }
            } catch (e: Exception) {
                _state.value = DebugState.Error(null, e.message ?: "Unknown error", null)
            }
        }
    }

    /**
     * Build the request preview.
     */
    private fun buildRequest(endpoint: ApiEndpoint): DebugRequest? {
        val path = buildPath(endpoint)
        
        val urlBuilder = StringBuilder(com.lacomprago.data.api.ApiConfig.BASE_URL)
        urlBuilder.append(path)
        
        if (queryParams.isNotEmpty()) {
            urlBuilder.append("?")
            urlBuilder.append(queryParams.entries.joinToString("&") { "${it.key}=${it.value}" })
        }
        
        val headers = mutableMapOf<String, String>()
        headers["Content-Type"] = "application/json"
        
        val token = tokenStorage.getToken()
        if (token != null) {
            headers["Authorization"] = "Bearer ${token.take(20)}..."
        }
        
        return DebugRequest(
            method = endpoint.method.name,
            url = urlBuilder.toString(),
            headers = headers,
            body = if (endpoint.hasBody) requestBody else null
        )
    }

    /**
     * Build the path with substituted path parameters.
     */
    private fun buildPath(endpoint: ApiEndpoint): String {
        var path = endpoint.path
        for ((name, value) in pathParams) {
            path = path.replace("{$name}", value)
        }
        return path
    }

    /**
     * Reset state to idle.
     */
    fun resetState() {
        _state.value = DebugState.Idle
    }

    /**
     * Validate token by calling the customer info endpoint.
     */
    fun validateToken() {
        val storedCustomerId = tokenStorage.getCustomerId()
        if (storedCustomerId.isNullOrBlank()) {
            _tokenStatus.value = TokenStatus.Invalid("Customer ID required for validation")
            return
        }
        
        viewModelScope.launch {
            try {
                val isValid = apiClient.validateToken(storedCustomerId)
                _tokenStatus.value = if (isValid) TokenStatus.Stored else TokenStatus.Invalid("Token invalid")
            } catch (e: Exception) {
                _tokenStatus.value = TokenStatus.Invalid("Validation failed: ${e.message}")
            }
        }
    }

    /**
     * Get the current value of a path parameter.
     */
    fun getPathParam(name: String): String = pathParams[name] ?: ""

    /**
     * Get the current value of a query parameter.
     */
    fun getQueryParam(name: String): String = queryParams[name] ?: ""

    /**
     * Get the current request body.
     */
    fun getRequestBody(): String? = requestBody
}
