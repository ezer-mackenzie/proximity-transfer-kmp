package io.github.ezer_mackenzie.proximitytransfer.core.capability.codec

import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.NegotiationResult
import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability

/** Deterministic two-byte codec for a negotiated data and optional bootstrap transport. */
object NegotiationResultCodec {
    private const val PAYLOAD_SIZE = 2
    private const val NO_BOOTSTRAP = 0

    fun encode(result: NegotiationResult): ByteArray = byteArrayOf(
        result.dataTransport.code.toByte(),
        (result.bootstrapTransport?.code ?: NO_BOOTSTRAP).toByte(),
    )

    fun decode(payload: ByteArray): NegotiationResult {
        if (payload.size != PAYLOAD_SIZE) {
            throw NegotiationResultDecodingException(
                "Transport selection must contain exactly $PAYLOAD_SIZE bytes",
            )
        }

        val dataTransport = decodeCapability(payload[0], "data")
        val bootstrapCode = payload[1].toInt() and 0xff
        val bootstrapTransport = if (bootstrapCode == NO_BOOTSTRAP) {
            null
        } else {
            TransportCapability.fromCode(bootstrapCode)
                ?: throw NegotiationResultDecodingException(
                    "Unknown bootstrap transport capability code: $bootstrapCode",
                )
        }

        try {
            return NegotiationResult(dataTransport, bootstrapTransport)
        } catch (exception: IllegalArgumentException) {
            throw NegotiationResultDecodingException(
                exception.message ?: "Invalid transport selection",
            )
        }
    }

    private fun decodeCapability(value: Byte, role: String): TransportCapability {
        val code = value.toInt() and 0xff
        return TransportCapability.fromCode(code)
            ?: throw NegotiationResultDecodingException(
                "Unknown $role transport capability code: $code",
            )
    }
}
