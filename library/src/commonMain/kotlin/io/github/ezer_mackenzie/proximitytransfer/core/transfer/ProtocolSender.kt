package io.github.ezer_mackenzie.proximitytransfer.core.transfer

import io.github.ezer_mackenzie.proximitytransfer.core.protocol.FrameType
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrame
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrameCodec
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolVersion
import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunkCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunker
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.TransferManifest
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.TransferManifestCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

/** Sends application payloads as versioned protocol frames over a [Connection]. */
class ProtocolSender(
    private val connection: Connection,
    chunkSize: Int = PayloadChunker.DEFAULT_CHUNK_SIZE,
) {
    private val chunker = PayloadChunker(chunkSize)

    /** Splits and sends one payload without taking ownership of the caller's mutable array. */
    suspend fun send(payload: ByteArray) {
        val chunks = chunker.split(payload)
        val manifest = TransferManifest(
            payloadSize = payload.size.toLong(),
            chunkCount = chunks.size,
            sha256 = Sha256.digest(payload),
        )
        sendFrame(FrameType.MANIFEST, TransferManifestCodec.encode(manifest))

        chunks.forEach { chunk ->
            sendFrame(FrameType.DATA, PayloadChunkCodec.encode(chunk))
        }
    }

    private suspend fun sendFrame(type: FrameType, payload: ByteArray) {
        val frame = ProtocolFrame(ProtocolVersion.Current, type, payload)
        connection.send(ProtocolFrameCodec.encode(frame))
    }
}
