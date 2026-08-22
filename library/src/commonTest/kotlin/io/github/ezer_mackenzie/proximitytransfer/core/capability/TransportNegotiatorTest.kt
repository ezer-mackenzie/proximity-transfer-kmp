package io.github.ezer_mackenzie.proximitytransfer.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TransportNegotiatorTest {
    @Test
    fun selectsBestCommonCrossPlatformCapabilities() {
        val android = DeviceCapabilities(
            setOf(
                TransportCapability.BLE,
                TransportCapability.LOCAL_NETWORK,
                TransportCapability.WIFI_AWARE,
                TransportCapability.NFC_BOOTSTRAP,
                TransportCapability.QR_BOOTSTRAP,
            ),
        )
        val iphone = DeviceCapabilities(
            setOf(
                TransportCapability.BLE,
                TransportCapability.LOCAL_NETWORK,
                TransportCapability.MULTIPEER_CONNECTIVITY,
                TransportCapability.QR_BOOTSTRAP,
            ),
        )

        val result = TransportNegotiator().negotiate(android, iphone)

        assertEquals(TransportCapability.LOCAL_NETWORK, result.dataTransport)
        assertEquals(TransportCapability.QR_BOOTSTRAP, result.bootstrapTransport)
    }

    @Test
    fun excludesBootstrapOnlyCapabilityFromDataSelection() {
        val local = DeviceCapabilities(
            setOf(TransportCapability.QR_BOOTSTRAP, TransportCapability.BLE),
        )
        val remote = DeviceCapabilities(
            setOf(TransportCapability.QR_BOOTSTRAP, TransportCapability.BLE),
        )

        val result = TransportNegotiator().negotiate(local, remote)

        assertEquals(TransportCapability.BLE, result.dataTransport)
        assertEquals(TransportCapability.QR_BOOTSTRAP, result.bootstrapTransport)
    }

    @Test
    fun bootstrapIsOptionalWhenConnectionAlreadyExists() {
        val local = DeviceCapabilities(setOf(TransportCapability.LOCAL_NETWORK))
        val remote = DeviceCapabilities(setOf(TransportCapability.LOCAL_NETWORK))

        val result = TransportNegotiator().negotiate(local, remote)

        assertNull(result.bootstrapTransport)
    }

    @Test
    fun failsWhenOnlyCommonCapabilityCannotTransferData() {
        val local = DeviceCapabilities(
            setOf(TransportCapability.QR_BOOTSTRAP, TransportCapability.WIFI_AWARE),
        )
        val remote = DeviceCapabilities(
            setOf(TransportCapability.QR_BOOTSTRAP, TransportCapability.MULTIPEER_CONNECTIVITY),
        )

        assertFailsWith<NoCompatibleTransportException> {
            TransportNegotiator().negotiate(local, remote)
        }
    }

    @Test
    fun supportsCallerDefinedPreference() {
        val capabilities = DeviceCapabilities(
            setOf(TransportCapability.BLE, TransportCapability.LOCAL_NETWORK),
        )
        val negotiator = TransportNegotiator(
            dataPreference = listOf(
                TransportCapability.BLE,
                TransportCapability.LOCAL_NETWORK,
            ),
        )

        assertEquals(
            TransportCapability.BLE,
            negotiator.negotiate(capabilities, capabilities).dataTransport,
        )
    }
}
