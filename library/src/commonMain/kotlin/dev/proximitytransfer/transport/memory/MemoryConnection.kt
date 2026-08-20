package dev.proximitytransfer.transport.memory

import dev.proximitytransfer.transport.Connection
import dev.proximitytransfer.transport.ConnectionClosedException
import kotlinx.coroutines.channels.Channel

internal class MemoryConnection(
    private val incoming: Channel<ByteArray>,
    private val outgoing: Channel<ByteArray>,
) : Connection {
    override suspend fun send(data: ByteArray) {
        if (outgoing.trySend(data.copyOf()).isFailure) {
            throw ConnectionClosedException("The remote connection is closed")
        }
    }

    override suspend fun receive(): ByteArray {
        val result = incoming.receiveCatching()
        return result.getOrNull()?.copyOf()
            ?: throw ConnectionClosedException("The remote connection is closed")
    }

    override suspend fun close() {
        outgoing.close()
        incoming.cancel()
    }
}
