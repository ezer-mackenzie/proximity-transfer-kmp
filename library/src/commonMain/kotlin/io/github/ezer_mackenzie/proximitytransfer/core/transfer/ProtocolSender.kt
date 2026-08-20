package io.github.ezer_mackenzie.proximitytransfer.core.transfer

import io.github.ezer_mackenzie.proximitytransfer.core.protocol.FrameType
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrame
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrameCodec
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolVersion
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunkCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunker
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

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
