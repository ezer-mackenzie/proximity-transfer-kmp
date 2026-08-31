package io.github.ezer_mackenzie.proximitytransfer.core.transfer.progress

/**
 * Calculates transfer progress metrics (percentage, speed, and ETA) given total payload size and elapsed time.
 */
class TransferProgressCalculator(
    val totalBytes: Long,
    private val currentTimeProviderMs: () -> Long = { getCurrentTimeMillis() },
) {
    private val startTimeMs: Long = currentTimeProviderMs()

    init {
        require(totalBytes >= 0) { "totalBytes cannot be negative" }
    }

    /**
     * Calculates [TransferProgressMetrics] for [bytesTransferred].
     */
    fun calculate(bytesTransferred: Long): TransferProgressMetrics {
        require(bytesTransferred in 0..totalBytes) { "bytesTransferred ($bytesTransferred) out of bounds [0, $totalBytes]" }

        val percentage = if (totalBytes == 0L) 100.0 else (bytesTransferred.toDouble() / totalBytes.toDouble()) * 100.0
        val elapsedMs = (currentTimeProviderMs() - startTimeMs).coerceAtLeast(1L)
        val bytesPerSecond = (bytesTransferred.toDouble() / (elapsedMs.toDouble() / 1000.0))

        val remainingBytes = totalBytes - bytesTransferred
        val etaSeconds = if (bytesPerSecond > 0.0 && remainingBytes > 0) {
            (remainingBytes / bytesPerSecond).toLong()
        } else if (remainingBytes == 0L) {
            0L
        } else {
            null
        }

        return TransferProgressMetrics(
            bytesTransferred = bytesTransferred,
            totalBytes = totalBytes,
            progressPercentage = percentage,
            bytesPerSecond = bytesPerSecond,
            estimatedRemainingSeconds = etaSeconds,
        )
    }
}

internal expect fun getCurrentTimeMillis(): Long
