package com.lacomprago.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Unit tests for ApiError and ApiException.
 */
class ApiErrorTest {
    
    // ApiError.fromException Tests
    
    @Test
    fun `fromException returns NetworkError for IOException`() {
        val exception = IOException("Connection failed")
        
        val error = ApiError.fromException(exception)
        
        assertTrue(error is ApiError.NetworkError)
    }
    
    @Test
    fun `fromException returns NetworkError for SocketTimeoutException`() {
        val exception = SocketTimeoutException("Connection timed out")
        
        val error = ApiError.fromException(exception)
        
        // SocketTimeoutException extends IOException
        assertTrue(error is ApiError.NetworkError)
    }
    
    @Test
    fun `fromException returns Unauthorized for 401 ApiException`() {
        val exception = ApiException("Unauthorized", 401)
        
        val error = ApiError.fromException(exception)
        
        assertTrue(error is ApiError.Unauthorized)
    }
    
    @Test
    fun `fromException returns NotFound for 404 ApiException`() {
        val exception = ApiException("Order not found", 404)
        
        val error = ApiError.fromException(exception)
        
        assertTrue(error is ApiError.NotFound)
        assertEquals("Order not found", (error as ApiError.NotFound).resource)
    }
    
    @Test
    fun `fromException returns BadRequest for 400 ApiException`() {
        val exception = ApiException("Invalid request data", 400)
        
        val error = ApiError.fromException(exception)
        
        assertTrue(error is ApiError.BadRequest)
        assertEquals("Invalid request data", (error as ApiError.BadRequest).message)
    }
    
    @Test
    fun `fromException returns ServerError for 500 ApiException`() {
        val exception = ApiException("Internal server error", 500)
        
        val error = ApiError.fromException(exception)
        
        assertTrue(error is ApiError.ServerError)
        assertEquals(500, (error as ApiError.ServerError).code)
    }
    
    @Test
    fun `fromException returns ServerError for 503 ApiException`() {
        val exception = ApiException("Service unavailable", 503)
        
        val error = ApiError.fromException(exception)
        
        assertTrue(error is ApiError.ServerError)
        assertEquals(503, (error as ApiError.ServerError).code)
    }
    
    @Test
    fun `fromException returns Unknown for generic Exception`() {
        val exception = RuntimeException("Something went wrong")
        
        val error = ApiError.fromException(exception)
        
        assertTrue(error is ApiError.Unknown)
        assertEquals("Something went wrong", (error as ApiError.Unknown).message)
    }
    
    @Test
    fun `fromException returns Unknown for ApiException without httpCode`() {
        val exception = ApiException("Unknown API error")
        
        val error = ApiError.fromException(exception)
        
        assertTrue(error is ApiError.Unknown)
        assertEquals("Unknown API error", (error as ApiError.Unknown).message)
    }
    
    @Test
    fun `fromException returns Unknown for unhandled HTTP code`() {
        val exception = ApiException("Forbidden", 403)
        
        val error = ApiError.fromException(exception)
        
        assertTrue(error is ApiError.Unknown)
    }
    
    // ApiError.getMessage Tests
    
    @Test
    fun `getMessage returns appropriate message for NetworkError`() {
        val message = ApiError.getMessage(ApiError.NetworkError)
        
        assertTrue(message.contains("Network"))
        assertTrue(message.contains("connection"))
    }
    
    @Test
    fun `getMessage returns appropriate message for Unauthorized`() {
        val message = ApiError.getMessage(ApiError.Unauthorized)
        
        assertTrue(message.contains("session") || message.contains("expired") || message.contains("token"))
    }
    
    @Test
    fun `getMessage returns appropriate message for ServerError`() {
        val message = ApiError.getMessage(ApiError.ServerError(500))
        
        assertTrue(message.contains("500"))
        assertTrue(message.contains("Server"))
    }
    
    @Test
    fun `getMessage returns appropriate message for NotFound`() {
        val message = ApiError.getMessage(ApiError.NotFound("Order XYZ"))
        
        assertTrue(message.contains("not found"))
        assertTrue(message.contains("Order XYZ"))
    }
    
    @Test
    fun `getMessage returns appropriate message for BadRequest`() {
        val message = ApiError.getMessage(ApiError.BadRequest("Invalid quantity"))
        
        assertTrue(message.contains("Invalid quantity"))
    }
    
    @Test
    fun `getMessage returns the message for Unknown`() {
        val message = ApiError.getMessage(ApiError.Unknown("Custom error message"))
        
        assertEquals("Custom error message", message)
    }
    
    // ApiException Tests
    
    @Test
    fun `ApiException stores message correctly`() {
        val exception = ApiException("Test error message")
        
        assertEquals("Test error message", exception.message)
    }
    
    @Test
    fun `ApiException stores httpCode correctly`() {
        val exception = ApiException("Test error", 404)
        
        assertEquals(404, exception.httpCode)
    }
    
    @Test
    fun `ApiException without httpCode has null httpCode`() {
        val exception = ApiException("Test error")
        
        assertEquals(null, exception.httpCode)
    }
}
