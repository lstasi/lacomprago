package com.lacomprago.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for AuthState sealed class.
 * Verifies that each authentication state holds the correct data
 * and behaves as expected with type checks and pattern matching.
 */
class AuthStateTest {

    @Test
    fun `NoToken is recognized as NoToken state`() {
        val state: AuthState = AuthState.NoToken

        assertTrue(state is AuthState.NoToken)
        assertFalse(state is AuthState.ValidatingToken)
        assertFalse(state is AuthState.TokenValid)
        assertFalse(state is AuthState.TokenInvalid)
    }

    @Test
    fun `ValidatingToken is recognized as ValidatingToken state`() {
        val state: AuthState = AuthState.ValidatingToken

        assertTrue(state is AuthState.ValidatingToken)
        assertFalse(state is AuthState.NoToken)
        assertFalse(state is AuthState.TokenValid)
        assertFalse(state is AuthState.TokenInvalid)
    }

    @Test
    fun `TokenValid holds the correct token value`() {
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test"
        val state: AuthState = AuthState.TokenValid(token)

        assertTrue(state is AuthState.TokenValid)
        assertEquals(token, (state as AuthState.TokenValid).token)
    }

    @Test
    fun `TokenInvalid holds the correct error message`() {
        val message = "Token is invalid or expired"
        val state: AuthState = AuthState.TokenInvalid(message)

        assertTrue(state is AuthState.TokenInvalid)
        assertEquals(message, (state as AuthState.TokenInvalid).message)
    }

    @Test
    fun `TokenValid equality works correctly`() {
        val token = "test_token_abc123"
        val state1 = AuthState.TokenValid(token)
        val state2 = AuthState.TokenValid(token)
        val state3 = AuthState.TokenValid("different_token")

        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `TokenInvalid equality works correctly`() {
        val message = "Network error"
        val state1 = AuthState.TokenInvalid(message)
        val state2 = AuthState.TokenInvalid(message)
        val state3 = AuthState.TokenInvalid("Different error")

        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `when expression covers all AuthState branches`() {
        val states = listOf(
            AuthState.NoToken,
            AuthState.ValidatingToken,
            AuthState.TokenValid("token123"),
            AuthState.TokenInvalid("error")
        )

        states.forEach { state ->
            val result = when (state) {
                is AuthState.NoToken -> "no_token"
                is AuthState.ValidatingToken -> "validating"
                is AuthState.TokenValid -> "valid:${state.token}"
                is AuthState.TokenInvalid -> "invalid:${state.message}"
            }
            assertNotNull(result)
        }
    }

    @Test
    fun `NoToken and ValidatingToken are singletons`() {
        val noToken1: AuthState = AuthState.NoToken
        val noToken2: AuthState = AuthState.NoToken
        assertSame(noToken1, noToken2)

        val validating1: AuthState = AuthState.ValidatingToken
        val validating2: AuthState = AuthState.ValidatingToken
        assertSame(validating1, validating2)
    }
}
