package com.lacomprago.data.api

import com.lacomprago.BuildConfig

/**
 * API Configuration
 * Contains base URL and timeout settings for API calls
 */
object ApiConfig {
    /**
     * Base URL for the API
     * Configured via gradle.properties (API_BASE_URL)
     */
    val BASE_URL: String = normalizeBaseUrl(BuildConfig.API_BASE_URL)
    
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
    
    private fun normalizeBaseUrl(raw: String?): String {
        if (raw.isNullOrBlank()) return "https://tienda.mercadona.es/api/"
        var base = raw.trim()
        // Ensure trailing slash
        if (!base.endsWith("/")) base += "/"
        // Ensure /api/ segment present exactly once
        val lower = base.lowercase()
        return when {
            lower.contains("/api/") -> base
            lower.endsWith("/api/") -> base
            lower.endsWith("/api") -> base + "/"
            else -> base + "api/"
        }
    }
}
