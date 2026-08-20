package dev.proximitytransfer.transport.connection

/**
 * A bidirectional boundary that preserves each sent [ByteArray] as one received message.
 *
 * Implementations own the transmitted bytes and must not expose mutable storage shared
 * with the caller.
 */
interface Connection {
    /** Sends one complete binary message. */
    suspend fun send(data: ByteArray)

    /** Waits for and returns one complete binary message. */
    suspend fun receive(): ByteArray

    /** Closes this endpoint and releases its transport resources. */
    suspend fun close()
}
