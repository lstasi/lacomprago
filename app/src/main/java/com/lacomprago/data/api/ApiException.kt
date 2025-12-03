package com.lacomprago.data.api

/**
 * Exception thrown when an API call fails.
 *
 * @property message Description of the error
 * @property httpCode Optional HTTP status code
 */
class ApiException(
    message: String,
    val httpCode: Int? = null
) : Exception(message)
