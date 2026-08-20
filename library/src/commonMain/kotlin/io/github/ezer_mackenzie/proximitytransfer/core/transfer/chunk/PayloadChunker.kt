package io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk

/** Splits one payload into deterministic, zero-based chunks. */
class PayloadChunker(
    val chunkSize: Int = DEFAULT_CHUNK_SIZE,
) {
    init {
        require(chunkSize > 0) { "Chunk size must be positive" }
    }

    fun split(payload: ByteArray): List<PayloadChunk> {
        val chunkCount = if (payload.isEmpty()) 1 else ((payload.size - 1) / chunkSize) + 1

        return List(chunkCount) { index ->
            val start = index * chunkSize
            val end = minOf(start + chunkSize, payload.size)
            PayloadChunk(
                index = index,
                total = chunkCount,
                data = payload.copyOfRange(start, end),
            )
        }
    }

    companion object {
        const val DEFAULT_CHUNK_SIZE: Int = 16 * 1_024
    }
}
