package io.github.ezer_mackenzie.proximitytransfer.core.capability.codec

/** Indicates that a transport-selection payload is malformed or unsupported. */
class NegotiationResultDecodingException(message: String) : IllegalArgumentException(message)
