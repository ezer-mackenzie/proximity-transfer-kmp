package io.github.ezer_mackenzie.proximitytransfer.core.transfer

import io.github.ezer_mackenzie.proximitytransfer.core.protocol.FrameType
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrame
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrameCodec
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolVersion
import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256
import io.github.ezer_mackenzie.proximitytransfer.core.integrity.IntegrityVerificationException
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunkCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunker
import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionState
import io.github.ezer_mackenzie.proximitytransfer.core.session.TransferSession
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.CompletionAcknowledgementCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.RemoteErrorCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.RemoteTransferException
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.TransferManifest
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.TransferManifestCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

/** Sends application payloads as versioned protocol frames over a [Connection]. */
class ProtocolSender(
    private val connection: Connection,
    chunkSize: Int = PayloadChunker.DEFAULT_CHUNK_SIZE,
) {
    private val chunker = PayloadChunker(chunkSize)
    val session = TransferSession()

    /** Sends one payload and waits until the receiver confirms successful verification. */
    suspend fun send(payload: ByteArray) {
        beginTransfer()
        try {
            val expectedDigest = Sha256.digest(payload)
            val chunks = chunker.split(payload)
            val manifest = TransferManifest(
                payloadSize = payload.size.toLong(),
                chunkCount = chunks.size,
                sha256 = expectedDigest,
            )
            sendFrame(FrameType.MANIFEST, TransferManifestCodec.encode(manifest))

            chunks.forEach { chunk ->
                sendFrame(FrameType.DATA, PayloadChunkCodec.encode(chunk))
            }

            session.transitionTo(SessionState.VERIFYING)
            receiveOutcome(expectedDigest)
            session.transitionTo(SessionState.COMPLETED)
        } catch (exception: Exception) {
            failSession()
            throw exception
        }
    }

    private suspend fun beginTransfer() {
        session.transitionTo(SessionState.CONNECTED)
        session.transitionTo(SessionState.TRANSFERRING)
    }

    private suspend fun receiveOutcome(expectedDigest: ByteArray) {
        val frame = ProtocolFrameCodec.decode(connection.receive())
        when (frame.type) {
            FrameType.COMPLETE -> {
                val acknowledgement = CompletionAcknowledgementCodec.decode(frame.payload)
                if (!acknowledgement.sha256.contentEquals(expectedDigest)) {
                    throw IntegrityVerificationException(
                        "Receiver acknowledged an unexpected SHA-256 digest",
                    )
                }
            }
            FrameType.ERROR -> throw RemoteTransferException(RemoteErrorCodec.decode(frame.payload))
            else -> throw IllegalArgumentException(
                "Expected COMPLETE or ERROR but received ${frame.type}",
            )
        }
    }

    private suspend fun failSession() {
        if (session.state.value != SessionState.COMPLETED && session.state.value != SessionState.FAILED) {
            session.fail()
        }
    }

    private suspend fun sendFrame(type: FrameType, payload: ByteArray) {
        val frame = ProtocolFrame(ProtocolVersion.Current, type, payload)
        connection.send(ProtocolFrameCodec.encode(frame))
    }
}
