package io.github.ezer_mackenzie.proximitytransfer.core.transport.network

import io.github.ezer_mackenzie.proximitytransfer.core.transfer.config.TransferLimits

/**
 * Encodes and decodes discrete binary frames over a continuous byte stream
 * using a 4-byte big-endian length prefix.
 */
object StreamFramingCodec {
    const val HEADER_SIZE_BYTES = 4

    /** Encodes [payload] into a length-prefixed binary frame. */
    fun encode(payload: ByteArray): ByteArray {
        val payloadLength = payload.size
        val frame = ByteArray(HEADER_SIZE_BYTES + payloadLength)
        frame[0] = (payloadLength shr 24).toByte()
        frame[1] = (payloadLength shr 16).toByte()
        frame[2] = (payloadLength shr 8).toByte()
        frame[3] = payloadLength.toByte()
        payload.copyInto(frame, destinationOffset = HEADER_SIZE_BYTES)
        return frame
    }

    /**
     * Decodes a 4-byte length header from [headerBytes].
     *
     * @throws StreamFramingCodecException if the header is malformed, negative, or exceeds [maxPayloadSizeBytes].
     */
    fun readHeader(
        headerBytes: ByteArray,
        maxPayloadSizeBytes: Long = TransferLimits.DEFAULT_MAX_PAYLOAD_BYTES,
    ): Int {
        require(headerBytes.size == HEADER_SIZE_BYTES) {
            "Header must be exactly $HEADER_SIZE_BYTES bytes, but got ${headerBytes.size}"
        }
        val length = ((headerBytes[0].toInt() and 0xFF) shl 24) or
                ((headerBytes[1].toInt() and 0xFF) shl 16) or
                ((headerBytes[2].toInt() and 0xFF) shl 8) or
                (headerBytes[3].toInt() and 0xFF)

        if (length < 0) {
            throw StreamFramingCodecException("Negative frame length: $length")
        }
        if (length.toLong() > maxPayloadSizeBytes) {
            throw StreamFramingCodecException("Frame length $length exceeds maximum allowed limit $maxPayloadSizeBytes")
        }
        return length
    }
}
