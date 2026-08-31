package io.github.ezer_mackenzie.proximitytransfer.core.transport.network

/**
 * Encodes and decodes binary local network service discovery advertisements.
 */
object NetworkDiscoveryCodec {
    private const val MAGIC_BYTE: Byte = 0x4E
    private const val MIN_SIZE = 1 + 2 + 2 + 0 + 2 + 0 + 2 + 0

    fun encode(advertisement: NetworkServiceAdvertisement): ByteArray {
        val nameBytes = advertisement.serviceName.encodeToByteArray()
        val hostBytes = advertisement.host.encodeToByteArray()
        val tokenBytes = advertisement.sessionToken.encodeToByteArray()

        require(advertisement.port in 1..65535) { "Port must be in range 1..65535" }
        require(nameBytes.size <= 65535) { "Service name too long" }
        require(hostBytes.size <= 65535) { "Host too long" }
        require(tokenBytes.size <= 65535) { "Session token too long" }

        val totalSize = 1 + 2 + 2 + nameBytes.size + 2 + hostBytes.size + 2 + tokenBytes.size
        val output = ByteArray(totalSize)
        var offset = 0

        output[offset++] = MAGIC_BYTE

        output[offset++] = ((advertisement.port shr 8) and 0xFF).toByte()
        output[offset++] = (advertisement.port and 0xFF).toByte()

        output[offset++] = ((nameBytes.size shr 8) and 0xFF).toByte()
        output[offset++] = (nameBytes.size and 0xFF).toByte()
        nameBytes.copyInto(output, offset)
        offset += nameBytes.size

        output[offset++] = ((hostBytes.size shr 8) and 0xFF).toByte()
        output[offset++] = (hostBytes.size and 0xFF).toByte()
        hostBytes.copyInto(output, offset)
        offset += hostBytes.size

        output[offset++] = ((tokenBytes.size shr 8) and 0xFF).toByte()
        output[offset++] = (tokenBytes.size and 0xFF).toByte()
        tokenBytes.copyInto(output, offset)

        return output
    }

    fun decode(bytes: ByteArray): NetworkServiceAdvertisement {
        if (bytes.size < MIN_SIZE) {
            throw StreamFramingCodecException("Network discovery payload size (${bytes.size}) is smaller than minimum required size ($MIN_SIZE)")
        }
        if (bytes[0] != MAGIC_BYTE) {
            throw StreamFramingCodecException("Invalid network discovery magic byte: expected 0x4E, got 0x${bytes[0].toUByte().toString(16)}")
        }

        var offset = 1
        val port = ((bytes[offset].toInt() and 0xFF) shl 8) or (bytes[offset + 1].toInt() and 0xFF)
        offset += 2

        val nameLen = ((bytes[offset].toInt() and 0xFF) shl 8) or (bytes[offset + 1].toInt() and 0xFF)
        offset += 2
        if (bytes.size < offset + nameLen) {
            throw StreamFramingCodecException("Truncated service name in network discovery payload")
        }
        val serviceName = bytes.decodeToString(offset, offset + nameLen)
        offset += nameLen

        val hostLen = ((bytes[offset].toInt() and 0xFF) shl 8) or (bytes[offset + 1].toInt() and 0xFF)
        offset += 2
        if (bytes.size < offset + hostLen) {
            throw StreamFramingCodecException("Truncated host in network discovery payload")
        }
        val host = bytes.decodeToString(offset, offset + hostLen)
        offset += hostLen

        val tokenLen = ((bytes[offset].toInt() and 0xFF) shl 8) or (bytes[offset + 1].toInt() and 0xFF)
        offset += 2
        if (bytes.size < offset + tokenLen) {
            throw StreamFramingCodecException("Truncated session token in network discovery payload")
        }
        val sessionToken = bytes.decodeToString(offset, offset + tokenLen)

        return NetworkServiceAdvertisement(
            serviceName = serviceName,
            host = host,
            port = port,
            sessionToken = sessionToken,
        )
    }
}
