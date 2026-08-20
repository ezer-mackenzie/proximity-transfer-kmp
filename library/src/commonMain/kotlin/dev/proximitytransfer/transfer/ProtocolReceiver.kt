package dev.proximitytransfer.transfer

import dev.proximitytransfer.protocol.FrameType
import dev.proximitytransfer.protocol.ProtocolFrameCodec
import dev.proximitytransfer.transfer.chunk.PayloadChunk
import dev.proximitytransfer.transfer.chunk.PayloadChunkCodec
import dev.proximitytransfer.transfer.chunk.PayloadReconstructor
import dev.proximitytransfer.transport.connection.Connection

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
