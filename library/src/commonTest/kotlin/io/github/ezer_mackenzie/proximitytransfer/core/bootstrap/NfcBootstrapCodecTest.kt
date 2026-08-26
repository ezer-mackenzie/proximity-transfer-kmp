package io.github.ezer_mackenzie.proximitytransfer.core.bootstrap

import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NfcBootstrapCodecTest {

    private val samplePayload = BootstrapPayload(
        peerId = "peer-nfc-99",
        bootstrapCapability = TransportCapability.NFC_BOOTSTRAP,
        targetCapability = TransportCapability.LOCAL_NETWORK,
        host = "192.168.1.105",
        port = 8080,
        sessionToken = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8),
    )

    @Test
    fun encodeAndDecodeNdefRoundTrip() {
        val ndefBytes = NfcBootstrapCodec.encodeToNdef(samplePayload)
        val decoded = NfcBootstrapCodec.decodeFromNdef(ndefBytes)

        assertEquals(samplePayload.peerId, decoded.peerId)
        assertEquals(samplePayload.bootstrapCapability, decoded.bootstrapCapability)
        assertEquals(samplePayload.targetCapability, decoded.targetCapability)
        assertEquals(samplePayload.host, decoded.host)
        assertEquals(samplePayload.port, decoded.port)
        assertEquals(samplePayload.sessionToken.toList(), decoded.sessionToken.toList())
    }

    @Test
    fun failsOnTooShortNdefData() {
        assertFailsWith<BootstrapCodecException> {
            NfcBootstrapCodec.decodeFromNdef(byteArrayOf(0x80.toByte(), 0x01))
        }
    }

    @Test
    fun failsOnInvalidTnf() {
        val bytes = byteArrayOf(0x81.toByte(), 0x01, 0x05, 0x61, 0x62)
        assertFailsWith<BootstrapCodecException> {
            NfcBootstrapCodec.decodeFromNdef(bytes)
        }
    }

    @Test
    fun failsOnMimeTypeMismatch() {
        val binaryPayload = BootstrapPayloadCodec.encode(samplePayload)
        val invalidMime = "application/other-type".encodeToByteArray()

        val headerFlags = (0x80 or 0x40 or 0x10 or 0x02).toByte()
        val buffer = ByteArray(1 + 1 + 1 + invalidMime.size + binaryPayload.size)

        var offset = 0
        buffer[offset++] = headerFlags
        buffer[offset++] = invalidMime.size.toByte()
        buffer[offset++] = binaryPayload.size.toByte()
        invalidMime.copyInto(buffer, offset)
        offset += invalidMime.size
        binaryPayload.copyInto(buffer, offset)

        assertFailsWith<BootstrapCodecException> {
            NfcBootstrapCodec.decodeFromNdef(buffer)
        }
    }
}
