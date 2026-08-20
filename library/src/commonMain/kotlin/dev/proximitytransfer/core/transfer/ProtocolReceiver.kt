package dev.proximitytransfer.core.transfer

import dev.proximitytransfer.core.protocol.FrameType
import dev.proximitytransfer.core.protocol.ProtocolFrameCodec
import dev.proximitytransfer.core.transfer.chunk.PayloadChunk
import dev.proximitytransfer.core.transfer.chunk.PayloadChunkCodec
import dev.proximitytransfer.core.transfer.chunk.PayloadReconstructor
import dev.proximitytransfer.core.transport.connection.Connection

/** Receives and validates versioned protocol frames from a [Connection]. */
class ProtocolReceiver(
    private val connection: Connection,
) {
    /** Waits for one complete chunk set and returns the reconstructed payload. */
    suspend fun receive(): ByteArray {
        val firstChunk = receiveChunk()
        val chunks = mutableListOf(firstChunk)
        repeat(firstChunk.total - 1) {
            chunks += receiveChunk()
        }
        return PayloadReconstructor.reconstruct(chunks)
    }

    private suspend fun receiveChunk(): PayloadChunk {
        val frame = ProtocolFrameCodec.decode(connection.receive())
        require(frame.type == FrameType.DATA) {
            "Expected a DATA frame but received ${frame.type}"
        }
        return PayloadChunkCodec.decode(frame.payload)
    }
}
