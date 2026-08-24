package io.github.ezer_mackenzie.proximitytransfer.core.transport.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StreamFramingCodecTest {

    @Test
    fun encodeAndReadHeaderRoundTrip() {
        val payload = ByteArray(1024) { (it % 256).toByte() }
        val encodedFrame = StreamFramingCodec.encode(payload)

        assertEquals(4 + 1024, encodedFrame.size)

        val headerBytes = encodedFrame.copyOfRange(0, 4)
        val decodedLength = StreamFramingCodec.readHeader(headerBytes, maxPayloadSizeBytes = 2048)

        assertEquals(1024, decodedLength)
    }

    @Test
    fun encodeZeroLengthPayload() {
        val payload = ByteArray(0)
        val encodedFrame = StreamFramingCodec.encode(payload)

        assertEquals(4, encodedFrame.size)

        val decodedLength = StreamFramingCodec.readHeader(encodedFrame, maxPayloadSizeBytes = 100)
        assertEquals(0, decodedLength)
    }

    @Test
    fun readHeaderThrowsForNegativeLength() {
        val negativeLengthHeader = byteArrayOf(-1, -1, -1, -1) // 0xFFFFFFFF = -1 in two's complement Int
        assertFailsWith<StreamFramingCodecException> {
            StreamFramingCodec.readHeader(negativeLengthHeader)
        }
    }

    @Test
    fun readHeaderThrowsWhenExceedingMaxPayloadSize() {
        val payload = ByteArray(500)
        val encodedFrame = StreamFramingCodec.encode(payload)
        val headerBytes = encodedFrame.copyOfRange(0, 4)

        assertFailsWith<StreamFramingCodecException> {
            StreamFramingCodec.readHeader(headerBytes, maxPayloadSizeBytes = 250)
        }
    }

    @Test
    fun readHeaderRequiresExactly4Bytes() {
        assertFailsWith<IllegalArgumentException> {
            StreamFramingCodec.readHeader(byteArrayOf(0, 0, 1))
        }
    }
}
