package io.github.ezer_mackenzie.proximitytransfer.core.bootstrap

import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability

/** Binary encoder/decoder for [BootstrapPayload]. */
object BootstrapPayloadCodec {
    private const val VERSION: Byte = 0x01

    /** Encodes [payload] into a versioned binary payload. */
    fun encode(payload: BootstrapPayload): ByteArray {
        val peerIdBytes = payload.peerId.encodeToByteArray()
        val hostBytes = payload.host.encodeToByteArray()
        val tokenBytes = payload.sessionToken

        require(peerIdBytes.size <= 0xFFFF) { "peerId exceeds maximum length" }
        require(hostBytes.size <= 0xFFFF) { "host exceeds maximum length" }
        require(tokenBytes.size <= 0xFFFF) { "sessionToken exceeds maximum length" }

        val totalSize = 1 + 1 + 1 + 2 + 2 + peerIdBytes.size + 2 + hostBytes.size + 2 + tokenBytes.size
        val buffer = ByteArray(totalSize)

        var offset = 0
        buffer[offset++] = VERSION
        buffer[offset++] = payload.bootstrapCapability.code.toByte()
        buffer[offset++] = payload.targetCapability.code.toByte()

        buffer[offset++] = (payload.port shr 8).toByte()
        buffer[offset++] = payload.port.toByte()

        buffer[offset++] = (peerIdBytes.size shr 8).toByte()
        buffer[offset++] = peerIdBytes.size.toByte()
        peerIdBytes.copyInto(buffer, offset)
        offset += peerIdBytes.size

        buffer[offset++] = (hostBytes.size shr 8).toByte()
        buffer[offset++] = hostBytes.size.toByte()
        hostBytes.copyInto(buffer, offset)
        offset += hostBytes.size

        buffer[offset++] = (tokenBytes.size shr 8).toByte()
        buffer[offset++] = tokenBytes.size.toByte()
        tokenBytes.copyInto(buffer, offset)

        return buffer
    }

    /** Decodes [bytes] into a [BootstrapPayload]. */
    fun decode(bytes: ByteArray): BootstrapPayload {
        if (bytes.size < 9) {
            throw BootstrapCodecException("Payload is too short to contain a valid bootstrap header")
        }
        var offset = 0
        val version = bytes[offset++]
        if (version != VERSION) {
            throw BootstrapCodecException("Unsupported bootstrap version $version")
        }
        val bootstrapCode = bytes[offset++].toInt() and 0xFF
        val targetCode = bytes[offset++].toInt() and 0xFF

        val bootstrapCap = TransportCapability.fromCode(bootstrapCode)
            ?: throw BootstrapCodecException("Unknown bootstrap capability code $bootstrapCode")
        val targetCap = TransportCapability.fromCode(targetCode)
            ?: throw BootstrapCodecException("Unknown target capability code $targetCode")

        val port = ((bytes[offset++].toInt() and 0xFF) shl 8) or (bytes[offset++].toInt() and 0xFF)

        fun readField(): ByteArray {
            if (offset + 2 > bytes.size) throw BootstrapCodecException("Unexpected EOF reading length prefix")
            val length = ((bytes[offset++].toInt() and 0xFF) shl 8) or (bytes[offset++].toInt() and 0xFF)
            if (offset + length > bytes.size) throw BootstrapCodecException("Unexpected EOF reading field bytes")
            val field = bytes.copyOfRange(offset, offset + length)
            offset += length
            return field
        }

        val peerIdBytes = readField()
        val hostBytes = readField()
        val tokenBytes = readField()

        val peerId = peerIdBytes.decodeToString()
        val host = hostBytes.decodeToString()

        try {
            return BootstrapPayload(
                peerId = peerId,
                bootstrapCapability = bootstrapCap,
                targetCapability = targetCap,
                host = host,
                port = port,
                sessionToken = tokenBytes,
            )
        } catch (e: IllegalArgumentException) {
            throw BootstrapCodecException("Invalid bootstrap payload attributes: ${e.message}", e)
        }
    }
}
