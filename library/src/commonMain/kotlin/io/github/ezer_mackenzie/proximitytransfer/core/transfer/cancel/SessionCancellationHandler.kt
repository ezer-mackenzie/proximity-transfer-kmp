package io.github.ezer_mackenzie.proximitytransfer.core.transfer.cancel

import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

/**
 * Sends and receives explicit transfer session cancellation signals over a [Connection].
 */
class SessionCancellationHandler {

    /**
     * Transmits a cancellation signal for [sessionToken] over [connection].
     */
    suspend fun sendCancellation(connection: Connection, sessionToken: String) {
        val payload = CancelSignalCodec.encode(sessionToken)
        connection.send(payload)
    }

    /**
     * Receives and decodes a cancellation signal [sessionToken] from [connection].
     */
    suspend fun receiveCancellation(connection: Connection): String {
        val bytes = connection.receive()
        return CancelSignalCodec.decode(bytes)
    }
}
