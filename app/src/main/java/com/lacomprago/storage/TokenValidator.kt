package com.lacomprago.storage

/**
 * Token validator that performs basic token format validation.
 * API validation will be performed on first API call.
 */
class TokenValidator {
    
    /**
     * Validate the token format.
     * @param token The token to validate
     * @return TokenValidationResult containing success or error
     */
    fun validate(token: String): TokenValidationResult {
        return when {
            token.isBlank() -> {
                TokenValidationResult.Invalid("Token cannot be empty")
            }
            token.length < MIN_TOKEN_LENGTH -> {
                TokenValidationResult.Invalid("Token is too short")
            }
            !token.matches(TOKEN_PATTERN.toRegex()) -> {
                TokenValidationResult.Invalid("Token contains invalid characters")
            }
            else -> {
                TokenValidationResult.Valid
            }
        }
    }
    
    companion object {
        /**
         * Minimum expected token length
         */
        const val MIN_TOKEN_LENGTH = 20
        
        /**
         * Token format: alphanumeric, dashes, underscores, and periods (for JWT)
         * JWT tokens have the format: header.payload.signature (Base64URL encoded)
         */
        private const val TOKEN_PATTERN = "[A-Za-z0-9_.-]+"
    }
}

/**
 * Result of token validation
 */
sealed class TokenValidationResult {
    /**
     * Token format is valid
     */
    object Valid : TokenValidationResult()
    
    /**
     * Token format is invalid
     * @param message Error message describing the issue
     */
    data class Invalid(val message: String) : TokenValidationResult()
}
