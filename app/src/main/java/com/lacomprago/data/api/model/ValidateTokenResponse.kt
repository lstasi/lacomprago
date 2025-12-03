package com.lacomprago.data.api.model

/**
 * Response from the API when validating a token.
 *
 * @property valid Whether the token is valid
 */
data class ValidateTokenResponse(
    val valid: Boolean
)
