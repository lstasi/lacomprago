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
    
    // OrderSummary Tests
    
    @Test
    fun `OrderSummary serialization works correctly`() {
        val orderSummary = OrderSummary(
            id = "order_123",
            orderDate = "2024-10-20T15:30:00Z"
        )
        
        val json = gson.toJson(orderSummary)
        val deserialized = gson.fromJson(json, OrderSummary::class.java)
        
        assertEquals(orderSummary.id, deserialized.id)
        assertEquals(orderSummary.orderDate, deserialized.orderDate)
    }
    
    @Test
    fun `OrderSummary deserialization from JSON works correctly`() {
        val json = """{"id":"order_456","orderDate":"2024-10-15T10:20:00Z"}"""
        
        val orderSummary = gson.fromJson(json, OrderSummary::class.java)
        
        assertEquals("order_456", orderSummary.id)
        assertEquals("2024-10-15T10:20:00Z", orderSummary.orderDate)
    }
    
    // OrderListResponse Tests
    
    @Test
    fun `OrderListResponse serialization works correctly`() {
        val orderList = OrderListResponse(
            orders = listOf(
                OrderSummary("order_123", "2024-10-20T15:30:00Z"),
                OrderSummary("order_456", "2024-10-15T10:20:00Z")
            )
        )
        
        val json = gson.toJson(orderList)
        val deserialized = gson.fromJson(json, OrderListResponse::class.java)
        
        assertEquals(2, deserialized.orders.size)
        assertEquals("order_123", deserialized.orders[0].id)
        assertEquals("order_456", deserialized.orders[1].id)
    }
    
    @Test
    fun `OrderListResponse with empty orders deserializes correctly`() {
        val json = """{"orders":[]}"""
        
        val orderList = gson.fromJson(json, OrderListResponse::class.java)
        
        assertEquals(0, orderList.orders.size)
    }
    
    // OrderItemResponse Tests
    
    @Test
    fun `OrderItemResponse with category serializes correctly`() {
        val item = OrderItemResponse(
            productId = "prod_123",
            productName = "Milk 1L",
            quantity = 2,
            category = "Dairy"
        )
        
        val json = gson.toJson(item)
        val deserialized = gson.fromJson(json, OrderItemResponse::class.java)
        
        assertEquals(item.productId, deserialized.productId)
        assertEquals(item.productName, deserialized.productName)
        assertEquals(item.quantity, deserialized.quantity)
        assertEquals(item.category, deserialized.category)
    }
    
    @Test
    fun `OrderItemResponse without category serializes correctly`() {
        val item = OrderItemResponse(
            productId = "prod_456",
            productName = "Bread",
            quantity = 1
        )
        
        val json = gson.toJson(item)
        val deserialized = gson.fromJson(json, OrderItemResponse::class.java)
        
        assertEquals(item.productId, deserialized.productId)
        assertEquals(item.productName, deserialized.productName)
        assertEquals(item.quantity, deserialized.quantity)
        assertNull(deserialized.category)
    }
    
    @Test
    fun `OrderItemResponse with null category in JSON deserializes correctly`() {
        val json = """{"productId":"prod_789","productName":"Eggs","quantity":12,"category":null}"""
        
        val item = gson.fromJson(json, OrderItemResponse::class.java)
        
        assertEquals("prod_789", item.productId)
        assertEquals("Eggs", item.productName)
        assertEquals(12, item.quantity)
        assertNull(item.category)
    }
    
    // OrderResponse Tests
    
    @Test
    fun `OrderResponse serialization works correctly`() {
        val order = OrderResponse(
            id = "order_123",
            orderNumber = "ORD-2024-001",
            orderDate = "2024-10-20T15:30:00Z",
            totalAmount = 125.50,
            items = listOf(
                OrderItemResponse("prod_123", "Milk 1L", 2, "Dairy"),
                OrderItemResponse("prod_456", "Bread", 1, "Bakery")
            )
        )
        
        val json = gson.toJson(order)
        val deserialized = gson.fromJson(json, OrderResponse::class.java)
        
        assertEquals(order.id, deserialized.id)
        assertEquals(order.orderNumber, deserialized.orderNumber)
        assertEquals(order.orderDate, deserialized.orderDate)
        assertEquals(order.totalAmount, deserialized.totalAmount, 0.001)
        assertEquals(2, deserialized.items.size)
    }
    
    @Test
    fun `OrderResponse deserialization from API JSON format works correctly`() {
        val json = """
            {
              "id": "order_123",
              "orderNumber": "ORD-2024-001",
              "orderDate": "2024-10-20T15:30:00Z",
              "totalAmount": 125.50,
              "items": [
                {
                  "productId": "prod_123",
                  "productName": "Milk 1L",
                  "quantity": 2,
                  "category": "Dairy"
                },
                {
                  "productId": "prod_456",
                  "productName": "Bread",
                  "quantity": 1,
                  "category": "Bakery"
                }
              ]
            }
        """.trimIndent()
        
        val order = gson.fromJson(json, OrderResponse::class.java)
        
        assertEquals("order_123", order.id)
        assertEquals("ORD-2024-001", order.orderNumber)
        assertEquals("2024-10-20T15:30:00Z", order.orderDate)
        assertEquals(125.50, order.totalAmount, 0.001)
        assertEquals(2, order.items.size)
        assertEquals("Milk 1L", order.items[0].productName)
        assertEquals("Bread", order.items[1].productName)
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
