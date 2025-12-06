package com.lacomprago.storage

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TokenValidator
 */
class TokenValidatorTest {
    
    private lateinit var validator: TokenValidator
    
    @Before
    fun setup() {
        validator = TokenValidator()
    }
    
    @Test
    fun `validate returns Invalid for empty token`() {
        val result = validator.validate("")
        
        assertTrue(result is TokenValidationResult.Invalid)
        assertEquals("Token cannot be empty", (result as TokenValidationResult.Invalid).message)
    }
    
    @Test
    fun `validate returns Invalid for blank token`() {
        val result = validator.validate("   ")
        
        assertTrue(result is TokenValidationResult.Invalid)
        assertEquals("Token cannot be empty", (result as TokenValidationResult.Invalid).message)
    }
    
    @Test
    fun `validate returns Invalid for short token`() {
        val shortToken = "abc123" // Less than 32 characters
        val result = validator.validate(shortToken)
        
        assertTrue(result is TokenValidationResult.Invalid)
        assertEquals("Token is too short (minimum 32 characters)", (result as TokenValidationResult.Invalid).message)
    }
    
    @Test
    fun `validate returns Invalid for token with special characters`() {
        // Token must be at least 32 characters to pass the length check first
        val invalidToken = "abcdefghijklmnopqrstuvwxyz!@#$%^"
        val result = validator.validate(invalidToken)
        
        assertTrue(result is TokenValidationResult.Invalid)
        assertEquals("Token contains invalid characters", (result as TokenValidationResult.Invalid).message)
    }
    
    @Test
    fun `validate returns Valid for alphanumeric token of minimum length`() {
        val validToken = "abcdefghijklmnopqrstuvwxyz123456" // Exactly 32 characters
        val result = validator.validate(validToken)
        
        assertTrue(result is TokenValidationResult.Valid)
    }
    
    @Test
    fun `validate returns Valid for long alphanumeric token`() {
        val validToken = "abcdefghijklmnopqrstuvwxyz1234567890abcd"
        val result = validator.validate(validToken)
        
        assertTrue(result is TokenValidationResult.Valid)
    }
    
    @Test
    fun `validate returns Valid for token with dashes and underscores`() {
        val validToken = "abc-def_ghi-jkl_mnopqrstuvwxyzzz" // 32 chars including allowed symbols
        val result = validator.validate(validToken)
        
        assertTrue(result is TokenValidationResult.Valid)
    }
    
    @Test
    fun `validate returns Valid for uppercase token`() {
        val validToken = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456"
        val result = validator.validate(validToken)
        
        assertTrue(result is TokenValidationResult.Valid)
    }
    
    @Test
    fun `validate returns Valid for mixed case token`() {
        val validToken = "AbCdEfGhIjKlMnOpQrStUvWxYz123456"
        val result = validator.validate(validToken)
        
        assertTrue(result is TokenValidationResult.Valid)
    }

    @Test
    fun `validate returns Valid for JWT token with periods`() {
        // JWT tokens have the format: header.payload.signature
        val jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        val result = validator.validate(jwtToken)

        assertTrue(result is TokenValidationResult.Valid)
    }
}
