package com.lacomprago.model

/**
 * Sealed class representing the authentication states in the app.
 * Used by AuthViewModel to communicate the current authentication status to the UI.
 */
sealed class AuthState {
    /**
     * Initial state when no token exists
     */
    object NoToken : AuthState()
    
    /**
     * State while validating the token
     */
    object ValidatingToken : AuthState()
    
    /**
     * State when token has been validated and stored successfully
     * @param token The validated token
     */
    data class TokenValid(val token: String) : AuthState()
    
    /**
     * State when token validation failed
     * @param message Error message to display to the user
     */
    data class TokenInvalid(val message: String) : AuthState()
}
