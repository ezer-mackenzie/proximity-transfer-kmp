package io.github.ezer_mackenzie.proximitytransfer.core.transfer

import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

internal class CorruptingConnection(
    private val delegate: Connection,
) : Connection {
    private var sentMessages = 0

    override suspend fun send(data: ByteArray) {
        sentMessages += 1
        val transmitted = data.copyOf()
        if (sentMessages == 2) {
            transmitted[transmitted.lastIndex] = (transmitted.last().toInt() xor 1).toByte()
        }
        delegate.send(transmitted)
    }

    override suspend fun receive(): ByteArray = delegate.receive()

    override suspend fun close() = delegate.close()
}
