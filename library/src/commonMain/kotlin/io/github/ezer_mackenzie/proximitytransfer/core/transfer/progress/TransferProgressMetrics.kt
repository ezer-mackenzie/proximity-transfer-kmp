package io.github.ezer_mackenzie.proximitytransfer.core.transfer.progress

/**
 * Snapshot of real-time transfer progress calculations.
 */
data class TransferProgressMetrics(
    val bytesTransferred: Long,
    val totalBytes: Long,
    val progressPercentage: Double,
    val bytesPerSecond: Double,
    val estimatedRemainingSeconds: Long?,
)
