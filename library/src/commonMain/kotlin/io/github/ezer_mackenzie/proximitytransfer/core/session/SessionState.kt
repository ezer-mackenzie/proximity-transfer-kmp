package io.github.ezer_mackenzie.proximitytransfer.core.session

/** The externally observable lifecycle state of one transfer session. */
enum class SessionState {
    IDLE,
    DISCOVERING,
    NEGOTIATING,
    CONNECTED,
    TRANSFERRING,
    VERIFYING,
    COMPLETED,
    FAILED,
}
