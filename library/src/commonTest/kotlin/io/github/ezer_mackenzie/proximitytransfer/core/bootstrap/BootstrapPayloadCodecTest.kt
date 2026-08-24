package io.github.ezer_mackenzie.proximitytransfer.core.bootstrap

import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BootstrapPayloadCodecTest {

    @Test
    fun encodeAndDecodeRoundTrip() {
        val payload = BootstrapPayload(
            peerId = "device-12345",
            bootstrapCapability = TransportCapability.QR_BOOTSTRAP,
            targetCapability = TransportCapability.LOCAL_NETWORK,
            host = "192.168.1.100",
            port = 8080,
            sessionToken = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8),
        )

        val encoded = BootstrapPayloadCodec.encode(payload)
        val decoded = BootstrapPayloadCodec.decode(encoded)

        assertEquals(payload, decoded)
    }

    @Test
    fun throwsForShortHeader() {
        assertFailsWith<BootstrapCodecException> {
            BootstrapPayloadCodec.decode(byteArrayOf(1, 2, 3))
        }
    }

    @Test
    fun throwsForUnsupportedVersion() {
        val badVersionBytes = byteArrayOf(0x02, 8, 3, 0, 80, 0, 1, 65, 0, 1, 66, 0, 1, 1)
        assertFailsWith<BootstrapCodecException> {
            BootstrapPayloadCodec.decode(badVersionBytes)
        }
    }

    @Test
    fun throwsForInvalidPort() {
        assertFailsWith<IllegalArgumentException> {
            BootstrapPayload(
                peerId = "peer",
                bootstrapCapability = TransportCapability.QR_BOOTSTRAP,
                targetCapability = TransportCapability.LOCAL_NETWORK,
                host = "127.0.0.1",
                port = 0,
                sessionToken = byteArrayOf(1),
            )
        }
    }
}
