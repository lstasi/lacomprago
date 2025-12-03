package com.lacomprago.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for OrderProcessingState sealed class.
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
            currentOrder = 5,
            totalOrders = 20,
            currentOrderId = "order_123"
        )
        
        assertEquals(5, state.currentOrder)
        assertEquals(20, state.totalOrders)
        assertEquals("order_123", state.currentOrderId)
    }

    @Test
    fun `Processing state equality works correctly`() {
        val state1 = OrderProcessingState.Processing(5, 20, "order_123")
        val state2 = OrderProcessingState.Processing(5, 20, "order_123")
        val state3 = OrderProcessingState.Processing(6, 20, "order_123")
        
        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `Completed state contains correct values`() {
        val state = OrderProcessingState.Completed(
            processedCount = 15,
            updatedProductCount = 42
        )
        
        assertEquals(15, state.processedCount)
        assertEquals(42, state.updatedProductCount)
    }

    @Test
    fun `Completed state has default updatedProductCount of 0`() {
        val state = OrderProcessingState.Completed(processedCount = 10)
        
        assertEquals(10, state.processedCount)
        assertEquals(0, state.updatedProductCount)
    }

    @Test
    fun `Cancelled state contains correct values`() {
        val state = OrderProcessingState.Cancelled(processedCount = 12)
        
        assertEquals(12, state.processedCount)
    }

    @Test
    fun `Error state contains correct values`() {
        val state = OrderProcessingState.Error(
            message = "Network error",
            processedCount = 8
        )
        
        assertEquals("Network error", state.message)
        assertEquals(8, state.processedCount)
    }

    @Test
    fun `when expression handles all OrderProcessingState subtypes`() {
        val states: List<OrderProcessingState> = listOf(
            OrderProcessingState.Idle,
            OrderProcessingState.FetchingOrders,
            OrderProcessingState.Processing(1, 10, "order_1"),
            OrderProcessingState.Completed(10, 50),
            OrderProcessingState.Cancelled(5),
            OrderProcessingState.Error("Error message", 3)
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
        val original = OrderProcessingState.Processing(5, 20, "order_123")
        val updated = original.copy(currentOrder = 6)
        
        assertEquals(5, original.currentOrder)
        assertEquals(6, updated.currentOrder)
        assertEquals(original.totalOrders, updated.totalOrders)
        assertEquals(original.currentOrderId, updated.currentOrderId)
    }

    @Test
    fun `Completed state copy works correctly`() {
        val original = OrderProcessingState.Completed(10, 50)
        val updated = original.copy(processedCount = 15)
        
        assertEquals(10, original.processedCount)
        assertEquals(15, updated.processedCount)
        assertEquals(original.updatedProductCount, updated.updatedProductCount)
    }
}
