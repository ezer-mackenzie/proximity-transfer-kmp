package io.github.ezer_mackenzie.proximitytransfer.core.transfer.control

/** Encodes a bounded ERROR payload as code, message length, and UTF-8 bytes. */
object RemoteErrorCodec {
    private const val HEADER_SIZE = 3
    const val MAX_MESSAGE_SIZE: Int = 1_024

    fun encode(error: RemoteError): ByteArray {
        val message = error.message.encodeToByteArray()
        val encoded = ByteArray(HEADER_SIZE + message.size)
        encoded[0] = error.code.code.toByte()
        encoded[1] = (message.size ushr 8).toByte()
        encoded[2] = message.size.toByte()
        message.copyInto(encoded, destinationOffset = HEADER_SIZE)
        return encoded
    }

    fun decode(encoded: ByteArray): RemoteError {
        if (encoded.size < HEADER_SIZE) {
            throw ControlMessageDecodingException("ERROR payload is shorter than its header")
        }

        val codeValue = encoded[0].toInt() and 0xFF
        val code = RemoteErrorCode.fromCode(codeValue)
            ?: throw ControlMessageDecodingException("Unknown remote error code: $codeValue")
        val messageSize =
            ((encoded[1].toInt() and 0xFF) shl 8) or (encoded[2].toInt() and 0xFF)
        if (messageSize > MAX_MESSAGE_SIZE || encoded.size != HEADER_SIZE + messageSize) {
            throw ControlMessageDecodingException("ERROR message length is invalid")
        }

        return RemoteError(
            code = code,
            message = encoded.copyOfRange(HEADER_SIZE, encoded.size).decodeToString(),
        )
    }
}
