package io.github.ezer_mackenzie.proximitytransfer.core.transfer.resume

import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.ControlMessageDecodingException

/** Binary encoder/decoder for [ResumeRequest]. */
object ResumeRequestCodec {
    private const val VERSION: Byte = 0x01

    /** Encodes [request] into a versioned binary ByteArray representation. */
    fun encode(request: ResumeRequest): ByteArray {
        val tokenBytes = request.sessionToken
        require(tokenBytes.size <= 0xFFFF) { "sessionToken exceeds maximum length" }

        val bitmapBytes = request.bitmap.encode()
        val totalSize = 1 + 2 + tokenBytes.size + 4 + 4 + 4 + bitmapBytes.size
        val buffer = ByteArray(totalSize)

        var offset = 0
        buffer[offset++] = VERSION

        buffer[offset++] = (tokenBytes.size shr 8).toByte()
        buffer[offset++] = tokenBytes.size.toByte()
        tokenBytes.copyInto(buffer, offset)
        offset += tokenBytes.size

        val lastIndex = request.lastReceivedChunkIndex
        buffer[offset++] = (lastIndex shr 24).toByte()
        buffer[offset++] = (lastIndex shr 16).toByte()
        buffer[offset++] = (lastIndex shr 8).toByte()
        buffer[offset++] = lastIndex.toByte()

        val totalChunks = request.bitmap.totalChunks
        buffer[offset++] = (totalChunks shr 24).toByte()
        buffer[offset++] = (totalChunks shr 16).toByte()
        buffer[offset++] = (totalChunks shr 8).toByte()
        buffer[offset++] = totalChunks.toByte()

        val bitmapLen = bitmapBytes.size
        buffer[offset++] = (bitmapLen shr 24).toByte()
        buffer[offset++] = (bitmapLen shr 16).toByte()
        buffer[offset++] = (bitmapLen shr 8).toByte()
        buffer[offset++] = bitmapLen.toByte()
        bitmapBytes.copyInto(buffer, offset)

        return buffer
    }

    /** Decodes [bytes] into a [ResumeRequest]. */
    fun decode(bytes: ByteArray): ResumeRequest {
        if (bytes.size < 1 + 2 + 4 + 4 + 4) {
            throw ControlMessageDecodingException("ResumeRequest payload is too short")
        }

        var offset = 0
        val version = bytes[offset++]
        if (version != VERSION) {
            throw ControlMessageDecodingException("Unsupported ResumeRequest version $version")
        }

        val tokenLen = ((bytes[offset++].toInt() and 0xFF) shl 8) or (bytes[offset++].toInt() and 0xFF)
        if (offset + tokenLen + 12 > bytes.size) {
            throw ControlMessageDecodingException("Unexpected EOF reading ResumeRequest header fields")
        }

        val sessionToken = bytes.copyOfRange(offset, offset + tokenLen)
        offset += tokenLen

        val lastReceivedChunkIndex = ((bytes[offset++].toInt() and 0xFF) shl 24) or
            ((bytes[offset++].toInt() and 0xFF) shl 16) or
            ((bytes[offset++].toInt() and 0xFF) shl 8) or
            (bytes[offset++].toInt() and 0xFF)

        val totalChunks = ((bytes[offset++].toInt() and 0xFF) shl 24) or
            ((bytes[offset++].toInt() and 0xFF) shl 16) or
            ((bytes[offset++].toInt() and 0xFF) shl 8) or
            (bytes[offset++].toInt() and 0xFF)

        val bitmapLen = ((bytes[offset++].toInt() and 0xFF) shl 24) or
            ((bytes[offset++].toInt() and 0xFF) shl 16) or
            ((bytes[offset++].toInt() and 0xFF) shl 8) or
            (bytes[offset++].toInt() and 0xFF)

        if (offset + bitmapLen > bytes.size) {
            throw ControlMessageDecodingException("Unexpected EOF reading ResumeRequest bitmap payload")
        }

        val bitmapBytes = bytes.copyOfRange(offset, offset + bitmapLen)

        try {
            val bitmap = ChunkBitmap.decode(totalChunks, bitmapBytes)
            return ResumeRequest(
                sessionToken = sessionToken,
                lastReceivedChunkIndex = lastReceivedChunkIndex,
                bitmap = bitmap,
            )
        } catch (e: IllegalArgumentException) {
            throw ControlMessageDecodingException("Invalid ResumeRequest payload: ${e.message}", e)
        }
    }
}
