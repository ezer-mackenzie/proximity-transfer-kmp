package io.github.ezer_mackenzie.proximitytransfer.core.capability

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeviceCapabilitiesCodecTest {
    @Test
    fun roundTripsCapabilitiesDeterministically() {
        val capabilities = DeviceCapabilities(
            setOf(
                TransportCapability.QR_BOOTSTRAP,
                TransportCapability.BLE,
                TransportCapability.LOCAL_NETWORK,
            ),
        )

        val encoded = DeviceCapabilitiesCodec.encode(capabilities)

        assertContentEquals(byteArrayOf(3, 2, 3, 8), encoded)
        assertEquals(capabilities, DeviceCapabilitiesCodec.decode(encoded))
    }

    @Test
    fun rejectsEmptyCapabilitySet() {
        assertFailsWith<IllegalArgumentException> {
            DeviceCapabilities(emptySet())
        }
        assertFailsWith<DeviceCapabilitiesDecodingException> {
            DeviceCapabilitiesCodec.decode(byteArrayOf(0))
        }
    }

    @Test
    fun rejectsUnknownCapability() {
        assertFailsWith<DeviceCapabilitiesDecodingException> {
            DeviceCapabilitiesCodec.decode(byteArrayOf(1, 127))
        }
    }

    @Test
    fun rejectsDuplicateCapability() {
        assertFailsWith<DeviceCapabilitiesDecodingException> {
            DeviceCapabilitiesCodec.decode(byteArrayOf(2, 2, 2))
        }
    }

    @Test
    fun rejectsMismatchedCount() {
        assertFailsWith<DeviceCapabilitiesDecodingException> {
            DeviceCapabilitiesCodec.decode(byteArrayOf(2, 1))
        }
    }
}
