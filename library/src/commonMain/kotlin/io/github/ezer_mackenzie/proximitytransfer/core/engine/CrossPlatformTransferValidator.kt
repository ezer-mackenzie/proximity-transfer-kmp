package io.github.ezer_mackenzie.proximitytransfer.core.engine

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256

/**
 * Validates cross-platform payload transfers for byte equality and SHA-256 digest integrity.
 */
object CrossPlatformTransferValidator {

    /**
     * Validates that [inputPayload] matches [receivedPayload] in size and SHA-256 hash.
     *
     * @return `true` if input and output sizes and SHA-256 digests match exactly.
     */
    fun validatePayloadIntegrity(inputPayload: ByteArray, receivedPayload: ByteArray): Boolean {
        if (inputPayload.size != receivedPayload.size) {
            return false
        }
        val inputDigest = Sha256.digest(inputPayload)
        val receivedDigest = Sha256.digest(receivedPayload)
        return inputDigest.contentEquals(receivedDigest)
    }
}
