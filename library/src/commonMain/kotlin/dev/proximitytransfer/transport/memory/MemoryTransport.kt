package dev.proximitytransfer.transport.memory

import dev.proximitytransfer.transport.Transport
import dev.proximitytransfer.transport.TransportAlreadyOpenedException
import dev.proximitytransfer.transport.connection.Connection
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
