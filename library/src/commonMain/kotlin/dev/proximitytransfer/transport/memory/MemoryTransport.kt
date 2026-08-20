package dev.proximitytransfer.transport.memory

import dev.proximitytransfer.transport.Connection
import dev.proximitytransfer.transport.ConnectionClosedException
import dev.proximitytransfer.transport.Transport
import dev.proximitytransfer.transport.TransportAlreadyOpenedException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MemoryTransport private constructor(
    private val incoming: Channel<ByteArray>,
    private val outgoing: Channel<ByteArray>,
) : Transport {
    private val openMutex = Mutex()
    private var opened = false

    override suspend fun open(): Connection = openMutex.withLock {
        if (opened) {
            throw TransportAlreadyOpenedException("A memory transport endpoint can only be opened once")
        }

        opened = true
        MemoryConnection(incoming, outgoing)
    }

    companion object {
        fun createPair(): Pair<MemoryTransport, MemoryTransport> {
            val firstToSecond = Channel<ByteArray>(Channel.UNLIMITED)
            val secondToFirst = Channel<ByteArray>(Channel.UNLIMITED)

            return MemoryTransport(
                incoming = secondToFirst,
                outgoing = firstToSecond,
            ) to MemoryTransport(
                incoming = firstToSecond,
                outgoing = secondToFirst,
            )
        }
    }
}

private class MemoryConnection(
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
