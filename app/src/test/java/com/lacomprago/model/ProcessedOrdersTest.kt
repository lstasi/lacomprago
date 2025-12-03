package com.lacomprago.model

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ProcessedOrders data model
 */
class ProcessedOrdersTest {
    
    private val gson = Gson()
    
    @Test
    fun `ProcessedOrders serialization works correctly`() {
        val processedOrders = ProcessedOrders(
            processedOrderIds = listOf("order_123", "order_456", "order_789"),
            lastProcessedAt = 1698865432000L
        )
        
        val json = gson.toJson(processedOrders)
        val deserialized = gson.fromJson(json, ProcessedOrders::class.java)
        
        assertEquals(processedOrders.processedOrderIds, deserialized.processedOrderIds)
        assertEquals(processedOrders.lastProcessedAt, deserialized.lastProcessedAt)
    }
    
    @Test
    fun `ProcessedOrders deserialization from JSON works correctly`() {
        val json = """
            {
                "processedOrderIds": [
                    "order_123",
                    "order_456",
                    "order_789"
                ],
                "lastProcessedAt": 1698865432000
            }
        """.trimIndent()
        
        val processedOrders = gson.fromJson(json, ProcessedOrders::class.java)
        
        assertEquals(3, processedOrders.processedOrderIds.size)
        assertEquals("order_123", processedOrders.processedOrderIds[0])
        assertEquals("order_456", processedOrders.processedOrderIds[1])
        assertEquals("order_789", processedOrders.processedOrderIds[2])
        assertEquals(1698865432000L, processedOrders.lastProcessedAt)
    }
    
    @Test
    fun `ProcessedOrders with empty list serializes correctly`() {
        val processedOrders = ProcessedOrders(
            processedOrderIds = emptyList(),
            lastProcessedAt = null
        )
        
        val json = gson.toJson(processedOrders)
        val deserialized = gson.fromJson(json, ProcessedOrders::class.java)
        
        assertTrue(deserialized.processedOrderIds.isEmpty())
        assertNull(deserialized.lastProcessedAt)
    }
    
    @Test
    fun `ProcessedOrders default values work correctly`() {
        val processedOrders = ProcessedOrders()
        
        assertTrue(processedOrders.processedOrderIds.isEmpty())
        assertNull(processedOrders.lastProcessedAt)
    }
    
    @Test
    fun `ProcessedOrders equality works correctly`() {
        val orders1 = ProcessedOrders(
            processedOrderIds = listOf("order_1", "order_2"),
            lastProcessedAt = 1698865432000L
        )
        val orders2 = ProcessedOrders(
            processedOrderIds = listOf("order_1", "order_2"),
            lastProcessedAt = 1698865432000L
        )
        val orders3 = ProcessedOrders(
            processedOrderIds = listOf("order_1"),
            lastProcessedAt = 1698865432000L
        )
        
        assertEquals(orders1, orders2)
        assertNotEquals(orders1, orders3)
    }
}
