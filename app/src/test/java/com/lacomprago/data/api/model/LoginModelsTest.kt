package com.lacomprago.data.api.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for LoginRequest and LoginResponse model serialization/deserialization.
 */
class LoginModelsTest {

    private val gson = Gson()

    // LoginRequest Tests

    @Test
    fun `LoginRequest serializes username and password with correct field names`() {
        val request = LoginRequest(
            username = "user@example.com",
            password = "s3cr3t"
        )

        val json = gson.toJson(request)

        assert(json.contains("\"username\"")) { "JSON should contain 'username' key" }
        assert(json.contains("\"password\"")) { "JSON should contain 'password' key" }
        assert(json.contains("user@example.com")) { "JSON should contain the email value" }
        assert(json.contains("s3cr3t")) { "JSON should contain the password value" }
    }

    @Test
    fun `LoginRequest round-trip serialization preserves values`() {
        val original = LoginRequest(
            username = "test@mercadona.es",
            password = "myPassword123"
        )

        val json = gson.toJson(original)
        val deserialized = gson.fromJson(json, LoginRequest::class.java)

        assertEquals(original.username, deserialized.username)
        assertEquals(original.password, deserialized.password)
    }

    @Test
    fun `LoginRequest equality works correctly`() {
        val request1 = LoginRequest("user@test.com", "pass1")
        val request2 = LoginRequest("user@test.com", "pass1")
        val request3 = LoginRequest("other@test.com", "pass1")

        assertEquals(request1, request2)
        assert(request1 != request3)
    }

    // LoginResponse Tests

    @Test
    fun `LoginResponse deserializes access_token and customer_id correctly`() {
        val json = """
            {
                "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test",
                "customer_id": "uuid-1234-abcd"
            }
        """.trimIndent()

        val response = gson.fromJson(json, LoginResponse::class.java)

        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test", response.accessToken)
        assertEquals("uuid-1234-abcd", response.customerId)
    }

    @Test
    fun `LoginResponse round-trip serialization preserves values`() {
        val original = LoginResponse(
            accessToken = "token_abc123",
            customerId = "cust-uuid-9999"
        )

        val json = gson.toJson(original)
        val deserialized = gson.fromJson(json, LoginResponse::class.java)

        assertEquals(original.accessToken, deserialized.accessToken)
        assertEquals(original.customerId, deserialized.customerId)
    }

    @Test
    fun `LoginResponse equality works correctly`() {
        val response1 = LoginResponse("token_xyz", "cust-001")
        val response2 = LoginResponse("token_xyz", "cust-001")
        val response3 = LoginResponse("token_xyz", "cust-002")

        assertEquals(response1, response2)
        assert(response1 != response3)
    }
}
