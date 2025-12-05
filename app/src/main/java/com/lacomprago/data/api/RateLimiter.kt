package com.lacomprago.data.api

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Rate limiter to prevent exceeding API request limits.
 *
 * Implements a simple sliding window rate limiter:
 * - Maximum 60 requests per minute
 * - Minimum 1.5 seconds between requests for sequential operations
 *
 * @property maxRequestsPerMinute Maximum number of requests allowed per minute
 * @property minDelayBetweenRequestsMs Minimum delay between sequential requests in milliseconds
 */
class RateLimiter(
    private val maxRequestsPerMinute: Int = MAX_REQUESTS_PER_MINUTE,
    private val minDelayBetweenRequestsMs: Long = MIN_DELAY_BETWEEN_REQUESTS_MS
) {
    private val mutex = Mutex()
    private val requestTimestamps = mutableListOf<Long>()
    private var lastRequestTime = 0L

    /**
     * Wait if necessary before making a request to respect rate limits.
     *
     * This method will:
     * 1. Ensure minimum delay between sequential requests
     * 2. Wait if we've exceeded the requests per minute limit
     */
    suspend fun acquire() {
        mutex.withLock {
            val now = System.currentTimeMillis()
            
            // Clean up old timestamps (older than 1 minute)
            val oneMinuteAgo = now - MINUTE_IN_MS
            requestTimestamps.removeAll { it < oneMinuteAgo }
            
            // Check if we need to wait for rate limit
            if (requestTimestamps.size >= maxRequestsPerMinute) {
                // Wait until the oldest request is more than 1 minute old
                val oldestRequest = requestTimestamps.first()
                val waitTime = (oldestRequest + MINUTE_IN_MS) - now
                if (waitTime > 0) {
                    delay(waitTime)
                }
                // Clean up again after waiting
                val newNow = System.currentTimeMillis()
                requestTimestamps.removeAll { it < newNow - MINUTE_IN_MS }
            }
            
            // Ensure minimum delay between requests
            val timeSinceLastRequest = now - lastRequestTime
            if (timeSinceLastRequest < minDelayBetweenRequestsMs && lastRequestTime > 0) {
                delay(minDelayBetweenRequestsMs - timeSinceLastRequest)
            }
            
            // Record this request
            val requestTime = System.currentTimeMillis()
            requestTimestamps.add(requestTime)
            lastRequestTime = requestTime
        }
    }

    /**
     * Get current request count in the sliding window.
     * Useful for monitoring and debugging.
     */
    suspend fun getCurrentRequestCount(): Int {
        mutex.withLock {
            val now = System.currentTimeMillis()
            val oneMinuteAgo = now - MINUTE_IN_MS
            requestTimestamps.removeAll { it < oneMinuteAgo }
            return requestTimestamps.size
        }
    }

    /**
     * Reset the rate limiter state.
     * Useful for testing.
     */
    suspend fun reset() {
        mutex.withLock {
            requestTimestamps.clear()
            lastRequestTime = 0
        }
    }

    companion object {
        private const val MINUTE_IN_MS = 60_000L
        private const val MAX_REQUESTS_PER_MINUTE = 60
        private const val MIN_DELAY_BETWEEN_REQUESTS_MS = 1500L // 1.5 seconds to respect 60 req/min
    }
}
