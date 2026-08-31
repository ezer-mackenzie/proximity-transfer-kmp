package io.github.ezer_mackenzie.proximitytransfer.core.session

/**
 * Interface for saving, retrieving, and clearing transfer session state snapshots.
 */
interface SessionStore {
    /** Persists a [snapshot] for an interrupted transfer session. */
    suspend fun saveSnapshot(snapshot: SessionSnapshot)

    /** Retrieves the persisted [SessionSnapshot] for [sessionId], or `null` if none exists. */
    suspend fun getSnapshot(sessionId: String): SessionSnapshot?

    /** Clears the persisted snapshot associated with [sessionId]. */
    suspend fun clearSnapshot(sessionId: String)
}
