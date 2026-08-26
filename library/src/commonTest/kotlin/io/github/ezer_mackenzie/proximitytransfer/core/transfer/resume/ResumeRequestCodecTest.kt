package io.github.ezer_mackenzie.proximitytransfer.core.transfer.resume

import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.ControlMessageDecodingException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResumeRequestCodecTest {

    @Test
    fun encodeAndDecodeResumeRequestRoundTrip() {
        val bitmap = ChunkBitmap(totalChunks = 12)
        bitmap.setReceived(0)
        bitmap.setReceived(1)
        bitmap.setReceived(2)
        bitmap.setReceived(5)

        val request = ResumeRequest(
            sessionToken = byteArrayOf(10, 20, 30, 40),
            lastReceivedChunkIndex = 2,
            bitmap = bitmap,
        )

        val encoded = ResumeRequestCodec.encode(request)
        val decoded = ResumeRequestCodec.decode(encoded)

        assertEquals(request.sessionToken.toList(), decoded.sessionToken.toList())
        assertEquals(request.lastReceivedChunkIndex, decoded.lastReceivedChunkIndex)
        assertEquals(request.bitmap, decoded.bitmap)
    }

    @Test
    fun failsOnTruncatedHeader() {
        assertFailsWith<ControlMessageDecodingException> {
            ResumeRequestCodec.decode(byteArrayOf(0x01, 0x00, 0x04))
        }
    }

    @Test
    fun failsOnInvalidVersion() {
        val bitmap = ChunkBitmap(totalChunks = 4)
        val request = ResumeRequest(
            sessionToken = byteArrayOf(1, 2, 3),
            lastReceivedChunkIndex = 1,
            bitmap = bitmap,
        )

        val encoded = ResumeRequestCodec.encode(request)
        encoded[0] = 0x02.toByte()

        assertFailsWith<ControlMessageDecodingException> {
            ResumeRequestCodec.decode(encoded)
        }
    }
}
