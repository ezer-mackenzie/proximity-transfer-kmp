package io.github.ezer_mackenzie.proximitytransfer.core.transfer.cancel

import io.github.ezer_mackenzie.proximitytransfer.core.transport.network.StreamFramingCodecException

/**
 * Encodes and decodes binary cancellation signal frames (`0x43` magic byte).
 */
object CancelSignalCodec {
    private const val MAGIC_BYTE: Byte = 0x43
    private const val MIN_SIZE = 1 + 2

    fun encode(sessionToken: String): ByteArray {
        val tokenBytes = sessionToken.encodeToByteArray()
        require(tokenBytes.size <= 65535) { "Session token too long" }

        val output = ByteArray(1 + 2 + tokenBytes.size)
        var offset = 0

        output[offset++] = MAGIC_BYTE
        output[offset++] = ((tokenBytes.size shr 8) and 0xFF).toByte()
        output[offset++] = (tokenBytes.size and 0xFF).toByte()

        tokenBytes.copyInto(output, offset)
        return output
    }

    fun decode(bytes: ByteArray): String {
        if (bytes.size < MIN_SIZE) {
            throw StreamFramingCodecException("Cancel signal payload size (${bytes.size}) is smaller than minimum required size ($MIN_SIZE)")
        }
        if (bytes[0] != MAGIC_BYTE) {
            throw StreamFramingCodecException("Invalid cancel signal magic byte: expected 0x43, got 0x${bytes[0].toUByte().toString(16)}")
        }

        val tokenLen = ((bytes[1].toInt() and 0xFF) shl 8) or (bytes[2].toInt() and 0xFF)
        if (bytes.size < 3 + tokenLen) {
            throw StreamFramingCodecException("Truncated session token in cancel signal payload")
        }

        return bytes.decodeToString(3, 3 + tokenLen)
    }
}
