package dev.proximitytransfer.core.transport

/** Indicates that a single-use transport endpoint was opened more than once. */
class TransportAlreadyOpenedException(message: String) : IllegalStateException(message)
