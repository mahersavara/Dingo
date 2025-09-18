package io.sukhuat.dingo.ui.screens.profile

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ProfilePerformanceMonitor
 */
class ProfilePerformanceMonitorTest {

    private lateinit var performanceMonitor: ProfilePerformanceMonitor

    @Before
    fun setup() {
        performanceMonitor = ProfilePerformanceMonitor()
    }

    @Test
    fun `startOperation should return unique operation ID`() {
        val operationId1 = performanceMonitor.startOperation("test_operation")
        val operationId2 = performanceMonitor.startOperation("test_operation")

        assertNotEquals(operationId1, operationId2)
        assertTrue(operationId1.startsWith("test_operation_"))
        assertTrue(operationId2.startsWith("test_operation_"))
    }

    @Test
    fun `endOperation should record metrics correctly`() {
        val operationId = performanceMonitor.startOperation("test_operation")
        Thread.sleep(10) // Small delay to ensure duration > 0
        performanceMonitor.endOperation(operationId, success = true, itemCount = 5)

        val metrics = performanceMonitor.getMetrics("test_operation")
        assertNotNull(metrics)
        assertEquals(1, metrics!!.totalOperations)
        assertEquals(1, metrics.successfulOperations)
        assertEquals(0, metrics.failedOperations)
        assertEquals(5, metrics.totalItems)
        assertTrue(metrics.lastDuration > 0)
        assertTrue(metrics.averageDuration > 0)
    }

    @Test
    fun `endOperation should handle failed operations`() {
        val operationId = performanceMonitor.startOperation("test_operation")
        performanceMonitor.endOperation(operationId, success = false, itemCount = 1)

        val metrics = performanceMonitor.getMetrics("test_operation")
        assertNotNull(metrics)
        assertEquals(1, metrics!!.totalOperations)
        assertEquals(0, metrics.successfulOperations)
        assertEquals(1, metrics.failedOperations)
    }

    @Test
    fun `multiple operations should aggregate metrics correctly`() {
        // First operation
        val operationId1 = performanceMonitor.startOperation("test_operation")
        Thread.sleep(5)
        performanceMonitor.endOperation(operationId1, success = true, itemCount = 3)

        // Second operation
        val operationId2 = performanceMonitor.startOperation("test_operation")
        Thread.sleep(15)
        performanceMonitor.endOperation(operationId2, success = true, itemCount = 7)

        val metrics = performanceMonitor.getMetrics("test_operation")
        assertNotNull(metrics)
        assertEquals(2, metrics!!.totalOperations)
        assertEquals(2, metrics.successfulOperations)
        assertEquals(0, metrics.failedOperations)
        assertEquals(10, metrics.totalItems)
        assertTrue(metrics.averageDuration > 0)
        assertTrue(metrics.maxDuration >= metrics.minDuration)
    }

    @Test
    fun `monitorOperation should handle successful operations`() = runTest {
        var operationExecuted = false

        val result = performanceMonitor.monitorOperation("test_operation") {
            delay(10)
            operationExecuted = true
            "success"
        }

        assertEquals("success", result)
        assertTrue(operationExecuted)

        val metrics = performanceMonitor.getMetrics("test_operation")
        assertNotNull(metrics)
        assertEquals(1, metrics!!.totalOperations)
        assertEquals(1, metrics.successfulOperations)
        assertEquals(0, metrics.failedOperations)
    }

    @Test
    fun `monitorOperation should handle failed operations`() = runTest {
        val exception = RuntimeException("Test exception")

        try {
            performanceMonitor.monitorOperation("test_operation") {
                delay(10)
                throw exception
            }
            fail("Expected exception to be thrown")
        } catch (e: RuntimeException) {
            assertEquals(exception, e)
        }

        val metrics = performanceMonitor.getMetrics("test_operation")
        assertNotNull(metrics)
        assertEquals(1, metrics!!.totalOperations)
        assertEquals(0, metrics.successfulOperations)
        assertEquals(1, metrics.failedOperations)
    }

    @Test
    fun `getAllMetrics should return all recorded metrics`() {
        performanceMonitor.endOperation(
            performanceMonitor.startOperation("operation1"),
            success = true
        )
        performanceMonitor.endOperation(
            performanceMonitor.startOperation("operation2"),
            success = true
        )

        val allMetrics = performanceMonitor.getAllMetrics()
        assertEquals(2, allMetrics.size)
        assertTrue(allMetrics.containsKey("operation1"))
        assertTrue(allMetrics.containsKey("operation2"))
    }

