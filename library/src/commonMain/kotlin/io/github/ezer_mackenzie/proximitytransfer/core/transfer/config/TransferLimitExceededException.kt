package io.github.ezer_mackenzie.proximitytransfer.core.transfer.config

/** Indicates that a local payload or remote manifest exceeds configured resource limits. */
class TransferLimitExceededException(message: String) : IllegalArgumentException(message)
