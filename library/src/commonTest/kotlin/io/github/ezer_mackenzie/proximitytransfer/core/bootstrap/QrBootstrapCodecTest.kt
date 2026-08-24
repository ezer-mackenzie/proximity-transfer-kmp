package io.github.ezer_mackenzie.proximitytransfer.core.bootstrap

import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class QrBootstrapCodecTest {

    @Test
    fun encodeAndDecodeQrUriRoundTrip() {
        val payload = BootstrapPayload(
            peerId = "peer-abc-789",
            bootstrapCapability = TransportCapability.QR_BOOTSTRAP,
            targetCapability = TransportCapability.LOCAL_NETWORK,
            host = "10.0.0.42",
            port = 9090,
            sessionToken = byteArrayOf(10, 20, 30, 40),
        )

        val uri = QrBootstrapCodec.encodeToUri(payload)
        assertTrue(uri.startsWith("proximity://v1?data="))

        val decoded = QrBootstrapCodec.decodeFromUri(uri)
        assertEquals(payload, decoded)
    }

    @Test
    fun throwsForInvalidScheme() {
        assertFailsWith<BootstrapCodecException> {
            QrBootstrapCodec.decodeFromUri("https://example.com/qr?data=123")
        }
    }

    @Test
    fun throwsForEmptyData() {
        assertFailsWith<BootstrapCodecException> {
            QrBootstrapCodec.decodeFromUri("proximity://v1?data=")
        }
    }
}
