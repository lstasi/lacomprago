package com.lacomprago.data.api

import com.lacomprago.BuildConfig
import java.util.concurrent.TimeUnit

/**
 * API Configuration
 * Contains base URL and timeout settings for API calls
 */
object ApiConfig {
    /**
     * Base URL for the API
     * Configured via gradle.properties (API_BASE_URL)
     */
    const val BASE_URL = BuildConfig.API_BASE_URL
    
    /**
     * Connection timeout in seconds
     */
    const val CONNECT_TIMEOUT = 30L
    
    /**
     * Read timeout in seconds
     */
    const val READ_TIMEOUT = 30L
    
    /**
     * Write timeout in seconds
     */
    const val WRITE_TIMEOUT = 30L
}
