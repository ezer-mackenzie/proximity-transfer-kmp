package dev.proximitytransfer.transport.connection

interface Connection {
    suspend fun send(data: ByteArray)

    suspend fun receive(): ByteArray

    suspend fun close()
}
