package dev.proximitytransfer.core.transfer.chunk

/** Reconstructs a payload after validating that its complete chunk set is present. */
object PayloadReconstructor {
    fun reconstruct(chunks: Collection<PayloadChunk>): ByteArray {
        if (chunks.isEmpty()) {
            throw PayloadReconstructionException("At least one chunk is required")
        }

        val expectedTotal = chunks.first().total
        if (chunks.any { it.total != expectedTotal }) {
            throw PayloadReconstructionException("Chunks declare inconsistent totals")
        }
        if (chunks.size != expectedTotal) {
            throw PayloadReconstructionException(
                "Expected $expectedTotal chunks but received ${chunks.size}",
            )
        }

        val chunksByIndex = chunks.associateBy { it.index }
        if (chunksByIndex.size != chunks.size) {
            throw PayloadReconstructionException("Duplicate chunk index received")
        }
        if ((0 until expectedTotal).any { it !in chunksByIndex }) {
            throw PayloadReconstructionException("One or more chunks are missing")
        }

        val payloadSize = chunks.sumOf { it.size.toLong() }
        if (payloadSize > Int.MAX_VALUE) {
            throw PayloadReconstructionException("Reconstructed payload is too large")
        }

        val payload = ByteArray(payloadSize.toInt())
        var offset = 0
        for (index in 0 until expectedTotal) {
            val data = chunksByIndex.getValue(index).data
            data.copyInto(payload, destinationOffset = offset)
            offset += data.size
        }
        return payload
    }
}
