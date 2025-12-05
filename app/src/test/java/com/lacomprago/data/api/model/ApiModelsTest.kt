package com.lacomprago.data.api.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for API model serialization/deserialization.
 */
class ApiModelsTest {
    
    private val gson = Gson()
    
    // OrderListResponse Tests (based on fixtures/orders.json)
    
    @Test
    fun `OrderListResponse deserialization with next_page works correctly`() {
        val json = """
            {
                "next_page": "https://api.example.com/orders?page=2",
                "results": []
            }
        """.trimIndent()
        
        val response = gson.fromJson(json, OrderListResponse::class.java)
        
        assertEquals("https://api.example.com/orders?page=2", response.nextPage)
        assertEquals(0, response.results.size)
    }
    
    @Test
    fun `OrderListResponse deserialization with null next_page works correctly`() {
        val json = """
            {
                "next_page": null,
                "results": []
            }
        """.trimIndent()
        
        val response = gson.fromJson(json, OrderListResponse::class.java)
        
        assertNull(response.nextPage)
        assertEquals(0, response.results.size)
    }
    
    // OrderResult Tests (based on fixtures/orders.json)
    
    @Test
    fun `OrderResult deserialization from API JSON format works correctly`() {
        val json = """
            {
                "id": 12895962,
                "order_id": 12895962,
                "address": {
                    "id": 1237917,
                    "address": "Carrer de Test, 14",
                    "address_detail": "Bajos 1º",
                    "town": "Barcelona",
                    "comments": "Test comments",
                    "entered_manually": false,
                    "latitude": "41.38747300",
                    "longitude": "2.06084220",
                    "permanent_address": true,
                    "postal_code": "08960"
                },
                "changes_until": "2022-10-19T20:59:59Z",
                "customer_phone": "+34600000000",
                "end_date": "2022-10-20T18:00:00Z",
                "final_price": true,
                "payment_method": {
                    "id": 998434,
                    "credit_card_type": 1,
                    "credit_card_number": "8012",
                    "expires_month": "06",
                    "expires_year": "2024",
                    "default_card": true,
                    "expiration_status": "expired"
                },
                "payment_status": 1,
                "phone_country_code": "34",
                "phone_national_number": "600000000",
                "price": "120.89",
                "products_count": 32,
                "slot": {
                    "id": "264606",
                    "start": "2022-10-20T17:00:00Z",
                    "end": "2022-10-20T18:00:00Z",
                    "price": "7.21",
                    "available": true,
                    "cutoff_time": "2022-10-19T20:59:59Z",
                    "timezone": "Europe/Madrid"
                },
                "slot_size": 1,
                "start_date": "2022-10-20T17:00:00Z",
                "status": 6,
                "status_ui": "delivered",
                "summary": {
                    "products": "120.89",
                    "slot": "7.21",
                    "slot_bonus": null,
                    "total": "128.10",
                    "taxes": "11.03",
                    "tax_type": "iva",
                    "tax_base": "117.07",
                    "volume_extra_cost": {
                        "threshold": 70,
                        "cost_by_extra_liter": "0.1",
                        "total_extra_liters": 0.0,
                        "total": "0.00"
                    }
                },
                "service_rating_token": "464388fd-0db6-4008-a968-ffea65cad806",
                "click_and_collect": false,
                "warehouse_code": "bcn1",
                "last_edit_message": null,
                "timezone": "Europe/Madrid"
            }
        """.trimIndent()
        
        val order = gson.fromJson(json, OrderResult::class.java)
        
        assertEquals(12895962L, order.id)
        assertEquals(12895962L, order.orderId)
        assertEquals("120.89", order.price)
        assertEquals(32, order.productsCount)
        assertEquals(6, order.status)
        assertEquals("delivered", order.statusUi)
        assertEquals("bcn1", order.warehouseCode)
        assertEquals(false, order.clickAndCollect)
        
        // Address assertions
        assertEquals("Carrer de Test, 14", order.address.address)
        assertEquals("Barcelona", order.address.town)
        assertEquals("08960", order.address.postalCode)
        
        // Payment method assertions
        assertEquals(998434L, order.paymentMethod.id)
        assertEquals("8012", order.paymentMethod.creditCardNumber)
        assertEquals("expired", order.paymentMethod.expirationStatus)
        
        // Slot assertions
        assertEquals("264606", order.slot.id)
        assertEquals("7.21", order.slot.price)
        
        // Summary assertions
        assertEquals("128.10", order.summary.total)
        assertEquals("iva", order.summary.taxType)
        assertEquals(70, order.summary.volumeExtraCost.threshold)
    }
    
