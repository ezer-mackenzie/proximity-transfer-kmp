package dev.proximitytransfer.transport

interface Transport {
    suspend fun open(): Connection
}

class TransportAlreadyOpenedException(message: String) : IllegalStateException(message)
