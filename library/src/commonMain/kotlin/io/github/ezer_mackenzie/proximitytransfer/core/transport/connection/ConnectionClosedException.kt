package io.github.ezer_mackenzie.proximitytransfer.core.transport.connection

/** Indicates that an operation cannot continue because a connection endpoint is closed. */
class ConnectionClosedException(message: String) : IllegalStateException(message)
