package com.lacomprago.model

import com.lacomprago.data.api.model.CartProduct
import com.lacomprago.data.api.model.RecommendationItem
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for RecommendationsState sealed class
 */
class RecommendationsStateTest {
    
    @Test
    fun `Loading state is singleton`() {
        val state1 = RecommendationsState.Loading
        val state2 = RecommendationsState.Loading
        
        assertSame(state1, state2)
    }
    
    @Test
    fun `Empty state is singleton`() {
        val state1 = RecommendationsState.Empty
        val state2 = RecommendationsState.Empty
        
        assertSame(state1, state2)
    }
    
    @Test
    fun `Success state contains recommendation items and local products`() {
        val precisionItems = listOf(
            createRecommendationItem("prod_1", "Product 1", 2),
            createRecommendationItem("prod_2", "Product 2", 1)
        )
        
        val recallItems = listOf(
            createRecommendationItem("prod_3", "Product 3", 1)
        )
        
        val localProducts = listOf(
            Product("prod_1", "Product 1", 5, 1234567890L, "Category 1", 10.0)
        )
        
        val state = RecommendationsState.Success(
            precisionItems = precisionItems,
            recallItems = recallItems,
            localProducts = localProducts
        )
        
        assertEquals(2, state.precisionItems.size)
        assertEquals(1, state.recallItems.size)
        assertEquals(1, state.localProducts.size)
        assertEquals("prod_1", state.precisionItems[0].product.id)
        assertEquals("Product 2", state.precisionItems[1].product.displayName)
    }
    
    @Test
    fun `Success state with empty lists`() {
        val state = RecommendationsState.Success(
            precisionItems = emptyList(),
            recallItems = emptyList(),
            localProducts = emptyList()
        )
        
        assertTrue(state.precisionItems.isEmpty())
        assertTrue(state.recallItems.isEmpty())
        assertTrue(state.localProducts.isEmpty())
    }
    
    @Test
    fun `Error state contains message`() {
        val errorMessage = "Failed to load recommendations"
        val state = RecommendationsState.Error(errorMessage)
        
        assertEquals(errorMessage, state.message)
    }
    
    @Test
    fun `Error states with same message are equal`() {
        val state1 = RecommendationsState.Error("Error message")
        val state2 = RecommendationsState.Error("Error message")
        
        assertEquals(state1, state2)
    }
    
    @Test
    fun `Error states with different messages are not equal`() {
        val state1 = RecommendationsState.Error("Error 1")
        val state2 = RecommendationsState.Error("Error 2")
        
        assertNotEquals(state1, state2)
    }
    
    @Test
    fun `When expression handles all states`() {
        val precisionItems = listOf(createRecommendationItem("prod_1", "Product 1", 1))
        val localProducts = listOf(Product("prod_1", "Product 1", 3, 0L, "Cat", 3.0))
        
        val states = listOf(
            RecommendationsState.Loading,
            RecommendationsState.Empty,
            RecommendationsState.Success(precisionItems, emptyList(), localProducts),
            RecommendationsState.Error("Error")
        )
        
        states.forEach { state ->
            val result = when (state) {
                is RecommendationsState.Loading -> "Loading"
                is RecommendationsState.Empty -> "Empty"
                is RecommendationsState.Success -> "Success with ${state.precisionItems.size} precision items"
                is RecommendationsState.Error -> "Error: ${state.message}"
            }
            
            assertNotNull(result)
        }
    }
    
    // Helper function to create recommendation items for testing
    private fun createRecommendationItem(
        id: String,
        name: String,
        recommendedQuantity: Int
    ): RecommendationItem {
        return RecommendationItem(
            product = CartProduct(
                id = id,
                displayName = name,
                slug = null,
                thumbnail = null,
                packaging = "Unit",
                published = true,
                limit = 999,
                shareUrl = null,
                categories = null,
                priceInstructions = null,
                badges = null
            ),
            recommendedQuantity = recommendedQuantity,
            sellingMethod = 0
        )
    }
}
