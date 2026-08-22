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
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.config.TransferLimitExceededException
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.config.TransferLimits
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.TransferManifest
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.TransferManifestCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.progress.TransferProgress
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Sends application payloads as versioned protocol frames over a [Connection]. */
class ProtocolSender(
    private val connection: Connection,
    chunkSize: Int = PayloadChunker.DEFAULT_CHUNK_SIZE,
    val session: TransferSession = TransferSession(),
    private val limits: TransferLimits = TransferLimits(),
) {
    private val chunker = PayloadChunker(chunkSize)
    private val mutableProgress = MutableStateFlow<TransferProgress?>(null)

    /** Current payload-byte progress, or `null` before transfer starts. */
    val progress: StateFlow<TransferProgress?> = mutableProgress.asStateFlow()

    /** Sends one payload and waits until the receiver confirms successful verification. */
    suspend fun send(payload: ByteArray) {
        beginTransfer()
        try {
            validatePayloadSize(payload.size.toLong())
            val expectedDigest = Sha256.digest(payload)
            val chunks = chunker.split(payload)
            validateChunkCount(chunks.size)
            val manifest = TransferManifest(
                payloadSize = payload.size.toLong(),
                chunkCount = chunks.size,
                sha256 = expectedDigest,
            )
            mutableProgress.value = TransferProgress(0, manifest.payloadSize)
            sendFrame(FrameType.MANIFEST, TransferManifestCodec.encode(manifest))

            var transferredBytes = 0L
            chunks.forEach { chunk ->
                sendFrame(FrameType.DATA, PayloadChunkCodec.encode(chunk))
                transferredBytes += chunk.size
                mutableProgress.value = TransferProgress(transferredBytes, manifest.payloadSize)
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
        if (session.state.value == SessionState.IDLE) {
            session.transitionTo(SessionState.CONNECTED)
        }
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

    private fun validatePayloadSize(payloadSize: Long) {
        if (payloadSize > limits.maxPayloadBytes) {
            throw TransferLimitExceededException(
                "Payload size $payloadSize exceeds limit ${limits.maxPayloadBytes}",
            )
        }
    }

    private fun validateChunkCount(chunkCount: Int) {
        if (chunkCount > limits.maxChunkCount) {
            throw TransferLimitExceededException(
                "Chunk count $chunkCount exceeds limit ${limits.maxChunkCount}",
            )
        }
    }

    private suspend fun sendFrame(type: FrameType, payload: ByteArray) {
        val frame = ProtocolFrame(ProtocolVersion.Current, type, payload)
        connection.send(ProtocolFrameCodec.encode(frame))
    }
}
