package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Request body for authenticating with email and password.
 *
 * Endpoint: POST /api/auth/tokens/
 *
 * @property username The user's email address (Mercadona account)
 * @property password The user's password
 */
data class LoginRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)
