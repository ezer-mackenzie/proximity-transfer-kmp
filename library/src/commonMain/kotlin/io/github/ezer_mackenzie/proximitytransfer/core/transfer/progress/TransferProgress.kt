package io.github.ezer_mackenzie.proximitytransfer.core.transfer.progress

/** Immutable payload-byte progress for one transfer. */
data class TransferProgress(
    val transferredBytes: Long,
    val totalBytes: Long,
) {
    init {
        require(totalBytes >= 0) { "Total bytes cannot be negative" }
        require(transferredBytes in 0..totalBytes) {
            "Transferred bytes must be between zero and the total"
        }
    }

    /** Fraction in the inclusive range 0.0 to 1.0. An empty payload is complete. */
    val fraction: Double
        get() = if (totalBytes == 0L) 1.0 else transferredBytes.toDouble() / totalBytes

    val isComplete: Boolean
        get() = transferredBytes == totalBytes
}