    // OrderAddress Tests
    
    @Test
    fun `OrderAddress deserialization works correctly`() {
        val json = """
            {
                "id": 1237917,
                "address": "Carrer de Emilia Guàrdia i Arbusi, 14",
                "address_detail": "Bajos 1º",
                "town": "Sant Just Desvern",
                "comments": "Entrance by side street",
                "entered_manually": false,
                "latitude": "41.38747300",
                "longitude": "2.06084220",
                "permanent_address": true,
                "postal_code": "08960"
            }
        """.trimIndent()
        
        val address = gson.fromJson(json, OrderAddress::class.java)
        
        assertEquals(1237917L, address.id)
        assertEquals("Carrer de Emilia Guàrdia i Arbusi, 14", address.address)
        assertEquals("Bajos 1º", address.addressDetail)
        assertEquals("Sant Just Desvern", address.town)
        assertEquals("08960", address.postalCode)
        assertEquals(false, address.enteredManually)
        assertEquals(true, address.permanentAddress)
    }
    
    // PaymentMethod Tests
    
    @Test
    fun `PaymentMethod deserialization works correctly`() {
        val json = """
            {
                "id": 998434,
                "credit_card_type": 1,
                "credit_card_number": "8012",
                "expires_month": "06",
                "expires_year": "2024",
                "default_card": true,
                "expiration_status": "expired"
            }
        """.trimIndent()
        
        val paymentMethod = gson.fromJson(json, PaymentMethod::class.java)
        
        assertEquals(998434L, paymentMethod.id)
        assertEquals(1, paymentMethod.creditCardType)
        assertEquals("8012", paymentMethod.creditCardNumber)
        assertEquals("06", paymentMethod.expiresMonth)
        assertEquals("2024", paymentMethod.expiresYear)
        assertEquals(true, paymentMethod.defaultCard)
        assertEquals("expired", paymentMethod.expirationStatus)
    }
    
    // DeliverySlot Tests
    
    @Test
    fun `DeliverySlot deserialization works correctly`() {
        val json = """
            {
                "id": "264606",
                "start": "2022-10-20T17:00:00Z",
                "end": "2022-10-20T18:00:00Z",
                "price": "7.21",
                "available": true,
                "cutoff_time": "2022-10-19T20:59:59Z",
                "timezone": "Europe/Madrid"
            }
        """.trimIndent()
        
        val slot = gson.fromJson(json, DeliverySlot::class.java)
        
        assertEquals("264606", slot.id)
        assertEquals("2022-10-20T17:00:00Z", slot.start)
        assertEquals("2022-10-20T18:00:00Z", slot.end)
        assertEquals("7.21", slot.price)
        assertEquals(true, slot.available)
        assertEquals("Europe/Madrid", slot.timezone)
    }
    
    // OrderSummaryDetails Tests
    
