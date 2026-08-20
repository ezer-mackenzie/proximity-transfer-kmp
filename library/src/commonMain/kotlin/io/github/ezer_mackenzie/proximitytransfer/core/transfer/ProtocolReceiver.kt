package io.github.ezer_mackenzie.proximitytransfer.core.transfer

import io.github.ezer_mackenzie.proximitytransfer.core.protocol.FrameType
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrameCodec
import io.github.ezer_mackenzie.proximitytransfer.core.integrity.IntegrityVerificationException
import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunk
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunkCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadReconstructor
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.TransferManifest
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.TransferManifestCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

/** Receives and validates versioned protocol frames from a [Connection]. */
class ProtocolReceiver(
    private val connection: Connection,
) {
    /** Waits for one complete chunk set and returns the reconstructed payload. */
    suspend fun receive(): ByteArray {
        val manifest = receiveManifest()
        val chunks = mutableListOf<PayloadChunk>()
        repeat(manifest.chunkCount) {
            chunks += receiveChunk()
        }
        val payload = PayloadReconstructor.reconstruct(chunks)
        verify(payload, manifest)
        return payload
    }

    private suspend fun receiveManifest(): TransferManifest {
        val frame = ProtocolFrameCodec.decode(connection.receive())
        require(frame.type == FrameType.MANIFEST) {
            "Expected a MANIFEST frame but received ${frame.type}"
        }
        return TransferManifestCodec.decode(frame.payload)
    }

    private suspend fun receiveChunk(): PayloadChunk {
        val frame = ProtocolFrameCodec.decode(connection.receive())
        require(frame.type == FrameType.DATA) {
            "Expected a DATA frame but received ${frame.type}"
        }
        return PayloadChunkCodec.decode(frame.payload)
    }

    private fun verify(payload: ByteArray, manifest: TransferManifest) {
        if (payload.size.toLong() != manifest.payloadSize) {
            throw IntegrityVerificationException(
                "Expected ${manifest.payloadSize} bytes but reconstructed ${payload.size}",
            )
        }
        if (!Sha256.digest(payload).contentEquals(manifest.sha256)) {
            throw IntegrityVerificationException("Reconstructed payload failed SHA-256 verification")
        }
    }
}
