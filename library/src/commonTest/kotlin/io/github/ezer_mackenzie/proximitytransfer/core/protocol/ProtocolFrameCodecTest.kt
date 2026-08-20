package io.github.ezer_mackenzie.proximitytransfer.core.protocol

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProtocolFrameCodecTest {
    @Test
    fun roundTripsDataFrame() {
        val original = ProtocolFrame(
            version = ProtocolVersion.Current,
            type = FrameType.DATA,
            payload = byteArrayOf(0, 1, 2, 127, -1),
        )

        val decoded = ProtocolFrameCodec.decode(ProtocolFrameCodec.encode(original))

        assertEquals(original, decoded)
        assertContentEquals(original.payload, decoded.payload)
    }

    @Test
    fun protectsFrameFromPayloadMutation() {
        val source = byteArrayOf(1, 2, 3)
        val frame = ProtocolFrame(ProtocolVersion.Current, FrameType.DATA, source)

        source[0] = 9
        frame.payload[1] = 9

        assertContentEquals(byteArrayOf(1, 2, 3), frame.payload)
    }

    @Test
    fun rejectsShortHeader() {
        assertFailsWith<FrameDecodingException> {
            ProtocolFrameCodec.decode(ByteArray(9))
        }
    }

    @Test
    fun rejectsInvalidMagic() {
        val encoded = validEncodedFrame().also { it[0] = 0 }

        assertFailsWith<FrameDecodingException> {
            ProtocolFrameCodec.decode(encoded)
        }
    }

    @Test
    fun rejectsUnsupportedVersion() {
        val encoded = validEncodedFrame().also { it[4] = 2 }

        assertFailsWith<FrameDecodingException> {
            ProtocolFrameCodec.decode(encoded)
        }
    }

    @Test
    fun rejectsUnknownFrameType() {
        val encoded = validEncodedFrame().also { it[5] = 127 }

        assertFailsWith<FrameDecodingException> {
            ProtocolFrameCodec.decode(encoded)
        }
    }

    @Test
    fun rejectsMismatchedPayloadLength() {
        val encoded = validEncodedFrame().also { it[9] = 2 }

        assertFailsWith<FrameDecodingException> {
            ProtocolFrameCodec.decode(encoded)
        }
    }

    private fun validEncodedFrame(): ByteArray = ProtocolFrameCodec.encode(
        ProtocolFrame(ProtocolVersion.Current, FrameType.DATA, byteArrayOf(42)),
    )
}
