package com.lacomprago.storage

import com.lacomprago.data.api.ApiValidation
import com.lacomprago.data.api.ValidationResult

/**
 * Token validator that delegates to ApiValidation for format checks.
 * API validation is still performed by hitting Mercadona endpoints.
 */
class TokenValidator {

    /**
     * Validate the token format using the shared API rules.
     * @param token The token to validate
     * @return TokenValidationResult containing success or error
     */
    fun validate(token: String): TokenValidationResult {
        val result = ApiValidation.validateToken(token)
        return when (result) {
            is ValidationResult.Valid -> TokenValidationResult.Valid
            is ValidationResult.Invalid -> TokenValidationResult.Invalid(result.message)
        }
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
