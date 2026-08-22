package io.github.ezer_mackenzie.proximitytransfer.core.transfer.control

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256

/** Immutable receiver confirmation containing the verified payload digest. */
class CompletionAcknowledgement(
    sha256: ByteArray,
) {
    private val sha256Bytes = sha256.copyOf()

    init {
        require(sha256Bytes.size == Sha256.DIGEST_SIZE) {
            "SHA-256 digest must contain ${Sha256.DIGEST_SIZE} bytes"
        }
    }

    val sha256: ByteArray
        get() = sha256Bytes.copyOf()
}
