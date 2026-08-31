package io.github.ezer_mackenzie.proximitytransfer.core.transfer.progress

import kotlin.test.Test
import kotlin.test.assertEquals

class TransferProgressCalculatorTest {

    @Test
    fun testProgressCalculation() {
        var mockTime = 1000L
        val calculator = TransferProgressCalculator(totalBytes = 1000L, currentTimeProviderMs = { mockTime })

        mockTime = 2000L // 1 second elapsed
        val metrics = calculator.calculate(bytesTransferred = 500L)

        assertEquals(500L, metrics.bytesTransferred)
        assertEquals(1000L, metrics.totalBytes)
        assertEquals(50.0, metrics.progressPercentage)
        assertEquals(500.0, metrics.bytesPerSecond)
        assertEquals(1L, metrics.estimatedRemainingSeconds)
    }

    @Test
    fun testCompletionProgress() {
        var mockTime = 1000L
        val calculator = TransferProgressCalculator(totalBytes = 1000L, currentTimeProviderMs = { mockTime })

        mockTime = 2000L
        val metrics = calculator.calculate(bytesTransferred = 1000L)

        assertEquals(100.0, metrics.progressPercentage)
        assertEquals(0L, metrics.estimatedRemainingSeconds)
    }
}
