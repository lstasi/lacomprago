package com.lacomprago.data.api

import kotlinx.coroutines.delay
import java.io.IOException

/**
 * Retry helper for API calls with exponential backoff.
 *
 * This helper only retries on network errors (IOException and its subclasses).
 * Other exceptions like ApiException (HTTP errors) are not retried as they typically
 * indicate client errors (4xx) or server errors (5xx) that won't be resolved by retrying.
 *
 * For HTTP 5xx errors, consider implementing application-level retry logic that
 * examines the ApiException.httpCode to determine if retrying is appropriate.
 */
object RetryHelper {
    
    /**
     * Retry an API call with exponential backoff on network errors.
     *
     * This method will retry the block only when an IOException is thrown,
     * which typically indicates network connectivity issues such as:
     * - Connection timeouts
     * - Socket timeouts
     * - No internet connectivity
     * - DNS resolution failures
     *
     * Non-network exceptions (like ApiException for HTTP errors) are propagated
     * immediately without retry, as these typically indicate errors that won't
     * be resolved by retrying the same request.
     *
     * @param maxRetries Maximum number of retry attempts (default: 3)
     * @param initialDelayMs Initial delay between retries in milliseconds (default: 1000)
     * @param maxDelayMs Maximum delay between retries in milliseconds (default: 10000)
     * @param factor Multiplier for exponential backoff (default: 2.0)
     * @param block The suspending function to retry
     * @return The result of the successful call
     * @throws IOException If all retries fail due to network errors
     * @throws Exception Any non-IOException is propagated immediately without retry
     */
    suspend fun <T> retryWithBackoff(
        maxRetries: Int = DEFAULT_MAX_RETRIES,
        initialDelayMs: Long = DEFAULT_INITIAL_DELAY_MS,
        maxDelayMs: Long = DEFAULT_MAX_DELAY_MS,
        factor: Double = DEFAULT_BACKOFF_FACTOR,
        block: suspend () -> T
    ): T {
        var lastException: Exception? = null
        var currentDelay = initialDelayMs
        
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: IOException) {
                lastException = e
                
                // Don't delay after the last attempt
                if (attempt < maxRetries - 1) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
                }
            }
            // Note: Non-IOException exceptions propagate immediately
        }
        
        throw lastException ?: IOException("Unknown error during retry")
    }
    
    private const val DEFAULT_MAX_RETRIES = 3
    private const val DEFAULT_INITIAL_DELAY_MS = 1000L
    private const val DEFAULT_MAX_DELAY_MS = 10000L
    private const val DEFAULT_BACKOFF_FACTOR = 2.0
}
