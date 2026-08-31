package io.github.ezer_mackenzie.proximitytransfer.core.event

import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionState
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.progress.TransferProgressMetrics

/**
 * Sealed hierarchy representing real-time transfer session events.
 */
sealed class TransferSessionEvent {
    abstract val sessionId: String

    data class StateChanged(
        override val sessionId: String,
        val previousState: SessionState,
        val newState: SessionState,
    ) : TransferSessionEvent()

    data class ChunkReceived(
        override val sessionId: String,
        val chunkIndex: Int,
        val totalChunks: Int,
    ) : TransferSessionEvent()

    data class ProgressUpdated(
        override val sessionId: String,
        val metrics: TransferProgressMetrics,
    ) : TransferSessionEvent()

    data class Completed(
        override val sessionId: String,
        val totalBytesTransferred: Long,
        val sha256Hex: String,
    ) : TransferSessionEvent()

    data class Failed(
        override val sessionId: String,
        val error: Throwable,
    ) : TransferSessionEvent()

    data class Cancelled(
        override val sessionId: String,
        val reason: String,
    ) : TransferSessionEvent()
}
