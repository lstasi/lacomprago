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
        val shortToken = "abc123" // Less than 20 characters
        val result = validator.validate(shortToken)
        
        assertTrue(result is TokenValidationResult.Invalid)
        assertEquals("Token is too short", (result as TokenValidationResult.Invalid).message)
    }
    
    @Test
    fun `validate returns Invalid for token with special characters`() {
        val invalidToken = "abcdefghijklmnopqrst!@#$"
        val result = validator.validate(invalidToken)
        
        assertTrue(result is TokenValidationResult.Invalid)
        assertEquals("Token contains invalid characters", (result as TokenValidationResult.Invalid).message)
    }
    
    @Test
    fun `validate returns Valid for alphanumeric token of minimum length`() {
        val validToken = "abcdefghijklmnopqrst" // Exactly 20 characters
        val result = validator.validate(validToken)
        
        assertTrue(result is TokenValidationResult.Valid)
    }
    
    @Test
    fun `validate returns Valid for long alphanumeric token`() {
        val validToken = "abcdefghijklmnopqrstuvwxyz123456789"
        val result = validator.validate(validToken)
        
        assertTrue(result is TokenValidationResult.Valid)
    }
    
    @Test
    fun `validate returns Valid for token with dashes and underscores`() {
        val validToken = "abc-def_ghi-jkl_mnopqrst"
        val result = validator.validate(validToken)
        
        assertTrue(result is TokenValidationResult.Valid)
    }
    
    @Test
    fun `validate returns Valid for uppercase token`() {
        val validToken = "ABCDEFGHIJKLMNOPQRST"
        val result = validator.validate(validToken)
        
        assertTrue(result is TokenValidationResult.Valid)
    }
    
    @Test
    fun `validate returns Valid for mixed case token`() {
        val validToken = "AbCdEfGhIjKlMnOpQrSt"
        val result = validator.validate(validToken)
        
        assertTrue(result is TokenValidationResult.Valid)
    }
}
