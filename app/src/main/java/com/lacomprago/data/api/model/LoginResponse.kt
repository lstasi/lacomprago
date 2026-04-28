package com.lacomprago.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Response from the login endpoint.
 *
 * Endpoint: POST /api/auth/tokens/
 *
 * @property accessToken The access token for authenticating subsequent requests
 * @property customerId The customer ID associated with this account
 */
data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("customer_id")
    val customerId: String
)
