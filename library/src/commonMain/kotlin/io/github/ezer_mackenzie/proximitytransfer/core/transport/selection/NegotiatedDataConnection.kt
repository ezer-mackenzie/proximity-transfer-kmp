package io.github.ezer_mackenzie.proximitytransfer.core.transport.selection

import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.NegotiationResult
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

/** A data connection opened from the transport selected during capability negotiation. */
class NegotiatedDataConnection(
    val connection: Connection,
    val negotiation: NegotiationResult,
)
