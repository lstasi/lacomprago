package com.lacomprago.data.api

import java.io.IOException

/**
 * Sealed class representing different types of API errors.
 * Used to categorize errors for appropriate handling and user feedback.
 */
sealed class ApiError {
    /**
     * Network connectivity error (no internet, timeout, etc.)
     */
    object NetworkError : ApiError()
    
    /**
     * Authentication error (401 Unauthorized)
     */
    object Unauthorized : ApiError()
    
    /**
     * Server error with HTTP status code (5xx errors)
     *
     * @property code HTTP status code
     */
    data class ServerError(val code: Int) : ApiError()
    
    /**
     * Resource not found error (404)
     *
     * @property resource Description of the resource that was not found
     */
    data class NotFound(val resource: String) : ApiError()
    
    /**
     * Bad request error (400)
     *
     * @property message Description of the validation error
     */
    data class BadRequest(val message: String) : ApiError()
    
    /**
     * Unknown or unhandled error
     *
     * @property message Error message
     */
    data class Unknown(val message: String) : ApiError()
    
    companion object {
        /**
         * Convert an exception to an appropriate ApiError.
         *
         * @param exception The exception to convert
         * @return Appropriate ApiError type
         */
        fun fromException(exception: Exception): ApiError {
            return when (exception) {
                is IOException -> NetworkError
                is ApiException -> {
                    val code = exception.httpCode
                    when {
                        code == 401 -> Unauthorized
                        code == 404 -> NotFound(exception.message ?: "Resource not found")
                        code == 400 -> BadRequest(exception.message ?: "Bad request")
                        code != null && code in 500..599 -> ServerError(code)
                        else -> Unknown(exception.message ?: "Unknown error")
                    }
                }
                else -> Unknown(exception.message ?: "Unknown error")
            }
        }
        
        /**
         * Get a user-friendly error message for an ApiError.
         *
         * @param error The ApiError to convert to a message
         * @return User-friendly error message
         */
        fun getMessage(error: ApiError): String {
            return when (error) {
                is NetworkError -> "Network error. Please check your connection and try again."
                is Unauthorized -> "Your session has expired. Please re-enter your token."
                is ServerError -> "Server error (${error.code}). Please try again later."
                is NotFound -> "Resource not found: ${error.resource}"
                is BadRequest -> "Invalid request: ${error.message}"
                is Unknown -> error.message
            }
        }
    }
}
