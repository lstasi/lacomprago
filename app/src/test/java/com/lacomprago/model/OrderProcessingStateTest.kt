package com.lacomprago.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for OrderProcessingState sealed class.
 * 
 * Note: To avoid API rate limiting, only ONE order is processed per sync.
 */
class OrderProcessingStateTest {

    @Test
    fun `Idle state is a singleton`() {
        val state1: OrderProcessingState = OrderProcessingState.Idle
        val state2: OrderProcessingState = OrderProcessingState.Idle
        assertEquals(state1, state2)
    }

    @Test
    fun `FetchingOrders state is a singleton`() {
        val state1: OrderProcessingState = OrderProcessingState.FetchingOrders
        val state2: OrderProcessingState = OrderProcessingState.FetchingOrders
        assertEquals(state1, state2)
    }

    @Test
    fun `Processing state contains correct values`() {
        val state = OrderProcessingState.Processing(
            currentOrderId = "order_123",
            remainingOrders = 19
        )
        
        assertEquals("order_123", state.currentOrderId)
        assertEquals(19, state.remainingOrders)
    }

    @Test
    fun `Processing state equality works correctly`() {
        val state1 = OrderProcessingState.Processing("order_123", 19)
        val state2 = OrderProcessingState.Processing("order_123", 19)
        val state3 = OrderProcessingState.Processing("order_456", 19)
        
        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `Completed state contains correct values`() {
        val state = OrderProcessingState.Completed(
            updatedProductCount = 42,
            remainingOrders = 10
        )
        
        assertEquals(42, state.updatedProductCount)
        assertEquals(10, state.remainingOrders)
    }

    @Test
    fun `Completed state has default values`() {
        val state = OrderProcessingState.Completed()
        
        assertEquals(0, state.updatedProductCount)
        assertEquals(0, state.remainingOrders)
    }

    @Test
    fun `Cancelled state is a singleton`() {
        val state1: OrderProcessingState = OrderProcessingState.Cancelled
        val state2: OrderProcessingState = OrderProcessingState.Cancelled
        assertEquals(state1, state2)
    }

    @Test
    fun `Error state contains correct values`() {
        val state = OrderProcessingState.Error(message = "Network error")
        
        assertEquals("Network error", state.message)
    }

    @Test
    fun `when expression handles all OrderProcessingState subtypes`() {
        val states: List<OrderProcessingState> = listOf(
            OrderProcessingState.Idle,
            OrderProcessingState.FetchingOrders,
            OrderProcessingState.Processing("order_1", 9),
            OrderProcessingState.Completed(50, 5),
            OrderProcessingState.Cancelled,
            OrderProcessingState.Error("Error message")
        )
        
        val results = states.map { state ->
            when (state) {
                is OrderProcessingState.Idle -> "idle"
                is OrderProcessingState.FetchingOrders -> "fetching"
                is OrderProcessingState.Processing -> "processing"
                is OrderProcessingState.Completed -> "completed"
                is OrderProcessingState.Cancelled -> "cancelled"
                is OrderProcessingState.Error -> "error"
            }
        }
        
        assertEquals(listOf("idle", "fetching", "processing", "completed", "cancelled", "error"), results)
    }

    @Test
    fun `Processing state copy works correctly`() {
        val original = OrderProcessingState.Processing("order_123", 19)
        val updated = original.copy(remainingOrders = 18)
        
        assertEquals(19, original.remainingOrders)
        assertEquals(18, updated.remainingOrders)
        assertEquals(original.currentOrderId, updated.currentOrderId)
    }

    @Test
    fun `Completed state copy works correctly`() {
        val original = OrderProcessingState.Completed(50, 5)
        val updated = original.copy(remainingOrders = 4)
        
        assertEquals(5, original.remainingOrders)
        assertEquals(4, updated.remainingOrders)
        assertEquals(original.updatedProductCount, updated.updatedProductCount)
    }
}
