package io.github.ezer_mackenzie.proximitytransfer.core.transport

import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

/** Opens a binary message connection using a concrete transport mechanism. */
interface Transport {
    /** Opens this transport endpoint and returns its connection. */
    suspend fun open(): Connection
}