    @Test
    fun `OrderSummaryDetails deserialization works correctly`() {
        val json = """
            {
                "products": "120.89",
                "slot": "7.21",
                "slot_bonus": null,
                "total": "128.10",
                "taxes": "11.03",
                "tax_type": "iva",
                "tax_base": "117.07",
                "volume_extra_cost": {
                    "threshold": 70,
                    "cost_by_extra_liter": "0.1",
                    "total_extra_liters": 0.0,
                    "total": "0.00"
                }
            }
        """.trimIndent()
        
        val summary = gson.fromJson(json, OrderSummaryDetails::class.java)
        
        assertEquals("120.89", summary.products)
        assertEquals("7.21", summary.slot)
        assertNull(summary.slotBonus)
        assertEquals("128.10", summary.total)
        assertEquals("11.03", summary.taxes)
        assertEquals("iva", summary.taxType)
        assertEquals("117.07", summary.taxBase)
        assertEquals(70, summary.volumeExtraCost.threshold)
        assertEquals("0.1", summary.volumeExtraCost.costByExtraLiter)
        assertEquals(0.0, summary.volumeExtraCost.totalExtraLiters, 0.001)
        assertEquals("0.00", summary.volumeExtraCost.total)
    }
    
    // ValidateTokenResponse Tests
    
    @Test
    fun `ValidateTokenResponse with valid true deserializes correctly`() {
        val json = """{"valid":true}"""
        
        val response = gson.fromJson(json, ValidateTokenResponse::class.java)
        
        assertEquals(true, response.valid)
    }
    
    @Test
    fun `ValidateTokenResponse with valid false deserializes correctly`() {
        val json = """{"valid":false}"""
        
        val response = gson.fromJson(json, ValidateTokenResponse::class.java)
        
        assertEquals(false, response.valid)
    }
    
    // CartItemRequest Tests
    
    @Test
    fun `CartItemRequest serialization works correctly`() {
        val cartItem = CartItemRequest(
            productId = "prod_123",
            quantity = 2
        )
        
        val json = gson.toJson(cartItem)
        val deserialized = gson.fromJson(json, CartItemRequest::class.java)
        
        assertEquals(cartItem.productId, deserialized.productId)
        assertEquals(cartItem.quantity, deserialized.quantity)
    }
    
    // CartRequest Tests
    
    @Test
    fun `CartRequest serialization produces correct JSON`() {
        val cartRequest = CartRequest(
            items = listOf(
                CartItemRequest("prod_123", 2),
                CartItemRequest("prod_456", 1)
            )
        )
        
        val json = gson.toJson(cartRequest)
        
        // Verify the JSON contains expected content
        assert(json.contains("prod_123"))
        assert(json.contains("prod_456"))
        assert(json.contains("\"quantity\":2"))
        assert(json.contains("\"quantity\":1"))
    }
    
    @Test
    fun `CartRequest with empty items serializes correctly`() {
        val cartRequest = CartRequest(items = emptyList())
        
        val json = gson.toJson(cartRequest)
        val deserialized = gson.fromJson(json, CartRequest::class.java)
        
        assertEquals(0, deserialized.items.size)
    }
    
    // CartResponse Tests
    
    @Test
    fun `CartResponse deserialization works correctly`() {
        val json = """
            {
              "cartId": "cart_789",
              "status": "CREATED",
              "createdAt": "2024-10-28T12:00:00Z"
            }
        """.trimIndent()
        
        val response = gson.fromJson(json, CartResponse::class.java)
        
        assertEquals("cart_789", response.cartId)
        assertEquals("CREATED", response.status)
        assertEquals("2024-10-28T12:00:00Z", response.createdAt)
    }
    
    @Test
    fun `CartResponse serialization works correctly`() {
        val cartResponse = CartResponse(
            cartId = "cart_abc",
            status = "PENDING",
            createdAt = "2024-11-01T09:00:00Z"
        )
        
        val json = gson.toJson(cartResponse)
        val deserialized = gson.fromJson(json, CartResponse::class.java)
        
        assertEquals(cartResponse.cartId, deserialized.cartId)
        assertEquals(cartResponse.status, deserialized.status)
        assertEquals(cartResponse.createdAt, deserialized.createdAt)
    }
}
