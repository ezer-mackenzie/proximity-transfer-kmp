package io.github.ezer_mackenzie.proximitytransfer.core.transport.network

import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.ConnectionClosedException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException

/** An in-memory paired implementation of [RawSocketStream] for multiplatform testing. */
class InMemoryRawSocketStream private constructor(
    private val readChannel: Channel<Byte>,
    private val writeChannel: Channel<Byte>,
) : RawSocketStream {
    private var isClosed = false

    override suspend fun readFully(buffer: ByteArray, offset: Int, count: Int) {
        if (isClosed) throw ConnectionClosedException("Stream is closed")
        var bytesRead = 0
        try {
            while (bytesRead < count) {
                val byte = readChannel.receive()
                buffer[offset + bytesRead] = byte
                bytesRead++
            }
        } catch (e: ClosedReceiveChannelException) {
            throw ConnectionClosedException("Stream closed prematurely while reading")
        }
    }

    override suspend fun writeFully(buffer: ByteArray, offset: Int, count: Int) {
        if (isClosed) throw ConnectionClosedException("Stream is closed")
        try {
            for (i in 0 until count) {
                writeChannel.send(buffer[offset + i])
            }
        } catch (e: Exception) {
            throw ConnectionClosedException("Stream closed while writing")
        }
    }

    override suspend fun close() {
        if (!isClosed) {
            isClosed = true
            readChannel.close()
            writeChannel.close()
        }
    }

    companion object {
        /** Creates a pair of interconnected [InMemoryRawSocketStream] instances. */
        fun createPair(bufferCapacity: Int = Channel.UNLIMITED): Pair<InMemoryRawSocketStream, InMemoryRawSocketStream> {
            val channelAtoB = Channel<Byte>(bufferCapacity)
            val channelBtoA = Channel<Byte>(bufferCapacity)

            val streamA = InMemoryRawSocketStream(readChannel = channelBtoA, writeChannel = channelAtoB)
            val streamB = InMemoryRawSocketStream(readChannel = channelAtoB, writeChannel = channelBtoA)

            return Pair(streamA, streamB)
        }
    }
}
