package io.github.ezer_mackenzie.proximitytransfer.core.transfer.control

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256

/** Encodes and validates a fixed-size completion acknowledgement. */
object CompletionAcknowledgementCodec {
    fun encode(acknowledgement: CompletionAcknowledgement): ByteArray = acknowledgement.sha256

    fun decode(encoded: ByteArray): CompletionAcknowledgement {
        if (encoded.size != Sha256.DIGEST_SIZE) {
            throw ControlMessageDecodingException(
                "COMPLETE payload must contain ${Sha256.DIGEST_SIZE} bytes",
            )
        }
        return CompletionAcknowledgement(encoded)
    }
}
