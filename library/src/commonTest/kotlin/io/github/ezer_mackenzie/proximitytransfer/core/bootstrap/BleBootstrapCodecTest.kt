package io.github.ezer_mackenzie.proximitytransfer.core.bootstrap

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BleBootstrapCodecTest {

    private val sampleUuidBytes = byteArrayOf(
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10
    )

    @Test
    fun testEncodeAndDecodeRoundTrip() {
        val originalDeviceName = "ProximityPeer-Android"
        val originalTxPower: Byte = -59

        val encoded = BleBootstrapCodec.encode(
            serviceUuidBytes = sampleUuidBytes,
            txPower = originalTxPower,
            deviceName = originalDeviceName,
        )

        val decoded = BleBootstrapCodec.decode(encoded)

        assertContentEquals(sampleUuidBytes, decoded.serviceUuidBytes)
        assertEquals(originalTxPower, decoded.txPower)
        assertEquals(originalDeviceName, decoded.deviceName)
    }

    @Test
    fun testInvalidMagicByteThrowsException() {
        val validEncoded = BleBootstrapCodec.encode(
            serviceUuidBytes = sampleUuidBytes,
            txPower = -60,
            deviceName = "PeerDevice",
        )
        val corrupted = validEncoded.copyOf()
        corrupted[0] = 0x99.toByte()

        assertFailsWith<BootstrapCodecException> {
            BleBootstrapCodec.decode(corrupted)
        }
    }

    @Test
    fun testTruncatedPayloadThrowsException() {
        val validEncoded = BleBootstrapCodec.encode(
            serviceUuidBytes = sampleUuidBytes,
            txPower = -60,
            deviceName = "PeerDeviceWithLongName",
        )
        val truncated = validEncoded.copyOf(validEncoded.size - 5)

        assertFailsWith<BootstrapCodecException> {
            BleBootstrapCodec.decode(truncated)
        }
    }
}
