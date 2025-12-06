package com.lacomprago.util

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * Utility class for decoding JWT tokens without validation.
 * Extracts claims from the payload section of the token.
 */
object JwtDecoder {

    private val gson = Gson()

    /**
     * Extract the customer_uuid from a JWT token.
     *
     * @param token The JWT token string
     * @return The customer UUID, or null if not found or token is invalid
     */
    fun extractCustomerId(token: String): String? {
        return try {
            val claims = decodePayload(token)
            claims?.get("customer_uuid")?.asString
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decode the payload section of a JWT token.
     * JWT tokens have 3 parts separated by dots: header.payload.signature
     *
     * @param token The JWT token string
     * @return The decoded payload as a JsonObject, or null if invalid
     */
    private fun decodePayload(token: String): JsonObject? {
        val parts = token.split(".")
        if (parts.size != 3) {
            return null
        }

        val payload = parts[1]

        // Base64 decode the payload; pad if needed to avoid IllegalArgumentException
        val paddedPayload = when (payload.length % 4) {
            2 -> "$payload=="
            3 -> "$payload="
            else -> payload
        }

        val decodedBytes = Base64.decode(paddedPayload, Base64.URL_SAFE or Base64.NO_WRAP)
        val decodedString = String(decodedBytes, Charsets.UTF_8)

        // Parse as JSON
        return gson.fromJson(decodedString, JsonObject::class.java)
    }
}

