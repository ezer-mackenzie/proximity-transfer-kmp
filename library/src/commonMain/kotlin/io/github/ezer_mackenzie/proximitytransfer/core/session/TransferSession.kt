package io.github.ezer_mackenzie.proximitytransfer.core.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Concurrency-safe state machine for one transfer lifecycle.
 *
 * State changes are exposed as a read-only [StateFlow]. Terminal states cannot
 * transition further, and invalid transitions leave the current state unchanged.
 */
class TransferSession {
    private val transitionMutex = Mutex()
    private val mutableState = MutableStateFlow(SessionState.IDLE)

    val state: StateFlow<SessionState> = mutableState.asStateFlow()

    /** Moves to [next] when the transition is valid. */
    suspend fun transitionTo(next: SessionState) {
        transitionMutex.withLock {
            val current = mutableState.value
            if (next !in allowedTransitions.getValue(current)) {
                throw InvalidSessionTransitionException(current, next)
            }
            mutableState.value = next
        }
    }

    /** Moves any nonterminal session to [SessionState.FAILED]. */
    suspend fun fail() {
        transitionTo(SessionState.FAILED)
    }

    private companion object {
        val allowedTransitions = mapOf(
            SessionState.IDLE to setOf(
                SessionState.DISCOVERING,
                SessionState.CONNECTED,
                SessionState.FAILED,
            ),
            SessionState.DISCOVERING to setOf(
                SessionState.NEGOTIATING,
                SessionState.FAILED,
            ),
            SessionState.NEGOTIATING to setOf(
                SessionState.CONNECTED,
                SessionState.FAILED,
            ),
            SessionState.CONNECTED to setOf(
                SessionState.TRANSFERRING,
                SessionState.FAILED,
            ),
            SessionState.TRANSFERRING to setOf(
                SessionState.VERIFYING,
                SessionState.FAILED,
            ),
            SessionState.VERIFYING to setOf(
                SessionState.COMPLETED,
                SessionState.FAILED,
            ),
            SessionState.COMPLETED to emptySet(),
            SessionState.FAILED to emptySet(),
        )
    }
}
