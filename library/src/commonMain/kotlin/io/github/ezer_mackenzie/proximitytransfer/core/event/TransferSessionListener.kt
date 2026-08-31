package io.github.ezer_mackenzie.proximitytransfer.core.event

/**
 * Interface for observing real-time [TransferSessionEvent] occurrences.
 */
interface TransferSessionListener {
    /** Callback invoked whenever a [event] occurs during a session. */
    fun onEvent(event: TransferSessionEvent)
}
