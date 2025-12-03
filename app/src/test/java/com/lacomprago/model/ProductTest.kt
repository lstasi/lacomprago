package com.lacomprago.model

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Product data model
 */
class ProductTest {
    
    private val gson = Gson()
    
    @Test
    fun `Product data class equality works correctly`() {
        val product1 = Product(
            id = "prod_123",
            name = "Milk 1L",
            frequency = 5,
            lastPurchase = 1234567890L,
            category = "Dairy",
            totalQuantity = 10.0
        )
        val product2 = Product(
            id = "prod_123",
            name = "Milk 1L",
            frequency = 5,
            lastPurchase = 1234567890L,
            category = "Dairy",
            totalQuantity = 10.0
        )
        val product3 = Product(
            id = "prod_456",
            name = "Bread",
            frequency = 3,
            lastPurchase = 1234567800L,
            category = "Bakery",
            totalQuantity = 3.0
        )
        
        assertEquals(product1, product2)
        assertNotEquals(product1, product3)
    }
    
    @Test
    fun `Product copy works correctly`() {
        val original = Product(
            id = "prod_123",
            name = "Milk 1L",
            frequency = 5,
            lastPurchase = 1234567890L,
            category = "Dairy",
            totalQuantity = 10.0
        )
        
        val modified = original.copy(frequency = 6)
        
        assertEquals(5, original.frequency)
        assertEquals(6, modified.frequency)
        assertEquals(original.id, modified.id)
        assertEquals(original.name, modified.name)
    }
    
    @Test
    fun `Product serialization to JSON works correctly`() {
        val product = Product(
            id = "prod_123",
            name = "Milk 1L",
            frequency = 5,
            lastPurchase = 1234567890L,
            category = "Dairy",
            totalQuantity = 10.0
        )
        
        val json = gson.toJson(product)
        val deserialized = gson.fromJson(json, Product::class.java)
        
        assertEquals(product, deserialized)
    }
    
    @Test
    fun `Product deserialization from JSON works correctly`() {
        val json = """
            {
                "id": "prod_123",
                "name": "Milk 1L",
                "frequency": 24,
                "lastPurchase": 1698765432000,
                "category": "Dairy",
                "totalQuantity": 48.0
            }
        """.trimIndent()
        
        val product = gson.fromJson(json, Product::class.java)
        
        assertEquals("prod_123", product.id)
        assertEquals("Milk 1L", product.name)
        assertEquals(24, product.frequency)
        assertEquals(1698765432000L, product.lastPurchase)
        assertEquals("Dairy", product.category)
        assertEquals(48.0, product.totalQuantity, 0.01)
    }
    
    @Test
    fun `Product with null category serializes correctly`() {
        val product = Product(
            id = "prod_123",
            name = "Milk 1L",
            frequency = 5,
            lastPurchase = 1234567890L,
            category = null,
            totalQuantity = 10.0
        )
        
        val json = gson.toJson(product)
        val deserialized = gson.fromJson(json, Product::class.java)
        
        assertEquals(product, deserialized)
        assertNull(deserialized.category)
    }
    
    @Test
    fun `Product default values work correctly`() {
        val product = Product(
            id = "prod_123",
            name = "Test Product",
            frequency = 1,
            lastPurchase = 1234567890L
        )
        
        assertNull(product.category)
        assertEquals(0.0, product.totalQuantity, 0.01)
    }
}
