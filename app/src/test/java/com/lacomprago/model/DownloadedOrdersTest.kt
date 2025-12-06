package com.lacomprago.model

import com.lacomprago.data.api.model.OrderDetailsResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for DownloadedOrders data class.
 */
class DownloadedOrdersTest {

    @Test
    fun `default constructor creates empty DownloadedOrders`() {
        val downloadedOrders = DownloadedOrders()
        
        assertTrue(downloadedOrders.downloadedOrderIds.isEmpty())
        assertTrue(downloadedOrders.orderDetails.isEmpty())
        assertNull(downloadedOrders.lastDownloadedAt)
    }

    @Test
    fun `constructor with values works correctly`() {
        val orderIds = setOf("order_1", "order_2")
        val details = mapOf(
            "order_1" to createMockOrderDetails(1L),
            "order_2" to createMockOrderDetails(2L)
        )
        val timestamp = 1234567890L
        
        val downloadedOrders = DownloadedOrders(
            downloadedOrderIds = orderIds,
            orderDetails = details,
            lastDownloadedAt = timestamp
        )
        
        assertEquals(2, downloadedOrders.downloadedOrderIds.size)
        assertEquals(2, downloadedOrders.orderDetails.size)
        assertEquals(timestamp, downloadedOrders.lastDownloadedAt)
    }

    @Test
    fun `copy works correctly`() {
        val original = DownloadedOrders(
            downloadedOrderIds = setOf("order_1"),
            orderDetails = mapOf("order_1" to createMockOrderDetails(1L)),
            lastDownloadedAt = 1000L
        )
        
        val updated = original.copy(
            downloadedOrderIds = original.downloadedOrderIds + "order_2",
            lastDownloadedAt = 2000L
        )
        
        assertEquals(1, original.downloadedOrderIds.size)
        assertEquals(2, updated.downloadedOrderIds.size)
        assertEquals(1000L, original.lastDownloadedAt)
        assertEquals(2000L, updated.lastDownloadedAt)
    }

    @Test
    fun `adding orders to set works correctly`() {
        var downloadedOrders = DownloadedOrders()
        
        // Add first order
        downloadedOrders = downloadedOrders.copy(
            downloadedOrderIds = downloadedOrders.downloadedOrderIds + "order_1"
        )
        assertEquals(1, downloadedOrders.downloadedOrderIds.size)
        assertTrue("order_1" in downloadedOrders.downloadedOrderIds)
        
        // Add second order
        downloadedOrders = downloadedOrders.copy(
            downloadedOrderIds = downloadedOrders.downloadedOrderIds + "order_2"
        )
        assertEquals(2, downloadedOrders.downloadedOrderIds.size)
        assertTrue("order_2" in downloadedOrders.downloadedOrderIds)
    }

    @Test
    fun `equality works correctly`() {
        val orders1 = DownloadedOrders(
            downloadedOrderIds = setOf("order_1"),
            orderDetails = emptyMap(),
            lastDownloadedAt = 1000L
        )
        val orders2 = DownloadedOrders(
            downloadedOrderIds = setOf("order_1"),
            orderDetails = emptyMap(),
            lastDownloadedAt = 1000L
        )
        
        assertEquals(orders1, orders2)
    }

    private fun createMockOrderDetails(id: Long): OrderDetailsResponse {
        return OrderDetailsResponse(
            id = id,
            orderId = id,
            lines = null,
            address = null,
            startDate = null,
            endDate = null,
            price = null,
            productsCount = null,
            status = null,
            statusUi = null,
            summary = null,
            warehouseCode = null
        )
    }
}
