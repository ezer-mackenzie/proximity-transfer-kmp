package io.github.ezer_mackenzie.proximitytransfer.core.bootstrap

/**
 * Encodes and decodes Bluetooth Low Energy (BLE) bootstrap payloads.
 *
 * Wire Format (Binary):
 * - Magic byte: 0x42 ('B')
 * - 16 bytes: Service UUID (128-bit raw bytes)
 * - 1 byte: Tx Power Level (signed Byte)
 * - 2 bytes: Device Name Length N (UInt16 Big Endian)
 * - N bytes: Device Name (UTF-8)
 */
object BleBootstrapCodec {
    private const val MAGIC_BYTE: Byte = 0x42
    private const val MIN_HEADER_SIZE = 1 + 16 + 1 + 2

    fun encode(serviceUuidBytes: ByteArray, txPower: Byte, deviceName: String): ByteArray {
        require(serviceUuidBytes.size == 16) {
            "Service UUID raw bytes must be exactly 16 bytes, but got ${serviceUuidBytes.size}"
        }
        val nameBytes = deviceName.encodeToByteArray()
        require(nameBytes.size <= 65535) {
            "Device name length exceeds 65535 bytes limit"
        }

        val totalSize = MIN_HEADER_SIZE + nameBytes.size
        val output = ByteArray(totalSize)
        var offset = 0

        output[offset++] = MAGIC_BYTE
        serviceUuidBytes.copyInto(output, offset)
        offset += 16

        output[offset++] = txPower

        output[offset++] = ((nameBytes.size shr 8) and 0xFF).toByte()
        output[offset++] = (nameBytes.size and 0xFF).toByte()

        nameBytes.copyInto(output, offset)
        return output
    }

    fun decode(bytes: ByteArray): BleBootstrapData {
        if (bytes.size < MIN_HEADER_SIZE) {
            throw BootstrapCodecException(
                "BLE bootstrap payload size (${bytes.size}) is smaller than minimum required header size ($MIN_HEADER_SIZE)"
            )
        }
        if (bytes[0] != MAGIC_BYTE) {
            throw BootstrapCodecException(
                "Invalid BLE bootstrap magic byte: expected 0x42, got 0x${bytes[0].toUByte().toString(16)}"
            )
        }

        var offset = 1
        val serviceUuidBytes = bytes.copyOfRange(offset, offset + 16)
        offset += 16

        val txPower = bytes[offset++]

        val nameLength = ((bytes[offset].toInt() and 0xFF) shl 8) or (bytes[offset + 1].toInt() and 0xFF)
        offset += 2

        if (bytes.size < offset + nameLength) {
            throw BootstrapCodecException(
                "Truncated BLE bootstrap payload: expected device name length of $nameLength bytes, but only ${bytes.size - offset} bytes available"
            )
        }

        val deviceName = bytes.decodeToString(offset, offset + nameLength)
        return BleBootstrapData(
            serviceUuidBytes = serviceUuidBytes,
            txPower = txPower,
            deviceName = deviceName,
        )
    }
}
