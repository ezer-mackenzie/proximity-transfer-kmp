package dev.proximitytransfer.core.transfer

import dev.proximitytransfer.core.protocol.FrameType
import dev.proximitytransfer.core.protocol.ProtocolFrame
import dev.proximitytransfer.core.protocol.ProtocolFrameCodec
import dev.proximitytransfer.core.protocol.ProtocolVersion
import dev.proximitytransfer.core.transfer.chunk.PayloadChunkCodec
import dev.proximitytransfer.core.transfer.chunk.PayloadChunker
import dev.proximitytransfer.core.transport.connection.Connection

/** Sends application payloads as versioned protocol frames over a [Connection]. */
class ProtocolSender(
    private val connection: Connection,
    chunkSize: Int = PayloadChunker.DEFAULT_CHUNK_SIZE,
) {
    private val chunker = PayloadChunker(chunkSize)

    /** Splits and sends one payload without taking ownership of the caller's mutable array. */
    suspend fun send(payload: ByteArray) {
        chunker.split(payload).forEach { chunk ->
            val frame = ProtocolFrame(
                version = ProtocolVersion.Current,
                type = FrameType.DATA,
                payload = PayloadChunkCodec.encode(chunk),
            )
            connection.send(ProtocolFrameCodec.encode(frame))
        }
    }
}
