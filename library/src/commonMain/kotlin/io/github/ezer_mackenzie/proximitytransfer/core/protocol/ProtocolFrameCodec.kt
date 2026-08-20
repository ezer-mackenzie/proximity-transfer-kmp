package io.github.ezer_mackenzie.proximitytransfer.core.protocol

/** Encodes and decodes the deterministic binary frame format documented in `docs/wire-format.md`. */
object ProtocolFrameCodec {
    private val magic = byteArrayOf(0x50, 0x58, 0x46, 0x52) // PXFR
    private const val HeaderSize = 10

    /** Encodes [frame], rejecting versions this implementation cannot emit. */
    fun encode(frame: ProtocolFrame): ByteArray {
        require(frame.version == ProtocolVersion.Current) {
            "Unsupported protocol version: ${frame.version.value}"
        }

        val payload = frame.payload
        val encoded = ByteArray(HeaderSize + payload.size)

        magic.copyInto(encoded)
        encoded[4] = frame.version.value.toByte()
        encoded[5] = frame.type.code.toByte()
        writeInt(encoded, offset = 6, value = payload.size)
        payload.copyInto(encoded, destinationOffset = HeaderSize)

        return encoded
    }

    /** Decodes one complete frame or throws [FrameDecodingException] when it is invalid. */
    fun decode(encoded: ByteArray): ProtocolFrame {
        if (encoded.size < HeaderSize) {
            throw FrameDecodingException("Frame is shorter than the $HeaderSize-byte header")
        }
        if (!encoded.hasMagic()) {
            throw FrameDecodingException("Frame magic is invalid")
        }

        val versionValue = encoded[4].toInt() and 0xFF
        if (versionValue != ProtocolVersion.Current.value) {
            throw FrameDecodingException("Unsupported protocol version: $versionValue")
        }

        val typeCode = encoded[5].toInt() and 0xFF
        val type = FrameType.fromCode(typeCode)
            ?: throw FrameDecodingException("Unknown frame type: $typeCode")

        val payloadSize = readInt(encoded, offset = 6)
        if (payloadSize < 0 || payloadSize != encoded.size - HeaderSize) {
            throw FrameDecodingException(
                "Declared payload size $payloadSize does not match ${encoded.size - HeaderSize} available bytes",
            )
        }

        return ProtocolFrame(
            version = ProtocolVersion.Current,
            type = type,
            payload = encoded.copyOfRange(HeaderSize, encoded.size),
        )
    }

    private fun ByteArray.hasMagic(): Boolean =
        magic.indices.all { index -> this[index] == magic[index] }

    private fun writeInt(destination: ByteArray, offset: Int, value: Int) {
        destination[offset] = (value ushr 24).toByte()
        destination[offset + 1] = (value ushr 16).toByte()
        destination[offset + 2] = (value ushr 8).toByte()
        destination[offset + 3] = value.toByte()
    }

    private fun readInt(source: ByteArray, offset: Int): Int =
        ((source[offset].toInt() and 0xFF) shl 24) or
            ((source[offset + 1].toInt() and 0xFF) shl 16) or
            ((source[offset + 2].toInt() and 0xFF) shl 8) or
            (source[offset + 3].toInt() and 0xFF)
}
