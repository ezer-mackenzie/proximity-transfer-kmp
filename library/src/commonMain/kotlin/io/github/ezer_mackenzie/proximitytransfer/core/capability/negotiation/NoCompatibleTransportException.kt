package io.github.ezer_mackenzie.proximitytransfer.core.capability.negotiation

/** Indicates that two peers share no capability able to transfer payload data. */
class NoCompatibleTransportException : IllegalStateException(
    "Peers do not share a compatible data transport",
)
