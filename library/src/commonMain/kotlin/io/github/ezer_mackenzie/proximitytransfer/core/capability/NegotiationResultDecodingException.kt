package io.github.ezer_mackenzie.proximitytransfer.core.capability

/** Indicates that a transport-selection payload is malformed or unsupported. */
class NegotiationResultDecodingException(message: String) : IllegalArgumentException(message)