    @Test
    fun `resetMetrics should clear specific operation metrics`() {
        performanceMonitor.endOperation(
            performanceMonitor.startOperation("operation1"),
            success = true
        )
        performanceMonitor.endOperation(
            performanceMonitor.startOperation("operation2"),
            success = true
        )

        performanceMonitor.resetMetrics("operation1")

        val allMetrics = performanceMonitor.getAllMetrics()
        assertEquals(1, allMetrics.size)
        assertFalse(allMetrics.containsKey("operation1"))
        assertTrue(allMetrics.containsKey("operation2"))
    }

    @Test
    fun `resetAllMetrics should clear all metrics`() {
        performanceMonitor.endOperation(
            performanceMonitor.startOperation("operation1"),
            success = true
        )
        performanceMonitor.endOperation(
            performanceMonitor.startOperation("operation2"),
            success = true
        )

        performanceMonitor.resetAllMetrics()

        val allMetrics = performanceMonitor.getAllMetrics()
        assertTrue(allMetrics.isEmpty())
    }

    @Test
    fun `getPerformanceSummary should calculate correct summary`() {
        // Add some test operations
        performanceMonitor.endOperation(
            performanceMonitor.startOperation("fast_operation"),
            success = true
        )
        Thread.sleep(20)
        performanceMonitor.endOperation(
            performanceMonitor.startOperation("slow_operation"),
            success = true
        )
        performanceMonitor.endOperation(
            performanceMonitor.startOperation("failed_operation"),
            success = false
        )

        val summary = performanceMonitor.getPerformanceSummary()
        assertEquals(3, summary.totalOperations)
        assertEquals(2, summary.totalSuccessfulOperations)
        assertEquals(1, summary.totalFailedOperations)
        assertTrue(summary.averageOperationTime >= 0)
        assertEquals(3, summary.operationMetrics.size)
    }

    @Test
    fun `getPerformanceAlerts should detect slow operations`() {
        // Simulate a slow operation by manually creating metrics
        val operationId = performanceMonitor.startOperation("slow_operation")
        Thread.sleep(10) // Small delay
        performanceMonitor.endOperation(operationId, success = true)

        // Manually set a high duration to trigger alert
        val metrics = performanceMonitor.getMetrics("slow_operation")
        if (metrics != null) {
            metrics.averageDuration = 3000 // 3 seconds
        }

        val alerts = performanceMonitor.getPerformanceAlerts()
        assertTrue(alerts.any { it.type == AlertType.SLOW_OPERATION })
    }

    @Test
    fun `getPerformanceAlerts should detect high failure rates`() {
        // Create operations with high failure rate
        repeat(10) {
            val operationId = performanceMonitor.startOperation("failing_operation")
            performanceMonitor.endOperation(operationId, success = it < 2) // 80% failure rate
        }

        val alerts = performanceMonitor.getPerformanceAlerts()
        assertTrue(alerts.any { it.type == AlertType.HIGH_FAILURE_RATE })
    }

    @Test
    fun `getMemoryUsage should return valid memory information`() {
        val memoryUsage = performanceMonitor.getMemoryUsage()

        assertTrue(memoryUsage.usedMemoryBytes >= 0)
        assertTrue(memoryUsage.totalMemoryBytes >= memoryUsage.usedMemoryBytes)
        assertTrue(memoryUsage.maxMemoryBytes >= memoryUsage.totalMemoryBytes)
        assertTrue(memoryUsage.freeMemoryBytes >= 0)
        assertTrue(memoryUsage.usagePercentage in 0..100)

        // Test convenience methods
        assertTrue(memoryUsage.getUsedMemoryMB() >= 0)
        assertTrue(memoryUsage.getTotalMemoryMB() >= 0)
        assertTrue(memoryUsage.getMaxMemoryMB() >= 0)
        assertTrue(memoryUsage.getFreeMemoryMB() >= 0)
    }

    @Test
    fun `endOperation with invalid operation ID should not crash`() {
        // This should not throw an exception
        performanceMonitor.endOperation("invalid_operation_id", success = true)

        // Should not create any metrics
        val metrics = performanceMonitor.getMetrics("invalid_operation")
        assertNull(metrics)
    }

    @Test
    fun `metrics should be thread-safe`() {
        val threads = mutableListOf<Thread>()

        // Create multiple threads that perform operations concurrently
        repeat(10) { threadIndex ->
            val thread = Thread {
                repeat(10) { operationIndex ->
                    val operationId = performanceMonitor.startOperation("concurrent_operation")
                    Thread.sleep(1)
                    performanceMonitor.endOperation(operationId, success = true)
                }
            }
            threads.add(thread)
            thread.start()
        }

        // Wait for all threads to complete
        threads.forEach { it.join() }

        val metrics = performanceMonitor.getMetrics("concurrent_operation")
        assertNotNull(metrics)
        assertEquals(100, metrics!!.totalOperations)
        assertEquals(100, metrics.successfulOperations)
        assertEquals(0, metrics.failedOperations)
    }
}
