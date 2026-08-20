package dev.proximitytransfer.core.transfer.chunk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PayloadChunkCodecTest {
    @Test
    fun roundTripsChunk() {
        val chunk = PayloadChunk(index = 2, total = 4, data = byteArrayOf(1, 2, 3))

        assertEquals(chunk, PayloadChunkCodec.decode(PayloadChunkCodec.encode(chunk)))
    }

    @Test
    fun rejectsShortHeader() {
        assertFailsWith<PayloadChunkDecodingException> {
            PayloadChunkCodec.decode(ByteArray(7))
        }
    }

    @Test
    fun rejectsIndexOutsideDeclaredTotal() {
        val encoded = PayloadChunkCodec.encode(
            PayloadChunk(index = 0, total = 1, data = byteArrayOf()),
        ).also { it[3] = 1 }

        assertFailsWith<PayloadChunkDecodingException> {
            PayloadChunkCodec.decode(encoded)
        }
    }
}
