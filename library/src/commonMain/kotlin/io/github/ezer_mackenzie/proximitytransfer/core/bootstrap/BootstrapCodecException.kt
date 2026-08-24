package io.github.ezer_mackenzie.proximitytransfer.core.bootstrap

/** Thrown when a bootstrap handshake payload or QR URI is invalid, corrupt, or unsupported. */
class BootstrapCodecException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
