package io.github.ezer_mackenzie.proximitytransfer.core.transport.selection

import io.github.ezer_mackenzie.proximitytransfer.core.capability.DeviceCapabilities
import io.github.ezer_mackenzie.proximitytransfer.core.capability.NegotiationResult
import io.github.ezer_mackenzie.proximitytransfer.core.capability.TransportCapability
import io.github.ezer_mackenzie.proximitytransfer.core.transport.Transport
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

/** Resolves negotiated data capabilities to transport implementations available at runtime. */
class DataTransportRegistry(
    transports: Map<TransportCapability, Transport>,
) {
    private val transports = transports.toMap()

    init {
        require(this.transports.isNotEmpty()) {
            "At least one data transport implementation must be registered"
        }
        require(this.transports.keys.all { it.supportsDataTransfer }) {
            "A data transport registry cannot contain bootstrap-only capabilities"
        }
    }

    /** Capabilities backed by concrete data transport implementations in this registry. */
    val capabilities: DeviceCapabilities = DeviceCapabilities(this.transports.keys)

    /** Opens the concrete data transport selected by [negotiation]. */
    suspend fun open(negotiation: NegotiationResult): Connection {
        val capability = negotiation.dataTransport
        val transport = transports[capability]
            ?: throw MissingTransportImplementationException(capability)
        return transport.open()
    }
}
