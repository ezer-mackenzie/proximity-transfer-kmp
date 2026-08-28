package io.github.ezer_mackenzie.proximitytransfer.core.transport.ios

import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability
import io.github.ezer_mackenzie.proximitytransfer.core.transport.Transport
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

/**
 * iOS-specific Multipeer Connectivity transport foundation.
 * Bridges Apple Multipeer Connectivity (MCSession / MCNearbyServiceAdvertiser) to the common Connection interface.
 */
class IosMultipeerTransport(
    val serviceType: String,
    val peerDisplayName: String,
) : Transport {

    override suspend fun open(): Connection {
        throw UnsupportedOperationException(
            "iOS Multipeer Connectivity transport requires physical iOS device APIs (MCSession)"
        )
    }
}
