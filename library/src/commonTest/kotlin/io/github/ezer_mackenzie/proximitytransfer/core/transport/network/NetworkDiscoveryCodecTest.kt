package io.github.ezer_mackenzie.proximitytransfer.core.transport.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NetworkDiscoveryCodecTest {

    @Test
    fun testEncodeAndDecodeRoundTrip() {
        val original = NetworkServiceAdvertisement(
            serviceName = "ProximityPeerService",
            host = "192.168.1.105",
            port = 9090,
            sessionToken = "token-xyz-12345",
        )

        val encoded = NetworkDiscoveryCodec.encode(original)
        val decoded = NetworkDiscoveryCodec.decode(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun testCorruptedMagicByteThrowsException() {
        val original = NetworkServiceAdvertisement("Service", "127.0.0.1", 8080, "token")
        val encoded = NetworkDiscoveryCodec.encode(original)
        encoded[0] = 0x00.toByte()

        assertFailsWith<StreamFramingCodecException> {
            NetworkDiscoveryCodec.decode(encoded)
        }
    }

    @Test
    fun testTruncatedPayloadThrowsException() {
        val original = NetworkServiceAdvertisement("Service", "127.0.0.1", 8080, "token-abc")
        val encoded = NetworkDiscoveryCodec.encode(original)
        val truncated = encoded.copyOf(encoded.size - 4)

        assertFailsWith<StreamFramingCodecException> {
            NetworkDiscoveryCodec.decode(truncated)
        }
    }
}
