package io.github.ezer_mackenzie.proximitytransfer.core.security

import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.ControlMessageDecodingException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HandshakeMessageCodecTest {

    private val sampleHandshake = HandshakeMessage(
        clientNonce = ByteArray(16) { (it + 1).toByte() },
        serverNonce = ByteArray(16) { (it + 20).toByte() },
        sessionToken = byteArrayOf(100, 101, 102, 103),
    )

    @Test
    fun encodeAndDecodeHandshakeRoundTrip() {
        val encoded = HandshakeMessageCodec.encode(sampleHandshake)
        val decoded = HandshakeMessageCodec.decode(encoded)

        assertEquals(sampleHandshake, decoded)
    }

    @Test
    fun derivesSessionKeySpecConsistently() {
        val keySpec1 = sampleHandshake.deriveSessionKeySpec()
        val keySpec2 = sampleHandshake.deriveSessionKeySpec()

        assertEquals(keySpec1, keySpec2)
        assertEquals(32, keySpec1.secretKey.size)
        assertEquals(16, keySpec1.noncePrefix.size)
    }

    @Test
    fun failsOnTruncatedHandshakePayload() {
        assertFailsWith<ControlMessageDecodingException> {
            HandshakeMessageCodec.decode(byteArrayOf(0x01, 0x02, 0x03))
        }
    }
}
