package dev.proximitytransfer.transfer.chunk

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PayloadChunkerTest {
    @Test
    fun splitsAndReconstructsPayload() {
        val payload = ByteArray(10) { it.toByte() }

        val chunks = PayloadChunker(chunkSize = 4).split(payload)

        assertEquals(listOf(4, 4, 2), chunks.map { it.size })
        assertEquals(listOf(0, 1, 2), chunks.map { it.index })
        assertContentEquals(payload, PayloadReconstructor.reconstruct(chunks.reversed()))
    }

    @Test
    fun representsEmptyPayloadAsOneEmptyChunk() {
        val chunks = PayloadChunker().split(byteArrayOf())

        assertEquals(1, chunks.size)
        assertEquals(0, chunks.single().size)
        assertContentEquals(byteArrayOf(), PayloadReconstructor.reconstruct(chunks))
    }

    @Test
    fun rejectsNonPositiveChunkSize() {
        assertFailsWith<IllegalArgumentException> { PayloadChunker(0) }
    }

    @Test
    fun rejectsMissingChunk() {
        val chunks = PayloadChunker(chunkSize = 2).split(byteArrayOf(1, 2, 3, 4))

        assertFailsWith<PayloadReconstructionException> {
            PayloadReconstructor.reconstruct(chunks.dropLast(1))
        }
    }

    @Test
    fun rejectsDuplicateChunk() {
        val chunks = PayloadChunker(chunkSize = 2).split(byteArrayOf(1, 2, 3, 4))

        assertFailsWith<PayloadReconstructionException> {
            PayloadReconstructor.reconstruct(listOf(chunks.first(), chunks.first()))
        }
    }

    @Test
    fun rejectsInconsistentTotals() {
        val chunks = listOf(
            PayloadChunk(index = 0, total = 2, data = byteArrayOf(1)),
            PayloadChunk(index = 1, total = 3, data = byteArrayOf(2)),
        )

        assertFailsWith<PayloadReconstructionException> {
            PayloadReconstructor.reconstruct(chunks)
        }
    }
}
