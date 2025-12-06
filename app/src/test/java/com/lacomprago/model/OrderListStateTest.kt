package com.lacomprago.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for OrderListState sealed class.
 */
class OrderListStateTest {

    @Test
    fun `Loading state is a singleton`() {
        val state1: OrderListState = OrderListState.Loading
        val state2: OrderListState = OrderListState.Loading
        assertEquals(state1, state2)
    }

    @Test
    fun `Success state contains correct values`() {
        val state = OrderListState.Success(
            totalOrders = 50,
            downloadedCount = 30,
            processedCount = 20,
            unprocessedCount = 10,
            fromCache = true,
            totalProducts = 100,
            totalQuantity = 500.0,
            avgFrequency = 5.0,
            lastOrderDate = "Oct 30, 2024"
        )
        
        assertEquals(50, state.totalOrders)
        assertEquals(30, state.downloadedCount)
        assertEquals(20, state.processedCount)
        assertEquals(10, state.unprocessedCount)
        assertEquals(true, state.fromCache)
        assertEquals(100, state.totalProducts)
        assertEquals(500.0, state.totalQuantity, 0.001)
        assertEquals(5.0, state.avgFrequency, 0.001)
        assertEquals("Oct 30, 2024", state.lastOrderDate)
    }

    @Test
    fun `Success state lastOrderDate is null by default`() {
        val state = OrderListState.Success(
            totalOrders = 0,
            downloadedCount = 0,
            processedCount = 0,
            unprocessedCount = 0,
            fromCache = true,
            totalProducts = 0,
            totalQuantity = 0.0,
            avgFrequency = 0.0
        )
        
        assertNull(state.lastOrderDate)
    }

    @Test
    fun `Success state equality works correctly`() {
        val state1 = OrderListState.Success(
            totalOrders = 50,
            downloadedCount = 30,
            processedCount = 20,
            unprocessedCount = 10,
            fromCache = true,
            totalProducts = 100,
            totalQuantity = 500.0,
            avgFrequency = 5.0,
            lastOrderDate = "Oct 30, 2024"
        )
        val state2 = OrderListState.Success(
            totalOrders = 50,
            downloadedCount = 30,
            processedCount = 20,
            unprocessedCount = 10,
            fromCache = true,
            totalProducts = 100,
            totalQuantity = 500.0,
            avgFrequency = 5.0,
            lastOrderDate = "Oct 30, 2024"
        )
        val state3 = OrderListState.Success(
            totalOrders = 100,
            downloadedCount = 30,
            processedCount = 20,
            unprocessedCount = 10,
            fromCache = true,
            totalProducts = 100,
            totalQuantity = 500.0,
            avgFrequency = 5.0,
            lastOrderDate = "Oct 30, 2024"
        )
        
        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `Error state contains correct values`() {
        val state = OrderListState.Error(message = "Network error")
        
        assertEquals("Network error", state.message)
    }

    @Test
    fun `Error state equality works correctly`() {
        val state1 = OrderListState.Error("Error A")
        val state2 = OrderListState.Error("Error A")
        val state3 = OrderListState.Error("Error B")
        
        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `when expression handles all OrderListState subtypes`() {
        val states: List<OrderListState> = listOf(
            OrderListState.Loading,
            OrderListState.Success(
                totalOrders = 50,
                downloadedCount = 30,
                processedCount = 20,
                unprocessedCount = 10,
                fromCache = true,
                totalProducts = 100,
                totalQuantity = 500.0,
                avgFrequency = 5.0
            ),
            OrderListState.Error("Error message")
        )
        
        val results = states.map { state ->
            when (state) {
                is OrderListState.Loading -> "loading"
                is OrderListState.Success -> "success"
                is OrderListState.Error -> "error"
            }
        }
        
        assertEquals(listOf("loading", "success", "error"), results)
    }

    @Test
    fun `Success state copy works correctly`() {
        val original = OrderListState.Success(
            totalOrders = 50,
            downloadedCount = 30,
            processedCount = 20,
            unprocessedCount = 10,
            fromCache = true,
            totalProducts = 100,
            totalQuantity = 500.0,
            avgFrequency = 5.0
        )
        
        val updated = original.copy(processedCount = 25, unprocessedCount = 5)
        
        assertEquals(20, original.processedCount)
        assertEquals(25, updated.processedCount)
        assertEquals(10, original.unprocessedCount)
        assertEquals(5, updated.unprocessedCount)
        assertEquals(original.totalOrders, updated.totalOrders)
    }
}
