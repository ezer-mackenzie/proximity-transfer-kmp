package io.github.ezer_mackenzie.proximitytransfer.core.session

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Thread-safe in-memory implementation of [SessionStore].
 */
class InMemorySessionStore : SessionStore {
    private val mutex = Mutex()
    private val snapshots = mutableMapOf<String, SessionSnapshot>()

    override suspend fun saveSnapshot(snapshot: SessionSnapshot) {
        mutex.withLock {
            snapshots[snapshot.sessionId] = snapshot
        }
    }

    override suspend fun getSnapshot(sessionId: String): SessionSnapshot? {
        return mutex.withLock {
            snapshots[sessionId]
        }
    }

    override suspend fun clearSnapshot(sessionId: String) {
        mutex.withLock {
            snapshots.remove(sessionId)
        }
    }
}
