package io.github.ezer_mackenzie.proximitytransfer.core.security

import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.ControlMessageDecodingException

/** Binary encoder and decoder for [HandshakeMessage]. */
object HandshakeMessageCodec {
    private const val VERSION: Byte = 0x01

    /** Encodes [message] into a versioned binary ByteArray representation. */
    fun encode(message: HandshakeMessage): ByteArray {
        val clientNonce = message.clientNonce
        val serverNonce = message.serverNonce
        val token = message.sessionToken

        require(token.size <= 0xFFFF) { "sessionToken exceeds maximum length" }

        val totalSize = 1 + HandshakeMessage.NONCE_SIZE + HandshakeMessage.NONCE_SIZE + 2 + token.size
        val buffer = ByteArray(totalSize)

        var offset = 0
        buffer[offset++] = VERSION

        clientNonce.copyInto(buffer, offset)
        offset += HandshakeMessage.NONCE_SIZE

        serverNonce.copyInto(buffer, offset)
        offset += HandshakeMessage.NONCE_SIZE

        buffer[offset++] = (token.size shr 8).toByte()
        buffer[offset++] = token.size.toByte()
        token.copyInto(buffer, offset)

        return buffer
    }

    /** Decodes [bytes] into a [HandshakeMessage]. */
    fun decode(bytes: ByteArray): HandshakeMessage {
        val minSize = 1 + HandshakeMessage.NONCE_SIZE + HandshakeMessage.NONCE_SIZE + 2
        if (bytes.size < minSize) {
            throw ControlMessageDecodingException("HandshakeMessage payload is too short")
        }

        var offset = 0
        val version = bytes[offset++]
        if (version != VERSION) {
            throw ControlMessageDecodingException("Unsupported HandshakeMessage version $version")
        }

        val clientNonce = bytes.copyOfRange(offset, offset + HandshakeMessage.NONCE_SIZE)
        offset += HandshakeMessage.NONCE_SIZE

        val serverNonce = bytes.copyOfRange(offset, offset + HandshakeMessage.NONCE_SIZE)
        offset += HandshakeMessage.NONCE_SIZE

        val tokenLen = ((bytes[offset++].toInt() and 0xFF) shl 8) or (bytes[offset++].toInt() and 0xFF)
        if (offset + tokenLen > bytes.size) {
            throw ControlMessageDecodingException("Unexpected EOF reading session token in HandshakeMessage")
        }

        val sessionToken = bytes.copyOfRange(offset, offset + tokenLen)

        try {
            return HandshakeMessage(clientNonce, serverNonce, sessionToken)
        } catch (e: IllegalArgumentException) {
            throw ControlMessageDecodingException("Invalid HandshakeMessage attributes: ${e.message}", e)
        }
    }
}
