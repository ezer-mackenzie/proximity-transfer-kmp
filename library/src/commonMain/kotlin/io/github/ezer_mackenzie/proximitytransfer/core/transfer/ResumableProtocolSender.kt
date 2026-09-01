package io.github.ezer_mackenzie.proximitytransfer.core.transfer

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256
import io.github.ezer_mackenzie.proximitytransfer.core.integrity.IntegrityVerificationException
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.FrameType
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrame
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrameCodec
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolVersion
import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionState
import io.github.ezer_mackenzie.proximitytransfer.core.session.TransferSession
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunkCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunker
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.config.TransferLimits
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.CompletionAcknowledgementCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.RemoteErrorCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.RemoteTransferException
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.TransferManifest
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.TransferManifestCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.progress.TransferProgress
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.resume.ResumeRequest
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Protocol sender with support for transfer resumption and selective chunk re-transmission.
 */
class ResumableProtocolSender(
    private val connection: Connection,
    chunkSize: Int = PayloadChunker.DEFAULT_CHUNK_SIZE,
    val session: TransferSession = TransferSession(),
    private val limits: TransferLimits = TransferLimits(),
    private val keySpec: io.github.ezer_mackenzie.proximitytransfer.core.security.SessionKeySpec? = null,
) {
    private val chunker = PayloadChunker(chunkSize)
    private val mutableProgress = MutableStateFlow<TransferProgress?>(null)
    private var sequenceNumber = 0L

    val progress: StateFlow<TransferProgress?> = mutableProgress.asStateFlow()

    /** Sends [payload], re-transmitting only missing chunks specified in [resumeRequest] if provided. */
    suspend fun send(payload: ByteArray, resumeRequest: ResumeRequest? = null) {
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

            sendFrame(FrameType.MANIFEST, TransferManifestCodec.encode(manifest))

            val chunksToSend = if (resumeRequest != null) {
                val missingIndices = resumeRequest.bitmap.missingChunkIndices().toSet()
                chunks.filter { it.index in missingIndices }
            } else {
                chunks
            }

            var transferredBytes = payload.size.toLong() - chunksToSend.sumOf { it.size.toLong() }
            mutableProgress.value = TransferProgress(transferredBytes, manifest.payloadSize)

            chunksToSend.forEach { chunk ->
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
        val frame = receiveFrame()
        when (frame.type) {
            FrameType.COMPLETE -> {
                val acknowledgement = CompletionAcknowledgementCodec.decode(frame.payload)
                if (!acknowledgement.sha256.contentEquals(expectedDigest)) {
                    throw IntegrityVerificationException("Receiver acknowledged an unexpected SHA-256 digest")
                }
            }
            FrameType.ERROR -> throw RemoteTransferException(RemoteErrorCodec.decode(frame.payload))
            else -> throw IllegalArgumentException("Expected COMPLETE or ERROR but received ${frame.type}")
        }
    }

    private suspend fun receiveFrame(): ProtocolFrame {
        var bytes = connection.receive()
        if (keySpec != null) {
            bytes = io.github.ezer_mackenzie.proximitytransfer.core.security.FrameEncryptionCodec.decrypt(bytes, keySpec)
        }
        return ProtocolFrameCodec.decode(bytes)
    }

    private suspend fun failSession() {
        if (session.state.value != SessionState.COMPLETED && session.state.value != SessionState.FAILED) {
            session.fail()
        }
    }

    private fun validatePayloadSize(payloadSize: Long) {
        if (payloadSize > limits.maxPayloadBytes) {
            throw IllegalArgumentException("Payload size $payloadSize exceeds limit ${limits.maxPayloadBytes}")
        }
    }

    private fun validateChunkCount(chunkCount: Int) {
        if (chunkCount > limits.maxChunkCount) {
            throw IllegalArgumentException("Chunk count $chunkCount exceeds limit ${limits.maxChunkCount}")
        }
    }

    private suspend fun sendFrame(type: FrameType, payload: ByteArray) {
        val frame = ProtocolFrame(ProtocolVersion.Current, type, payload)
        var bytes = ProtocolFrameCodec.encode(frame)
        if (keySpec != null) {
            bytes = io.github.ezer_mackenzie.proximitytransfer.core.security.FrameEncryptionCodec.encrypt(
                payload = bytes,
                sequenceNumber = sequenceNumber++,
                keySpec = keySpec,
            )
        }
        connection.send(bytes)
    }
}
