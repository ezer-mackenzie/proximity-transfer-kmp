package io.github.ezer_mackenzie.proximitytransfer.core.security

/** Thrown when frame payload encryption or decryption/MAC verification fails. */
class FrameEncryptionException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
