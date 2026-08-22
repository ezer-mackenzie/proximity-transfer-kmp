package io.github.ezer_mackenzie.proximitytransfer.core.transfer.config

/** Resource limits enforced before sending or accepting a transfer. */
data class TransferLimits(
    val maxPayloadBytes: Long = DEFAULT_MAX_PAYLOAD_BYTES,
    val maxChunkCount: Int = DEFAULT_MAX_CHUNK_COUNT,
) {
    init {
        require(maxPayloadBytes >= 0) { "Maximum payload bytes cannot be negative" }
        require(maxChunkCount > 0) { "Maximum chunk count must be positive" }
    }

    companion object {
        const val DEFAULT_MAX_PAYLOAD_BYTES: Long = 1024L * 1024L * 1024L
        const val DEFAULT_MAX_CHUNK_COUNT: Int = 1_000_000
    }
}
