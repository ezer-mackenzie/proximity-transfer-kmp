package io.github.ezer_mackenzie.proximitytransfer.core.session

/** Indicates an attempted session-state transition that the protocol does not allow. */
class InvalidSessionTransitionException(
    val from: SessionState,
    val to: SessionState,
) : IllegalStateException("Cannot transition a session from $from to $to")
