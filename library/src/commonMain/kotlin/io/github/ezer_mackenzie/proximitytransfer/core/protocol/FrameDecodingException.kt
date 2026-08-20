package io.github.ezer_mackenzie.proximitytransfer.core.protocol

/** Indicates that received bytes are not a valid frame supported by this implementation. */
class FrameDecodingException(message: String) : IllegalArgumentException(message)
