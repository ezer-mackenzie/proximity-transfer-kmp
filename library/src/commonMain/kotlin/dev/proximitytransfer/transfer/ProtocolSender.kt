package dev.proximitytransfer.transfer

import dev.proximitytransfer.protocol.FrameType
import dev.proximitytransfer.protocol.ProtocolFrame
import dev.proximitytransfer.protocol.ProtocolFrameCodec
import dev.proximitytransfer.protocol.ProtocolVersion
import dev.proximitytransfer.transfer.chunk.PayloadChunkCodec
import dev.proximitytransfer.transfer.chunk.PayloadChunker
import dev.proximitytransfer.transport.connection.Connection

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
