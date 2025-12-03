package com.lacomprago.model

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ProductList data model
 */
class ProductListTest {
    
    private val gson = Gson()
    
    @Test
    fun `ProductList serialization works correctly`() {
        val products = listOf(
            Product(
                id = "prod_123",
                name = "Milk 1L",
                frequency = 24,
                lastPurchase = 1698765432000L,
                category = "Dairy",
                totalQuantity = 48.0
            ),
            Product(
                id = "prod_456",
                name = "Bread",
                frequency = 20,
                lastPurchase = 1698851832000L,
                category = "Bakery",
                totalQuantity = 20.0
            )
        )
        val productList = ProductList(
            products = products,
            lastUpdated = 1698865432000L
        )
        
        val json = gson.toJson(productList)
        val deserialized = gson.fromJson(json, ProductList::class.java)
        
        assertEquals(productList.products.size, deserialized.products.size)
        assertEquals(productList.lastUpdated, deserialized.lastUpdated)
        assertEquals(productList.products[0].id, deserialized.products[0].id)
        assertEquals(productList.products[1].name, deserialized.products[1].name)
    }
    
    @Test
    fun `ProductList deserialization from JSON works correctly`() {
        val json = """
            {
                "products": [
                    {
                        "id": "prod_123",
                        "name": "Milk 1L",
                        "frequency": 24,
                        "lastPurchase": 1698765432000,
                        "category": "Dairy",
                        "totalQuantity": 48.0
                    },
                    {
                        "id": "prod_456",
                        "name": "Bread",
                        "frequency": 20,
                        "lastPurchase": 1698851832000,
                        "category": "Bakery",
                        "totalQuantity": 20.0
                    }
                ],
                "lastUpdated": 1698865432000
            }
        """.trimIndent()
        
        val productList = gson.fromJson(json, ProductList::class.java)
        
        assertEquals(2, productList.products.size)
        assertEquals(1698865432000L, productList.lastUpdated)
        assertEquals("prod_123", productList.products[0].id)
        assertEquals("Bread", productList.products[1].name)
    }
    
    @Test
    fun `ProductList with empty products serializes correctly`() {
        val productList = ProductList(
            products = emptyList(),
            lastUpdated = 1698865432000L
        )
        
        val json = gson.toJson(productList)
        val deserialized = gson.fromJson(json, ProductList::class.java)
        
        assertTrue(deserialized.products.isEmpty())
        assertEquals(productList.lastUpdated, deserialized.lastUpdated)
    }
    
    @Test
    fun `ProductList default lastUpdated is set`() {
        val beforeCreation = System.currentTimeMillis()
        val productList = ProductList(products = emptyList())
        val afterCreation = System.currentTimeMillis()
        
        assertTrue(productList.lastUpdated >= beforeCreation)
        assertTrue(productList.lastUpdated <= afterCreation)
    }
}
