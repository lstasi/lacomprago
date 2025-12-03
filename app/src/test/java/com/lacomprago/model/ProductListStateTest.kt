package com.lacomprago.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ProductListState sealed class
 */
class ProductListStateTest {
    
    @Test
    fun `Loading state is singleton`() {
        val state1 = ProductListState.Loading
        val state2 = ProductListState.Loading
        
        assertSame(state1, state2)
    }
    
    @Test
    fun `Empty state is singleton`() {
        val state1 = ProductListState.Empty
        val state2 = ProductListState.Empty
        
        assertSame(state1, state2)
    }
    
    @Test
    fun `Success state contains products list`() {
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
        
        val state = ProductListState.Success(products)
        
        assertEquals(2, state.products.size)
        assertEquals("prod_123", state.products[0].id)
        assertEquals("Bread", state.products[1].name)
    }
    
    @Test
    fun `Success state with empty list`() {
        val state = ProductListState.Success(emptyList())
        
        assertTrue(state.products.isEmpty())
    }
    
    @Test
    fun `Error state contains message`() {
        val errorMessage = "Failed to load products"
        val state = ProductListState.Error(errorMessage)
        
        assertEquals(errorMessage, state.message)
    }
    
    @Test
    fun `Error states with same message are equal`() {
        val state1 = ProductListState.Error("Error message")
        val state2 = ProductListState.Error("Error message")
        
        assertEquals(state1, state2)
    }
    
    @Test
    fun `Error states with different messages are not equal`() {
        val state1 = ProductListState.Error("Error 1")
        val state2 = ProductListState.Error("Error 2")
        
        assertNotEquals(state1, state2)
    }
    
    @Test
    fun `When expression handles all states`() {
        val states = listOf(
            ProductListState.Loading,
            ProductListState.Empty,
            ProductListState.Success(emptyList()),
            ProductListState.Error("Error")
        )
        
        states.forEach { state ->
            val result = when (state) {
                is ProductListState.Loading -> "Loading"
                is ProductListState.Empty -> "Empty"
                is ProductListState.Success -> "Success with ${state.products.size} products"
                is ProductListState.Error -> "Error: ${state.message}"
            }
            
            assertNotNull(result)
        }
    }
}
