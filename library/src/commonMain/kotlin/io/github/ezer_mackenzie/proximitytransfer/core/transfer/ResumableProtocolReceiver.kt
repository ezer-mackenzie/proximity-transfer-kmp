package io.github.ezer_mackenzie.proximitytransfer.core.transfer

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.IntegrityVerificationException
import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.FrameType
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrame
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrameCodec
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolVersion
import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionState
import io.github.ezer_mackenzie.proximitytransfer.core.session.TransferSession
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunk
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunkCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadReconstructor
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.config.TransferLimits
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.CompletionAcknowledgement
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.CompletionAcknowledgementCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.RemoteError
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.RemoteErrorCode
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.RemoteErrorCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.TransferManifestCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.progress.TransferProgress
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.resume.ChunkBitmap
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Protocol receiver with support for storing partial chunk state and resuming interrupted transfers.
 */
class ResumableProtocolReceiver(
    private val connection: Connection,
    val session: TransferSession = TransferSession(),
    private val limits: TransferLimits = TransferLimits(),
) {
    private val mutableProgress = MutableStateFlow<TransferProgress?>(null)

    val progress: StateFlow<TransferProgress?> = mutableProgress.asStateFlow()

    /**
     * Receives and reconstructs a payload, resuming from [existingChunks] and [existingBitmap] if provided.
     *
     * @return Pair containing the reconstructed payload bytes and the updated [ChunkBitmap].
     */
    suspend fun receive(
        existingChunks: List<PayloadChunk> = emptyList(),
        existingBitmap: ChunkBitmap? = null,
    ): Pair<ByteArray, ChunkBitmap> {
        beginTransfer()
        try {
            val manifestFrame = ProtocolFrameCodec.decode(connection.receive())
            if (manifestFrame.type != FrameType.MANIFEST) {
                val message = "Expected MANIFEST frame but received ${manifestFrame.type}"
                sendErrorFrame(RemoteErrorCode.MALFORMED_TRANSFER, message)
                throw IllegalArgumentException(message)
            }

            val manifest = TransferManifestCodec.decode(manifestFrame.payload)
            validateLimits(manifest.payloadSize, manifest.chunkCount)

            val bitmap = existingBitmap ?: ChunkBitmap(manifest.chunkCount)
            val chunksMap = mutableMapOf<Int, PayloadChunk>()

            existingChunks.forEach { chunk ->
                chunksMap[chunk.index] = chunk
                bitmap.setReceived(chunk.index)
            }

            var receivedBytes = chunksMap.values.sumOf { it.size.toLong() }
            mutableProgress.value = TransferProgress(receivedBytes, manifest.payloadSize)

            while (bitmap.missingChunkIndices().isNotEmpty()) {
                val frame = ProtocolFrameCodec.decode(connection.receive())
                if (frame.type != FrameType.DATA) {
                    val message = "Expected DATA frame but received ${frame.type}"
                    sendErrorFrame(RemoteErrorCode.MALFORMED_TRANSFER, message)
                    throw IllegalArgumentException(message)
                }

                val chunk = PayloadChunkCodec.decode(frame.payload)
                if (!bitmap.isReceived(chunk.index)) {
                    chunksMap[chunk.index] = chunk
                    bitmap.setReceived(chunk.index)
                    receivedBytes += chunk.size
                    mutableProgress.value = TransferProgress(receivedBytes, manifest.payloadSize)
                }
            }

            session.transitionTo(SessionState.VERIFYING)
            val reconstructedPayload = PayloadReconstructor.reconstruct(chunksMap.values)

            val actualDigest = Sha256.digest(reconstructedPayload)
            if (!actualDigest.contentEquals(manifest.sha256)) {
                val message = "Calculated SHA-256 digest does not match manifest"
                sendErrorFrame(RemoteErrorCode.INTEGRITY_FAILURE, message)
                throw IntegrityVerificationException(message)
            }

            sendFrame(FrameType.COMPLETE, CompletionAcknowledgementCodec.encode(CompletionAcknowledgement(actualDigest)))
            session.transitionTo(SessionState.COMPLETED)

            return Pair(reconstructedPayload, bitmap)
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

    private suspend fun failSession() {
        if (session.state.value != SessionState.COMPLETED && session.state.value != SessionState.FAILED) {
            session.fail()
        }
    }

    private fun validateLimits(payloadSize: Long, chunkCount: Int) {
        if (payloadSize > limits.maxPayloadBytes) {
            throw IllegalArgumentException("Declared payload size $payloadSize exceeds limit ${limits.maxPayloadBytes}")
        }
        if (chunkCount > limits.maxChunkCount) {
            throw IllegalArgumentException("Declared chunk count $chunkCount exceeds limit ${limits.maxChunkCount}")
        }
    }

    private suspend fun sendErrorFrame(code: RemoteErrorCode, message: String) {
        try {
            val payload = RemoteErrorCodec.encode(RemoteError(code, message))
            sendFrame(FrameType.ERROR, payload)
        } catch (_: Exception) {
            // Ignore error reporting failures over broken connections
        }
    }

    private suspend fun sendFrame(type: FrameType, payload: ByteArray) {
        val frame = ProtocolFrame(ProtocolVersion.Current, type, payload)
        connection.send(ProtocolFrameCodec.encode(frame))
    }
}
