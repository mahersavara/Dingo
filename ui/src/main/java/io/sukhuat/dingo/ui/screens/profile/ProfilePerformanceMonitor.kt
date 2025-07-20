package io.sukhuat.dingo.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Performance monitoring system for profile-related operations
 */
@Singleton
class ProfilePerformanceMonitor @Inject constructor() {

    private val metrics = ConcurrentHashMap<String, PerformanceMetric>()
    private val activeOperations = ConcurrentHashMap<String, Long>()

    /**
     * Start monitoring an operation
     */
    fun startOperation(operationName: String): String {
        val operationId = "${operationName}_${System.currentTimeMillis()}"
        activeOperations[operationId] = System.currentTimeMillis()
        return operationId
    }

    /**
     * End monitoring an operation
     */
    fun endOperation(operationId: String, success: Boolean = true, itemCount: Int = 1) {
        val startTime = activeOperations.remove(operationId) ?: return
        val duration = System.currentTimeMillis() - startTime

        val operationName = operationId.substringBeforeLast("_")
        val metric = metrics.getOrPut(operationName) { PerformanceMetric(operationName) }

        synchronized(metric) {
            metric.totalOperations++
            metric.totalDuration += duration
            metric.averageDuration = metric.totalDuration / metric.totalOperations
            metric.lastDuration = duration
            metric.totalItems += itemCount

            if (success) {
                metric.successfulOperations++
            } else {
                metric.failedOperations++
            }

            if (duration > metric.maxDuration) {
                metric.maxDuration = duration
            }

            if (metric.minDuration == 0L || duration < metric.minDuration) {
                metric.minDuration = duration
            }
        }
    }

    /**
     * Monitor a suspending operation
     */
    suspend inline fun <T> monitorOperation(
        operationName: String,
        itemCount: Int = 1,
        operation: suspend () -> T
    ): T {
        val operationId = startOperation(operationName)
        return try {
            val result = operation()
            endOperation(operationId, success = true, itemCount = itemCount)
            result
        } catch (e: Exception) {
            endOperation(operationId, success = false, itemCount = itemCount)
            throw e
        }
    }

    /**
     * Get performance metrics for an operation
     */
    fun getMetrics(operationName: String): PerformanceMetric? {
        return metrics[operationName]?.copy()
    }

    /**
     * Get all performance metrics
     */
    fun getAllMetrics(): Map<String, PerformanceMetric> {
        return metrics.mapValues { it.value.copy() }
    }

    /**
     * Reset metrics for an operation
     */
    fun resetMetrics(operationName: String) {
        metrics.remove(operationName)
    }

    /**
     * Reset all metrics
     */
    fun resetAllMetrics() {
        metrics.clear()
        activeOperations.clear()
    }

    /**
     * Get performance summary
     */
    fun getPerformanceSummary(): PerformanceSummary {
        val allMetrics = getAllMetrics().values

        return PerformanceSummary(
            totalOperations = allMetrics.sumOf { it.totalOperations },
            totalSuccessfulOperations = allMetrics.sumOf { it.successfulOperations },
            totalFailedOperations = allMetrics.sumOf { it.failedOperations },
            averageOperationTime = if (allMetrics.isNotEmpty()) {
                allMetrics.map { it.averageDuration }.average().toLong()
            } else {
                0L
            },
            slowestOperation = allMetrics.maxByOrNull { it.maxDuration }?.operationName,
            fastestOperation = allMetrics.minByOrNull { it.minDuration }?.operationName,
            operationMetrics = allMetrics.toList()
        )
    }

