package dev.proximitytransfer.core.transport

import dev.proximitytransfer.core.transport.connection.Connection

/** Opens a binary message connection using a concrete transport mechanism. */
interface Transport {
    /** Opens this transport endpoint and returns its connection. */
    suspend fun open(): Connection
}
