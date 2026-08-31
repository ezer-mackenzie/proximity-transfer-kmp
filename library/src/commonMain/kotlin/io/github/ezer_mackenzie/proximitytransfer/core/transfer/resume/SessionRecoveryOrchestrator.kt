package io.github.ezer_mackenzie.proximitytransfer.core.transfer.resume

import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionSnapshot
import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionStore

/**
 * Orchestrates transfer session recovery by querying [SessionStore] checkpoints and building [ResumeRequest] models.
 */
class SessionRecoveryOrchestrator(
    private val sessionStore: SessionStore,
) {
    /**
     * Checks if a previous snapshot exists for [sessionId].
     *
     * @return Formatted [ResumeRequest] if a valid checkpoint was found, or `null` if no checkpoint exists.
     */
    suspend fun createResumeRequestIfAvailable(sessionId: String): ResumeRequest? {
        val snapshot = sessionStore.getSnapshot(sessionId) ?: return null
        val totalChunks = ((snapshot.totalSize + snapshot.chunkSize - 1) / snapshot.chunkSize).toInt()
        val bitmap = ChunkBitmap.decode(totalChunks, snapshot.bitmapBytes)
        val lastReceived = (0 until totalChunks).lastOrNull { bitmap.isReceived(it) } ?: -1

        return ResumeRequest(
            sessionToken = snapshot.sessionId.encodeToByteArray(),
            lastReceivedChunkIndex = lastReceived,
            bitmap = bitmap,
        )
    }

    /** Saves an updated checkpoint [snapshot] into [SessionStore]. */
    suspend fun saveCheckpoint(snapshot: SessionSnapshot) {
        sessionStore.saveSnapshot(snapshot)
    }

    /** Clears a completed session checkpoint [sessionId] from [SessionStore]. */
    suspend fun clearCheckpoint(sessionId: String) {
        sessionStore.clearSnapshot(sessionId)
    }
}