    /**
     * Check if any operations are performing poorly
     */
    fun getPerformanceAlerts(): List<PerformanceAlert> {
        val alerts = mutableListOf<PerformanceAlert>()

        metrics.values.forEach { metric ->
            // Alert if average duration is over 2 seconds
            if (metric.averageDuration > 2000) {
                alerts.add(
                    PerformanceAlert(
                        type = AlertType.SLOW_OPERATION,
                        operationName = metric.operationName,
                        message = "Operation ${metric.operationName} is taking ${metric.averageDuration}ms on average",
                        severity = if (metric.averageDuration > 5000) AlertSeverity.HIGH else AlertSeverity.MEDIUM
                    )
                )
            }

            // Alert if failure rate is over 10%
            val failureRate = if (metric.totalOperations > 0) {
                (metric.failedOperations.toDouble() / metric.totalOperations) * 100
            } else {
                0.0
            }

            if (failureRate > 10.0) {
                alerts.add(
                    PerformanceAlert(
                        type = AlertType.HIGH_FAILURE_RATE,
                        operationName = metric.operationName,
                        message = "Operation ${metric.operationName} has ${failureRate.toInt()}% failure rate",
                        severity = if (failureRate > 25.0) AlertSeverity.HIGH else AlertSeverity.MEDIUM
                    )
                )
            }

            // Alert if there are too many concurrent operations
            if (activeOperations.keys.count { it.startsWith(metric.operationName) } > 5) {
                alerts.add(
                    PerformanceAlert(
                        type = AlertType.HIGH_CONCURRENCY,
                        operationName = metric.operationName,
                        message = "Too many concurrent ${metric.operationName} operations",
                        severity = AlertSeverity.MEDIUM
                    )
                )
            }
        }

        return alerts
    }

    /**
     * Composable function to monitor screen performance
     */
    @Composable
    fun MonitorScreenPerformance(screenName: String) {
        var screenLoadTime by remember { mutableStateOf(0L) }

        LaunchedEffect(screenName) {
            val startTime = System.currentTimeMillis()
            delay(100) // Wait for initial composition
            screenLoadTime = System.currentTimeMillis() - startTime

            endOperation(
                startOperation("screen_load_$screenName"),
                success = true
            )
        }

        DisposableEffect(screenName) {
            val operationId = startOperation("screen_session_$screenName")

            onDispose {
                endOperation(operationId, success = true)
            }
        }
    }

    /**
     * Monitor memory usage
     */
    fun getMemoryUsage(): MemoryUsage {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()

        return MemoryUsage(
            usedMemoryBytes = usedMemory,
            totalMemoryBytes = totalMemory,
            maxMemoryBytes = maxMemory,
            freeMemoryBytes = freeMemory,
            usagePercentage = (usedMemory.toDouble() / maxMemory * 100).toInt()
        )
    }
}

/**
 * Data class representing performance metrics for an operation
 */
data class PerformanceMetric(
    val operationName: String,
    var totalOperations: Long = 0,
    var successfulOperations: Long = 0,
    var failedOperations: Long = 0,
    var totalDuration: Long = 0,
    var averageDuration: Long = 0,
    var minDuration: Long = 0,
    var maxDuration: Long = 0,
    var lastDuration: Long = 0,
    var totalItems: Int = 0
)

/**
 * Data class representing overall performance summary
 */
data class PerformanceSummary(
    val totalOperations: Long,
    val totalSuccessfulOperations: Long,
    val totalFailedOperations: Long,
    val averageOperationTime: Long,
    val slowestOperation: String?,
    val fastestOperation: String?,
    val operationMetrics: List<PerformanceMetric>
)

/**
 * Data class representing a performance alert
 */
data class PerformanceAlert(
    val type: AlertType,
    val operationName: String,
    val message: String,
    val severity: AlertSeverity,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Types of performance alerts
 */
enum class AlertType {
    SLOW_OPERATION,
    HIGH_FAILURE_RATE,
    HIGH_CONCURRENCY,
    MEMORY_WARNING
}

/**
 * Severity levels for alerts
 */
enum class AlertSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Data class representing memory usage
 */
data class MemoryUsage(
    val usedMemoryBytes: Long,
    val totalMemoryBytes: Long,
    val maxMemoryBytes: Long,
    val freeMemoryBytes: Long,
    val usagePercentage: Int
) {
    fun getUsedMemoryMB(): Long = usedMemoryBytes / (1024 * 1024)
    fun getTotalMemoryMB(): Long = totalMemoryBytes / (1024 * 1024)
    fun getMaxMemoryMB(): Long = maxMemoryBytes / (1024 * 1024)
    fun getFreeMemoryMB(): Long = freeMemoryBytes / (1024 * 1024)
}
