package io.github.ezer_mackenzie.proximitytransfer.core.transport.network

import io.github.ezer_mackenzie.proximitytransfer.core.transfer.config.TransferLimits
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.ConnectionClosedException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A network socket-backed [Connection] that uses [StreamFramingCodec] to transmit
 * and receive discrete binary messages over a continuous [RawSocketStream].
 */
class SocketConnection(
    private val stream: RawSocketStream,
    private val maxPayloadSizeBytes: Long = TransferLimits.DEFAULT_MAX_PAYLOAD_BYTES,
) : Connection {
    private val sendMutex = Mutex()
    private val receiveMutex = Mutex()
    private var isClosed = false

    override suspend fun send(data: ByteArray) {
        sendMutex.withLock {
            if (isClosed) {
                throw ConnectionClosedException("Connection is closed")
            }
            val copy = data.copyOf()
            val framed = StreamFramingCodec.encode(copy)
            stream.writeFully(framed)
        }
    }

    override suspend fun receive(): ByteArray {
        return receiveMutex.withLock {
            if (isClosed) {
                throw ConnectionClosedException("Connection is closed")
            }
            val headerBytes = ByteArray(StreamFramingCodec.HEADER_SIZE_BYTES)
            stream.readFully(headerBytes)

            val payloadLength = StreamFramingCodec.readHeader(
                headerBytes = headerBytes,
                maxPayloadSizeBytes = maxPayloadSizeBytes,
            )
            val payload = ByteArray(payloadLength)
            if (payloadLength > 0) {
                stream.readFully(payload)
            }
            payload
        }
    }

    override suspend fun close() {
        if (!isClosed) {
            isClosed = true
            stream.close()
        }
    }
}
