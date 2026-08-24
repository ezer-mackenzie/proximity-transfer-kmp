package io.github.ezer_mackenzie.proximitytransfer.core.transport.network

/** Thrown when a length-prefixed stream frame is malformed, truncated, or exceeds maximum size limits. */
class StreamFramingCodecException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
