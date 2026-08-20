package dev.proximitytransfer.transport

import dev.proximitytransfer.transport.connection.Connection

interface Transport {
    suspend fun open(): Connection
}
