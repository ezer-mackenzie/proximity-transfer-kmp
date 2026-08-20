package dev.proximitytransfer.transport.connection

/** Indicates that an operation cannot continue because a connection endpoint is closed. */
class ConnectionClosedException(message: String) : IllegalStateException(message)
