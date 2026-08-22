package io.github.ezer_mackenzie.proximitytransfer.core.transfer

import io.github.ezer_mackenzie.proximitytransfer.core.protocol.FrameType
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrameCodec
import io.github.ezer_mackenzie.proximitytransfer.core.integrity.IntegrityVerificationException
import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrame
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolVersion
import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionState
import io.github.ezer_mackenzie.proximitytransfer.core.session.TransferSession
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunk
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunkCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadReconstructor
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.TransferManifest
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.manifest.TransferManifestCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.CompletionAcknowledgement
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.CompletionAcknowledgementCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.RemoteError
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.RemoteErrorCode
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.RemoteErrorCodec
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.progress.TransferProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Receives and validates versioned protocol frames from a [Connection]. */
class ProtocolReceiver(
    private val connection: Connection,
    val session: TransferSession = TransferSession(),
) {
    private val mutableProgress = MutableStateFlow<TransferProgress?>(null)

    /** Current payload-byte progress, or `null` before a manifest is received. */
    val progress: StateFlow<TransferProgress?> = mutableProgress.asStateFlow()

    /** Waits for one complete chunk set and returns the reconstructed payload. */
    suspend fun receive(): ByteArray {
        beginTransfer()
        try {
            val manifest = receiveManifest()
            mutableProgress.value = TransferProgress(0, manifest.payloadSize)
            val chunks = mutableListOf<PayloadChunk>()
            var transferredBytes = 0L
            repeat(manifest.chunkCount) {
                val chunk = receiveChunk()
                chunks += chunk
                transferredBytes += chunk.size
                if (transferredBytes <= manifest.payloadSize) {
                    mutableProgress.value = TransferProgress(
                        transferredBytes = transferredBytes,
                        totalBytes = manifest.payloadSize,
                    )
                }
            }
            val payload = PayloadReconstructor.reconstruct(chunks)
            session.transitionTo(SessionState.VERIFYING)
            verify(payload, manifest)
            sendComplete(manifest.sha256)
            session.transitionTo(SessionState.COMPLETED)
            return payload
        } catch (exception: IntegrityVerificationException) {
            sendError(RemoteErrorCode.INTEGRITY_FAILURE, "Payload integrity verification failed")
            failSession()
            throw exception
        } catch (exception: IllegalArgumentException) {
            sendError(RemoteErrorCode.MALFORMED_TRANSFER, "Malformed transfer")
            failSession()
            throw exception
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

    private suspend fun sendComplete(digest: ByteArray) {
        sendFrame(
            FrameType.COMPLETE,
            CompletionAcknowledgementCodec.encode(CompletionAcknowledgement(digest)),
        )
    }

    private suspend fun sendError(code: RemoteErrorCode, message: String?) {
        try {
            sendFrame(
                FrameType.ERROR,
                RemoteErrorCodec.encode(RemoteError(code, message.orEmpty())),
            )
        } catch (_: Exception) {
            // Preserve the original receive failure when the connection cannot report it.
        }
    }

    private suspend fun sendFrame(type: FrameType, payload: ByteArray) {
        val frame = ProtocolFrame(ProtocolVersion.Current, type, payload)
        connection.send(ProtocolFrameCodec.encode(frame))
    }

    private suspend fun failSession() {
        if (session.state.value != SessionState.COMPLETED && session.state.value != SessionState.FAILED) {
            session.fail()
        }
    }
}
