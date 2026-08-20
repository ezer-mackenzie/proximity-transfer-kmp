package io.github.ezer_mackenzie.proximitytransfer.core.transfer

import io.github.ezer_mackenzie.proximitytransfer.core.protocol.FrameType
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrameCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunk
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunkCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadReconstructor
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

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
