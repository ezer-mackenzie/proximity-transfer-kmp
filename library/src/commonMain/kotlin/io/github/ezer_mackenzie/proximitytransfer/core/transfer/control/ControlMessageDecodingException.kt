package io.github.ezer_mackenzie.proximitytransfer.core.transfer.control

/** Indicates that a COMPLETE or ERROR payload is malformed. */
class ControlMessageDecodingException(
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)
