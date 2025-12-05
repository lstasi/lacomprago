package com.lacomprago.data.api

/**
 * Validation utilities for API requests and data.
 *
 * Provides validation for tokens, customer IDs, and other API-related data
 * before making requests.
 */
object ApiValidation {

    /**
     * Minimum length for a valid token.
     * Based on observed Mercadona token format.
     */
    private const val MIN_TOKEN_LENGTH = 32

    /**
     * Maximum length for a valid token.
     */
    private const val MAX_TOKEN_LENGTH = 2048

    /**
     * Token format regex pattern.
     * Tokens are typically Base64-encoded JWT or similar.
     */
    private val TOKEN_PATTERN = Regex("^[A-Za-z0-9_\\-=.]+$")

    /**
     * Customer ID format regex pattern.
     * Customer IDs are typically numeric.
     */
    private val CUSTOMER_ID_PATTERN = Regex("^[A-Za-z0-9-]{4,128}$")

    /**
     * Order ID format regex pattern.
     * Order IDs are numeric.
     */
    private val ORDER_ID_PATTERN = Regex("^[0-9]+$")

    /**
     * Validate a token format.
     *
     * @param token The token to validate
     * @return ValidationResult indicating success or the specific error
     */
    fun validateToken(token: String?): ValidationResult {
        return when {
            token.isNullOrBlank() -> ValidationResult.Invalid("Token cannot be empty")
            token.length < MIN_TOKEN_LENGTH -> ValidationResult.Invalid("Token is too short (minimum $MIN_TOKEN_LENGTH characters)")
            token.length > MAX_TOKEN_LENGTH -> ValidationResult.Invalid("Token is too long")
            !token.matches(TOKEN_PATTERN) -> ValidationResult.Invalid("Token contains invalid characters")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate a customer ID format.
     *
     * @param customerId The customer ID to validate
     * @return ValidationResult indicating success or the specific error
     */
    fun validateCustomerId(customerId: String?): ValidationResult {
        return when {
            customerId.isNullOrBlank() -> ValidationResult.Invalid("Customer ID cannot be empty")
            !customerId.matches(CUSTOMER_ID_PATTERN) -> ValidationResult.Invalid("Customer ID must be alphanumeric/UUID")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate an order ID format.
     *
     * @param orderId The order ID to validate
     * @return ValidationResult indicating success or the specific error
     */
    fun validateOrderId(orderId: String?): ValidationResult {
        return when {
            orderId.isNullOrBlank() -> ValidationResult.Invalid("Order ID cannot be empty")
            !orderId.matches(ORDER_ID_PATTERN) -> ValidationResult.Invalid("Order ID must be numeric")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate postal code format (Spanish postal codes).
     *
     * @param postalCode The postal code to validate
     * @return ValidationResult indicating success or the specific error
     */
    fun validatePostalCode(postalCode: String?): ValidationResult {
        return when {
            postalCode.isNullOrBlank() -> ValidationResult.Invalid("Postal code cannot be empty")
            !postalCode.matches(Regex("^[0-9]{5}$")) -> ValidationResult.Invalid("Postal code must be 5 digits")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate recommendation type.
     *
     * @param type The recommendation type to validate
     * @return ValidationResult indicating success or the specific error
     */
    fun validateRecommendationType(type: String?): ValidationResult {
        val validTypes = listOf("precision", "recall")
        return when {
            type.isNullOrBlank() -> ValidationResult.Invalid("Recommendation type cannot be empty")
            type !in validTypes -> ValidationResult.Invalid("Recommendation type must be one of: ${validTypes.joinToString()}")
            else -> ValidationResult.Valid
        }
    }
}

/**
 * Result of a validation operation.
 */
sealed class ValidationResult {
    /**
     * Validation passed.
     */
    object Valid : ValidationResult()

    /**
     * Validation failed with an error message.
     *
     * @property message Description of why validation failed
     */
    data class Invalid(val message: String) : ValidationResult()

    /**
     * Check if the validation result is valid.
     */
    val isValid: Boolean
        get() = this is Valid

    /**
     * Get the error message if invalid, or null if valid.
     */
    val errorMessage: String?
        get() = (this as? Invalid)?.message

    /**
     * Throw an exception if validation failed.
     *
     * @throws ApiException with the validation error message
     */
    fun throwIfInvalid() {
        if (this is Invalid) {
            throw ApiException("Validation failed: $message", 400)
        }
    }
}
