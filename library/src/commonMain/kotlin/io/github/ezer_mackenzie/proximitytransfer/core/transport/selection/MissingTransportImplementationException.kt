package io.github.ezer_mackenzie.proximitytransfer.core.transport.selection

import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability

/** Indicates that a negotiated capability has no registered runtime implementation. */
class MissingTransportImplementationException(
    val capability: TransportCapability,
) : IllegalStateException("No transport implementation is registered for $capability")
