package io.github.ezer_mackenzie.proximitytransfer.core.transport.selection

import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability
import io.github.ezer_mackenzie.proximitytransfer.core.transport.Transport
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

/**
 * Selects and opens an available [Transport] ordered by capability preference, falling back upon connection failure.
 */
class TransportFallbackSelector(
    private val preferredPriorities: List<TransportCapability> = listOf(
        TransportCapability.LOCAL_NETWORK,
        TransportCapability.WIFI_DIRECT,
        TransportCapability.WIFI_AWARE,
        TransportCapability.BLE,
        TransportCapability.MEMORY,
    ),
) {
    /**
     * Attempts to open a [Connection] by trying available transports in order of priority preference.
     *
     * @param transports Map of available [TransportCapability] to concrete [Transport] candidates.
     * @return Open [Connection] from the highest priority succeeding transport.
     * @throws IllegalStateException If no transports succeed or no candidates match available capabilities.
     */
    suspend fun openFirstAvailable(transports: Map<TransportCapability, Transport>): Connection {
        val orderedCandidates = preferredPriorities.mapNotNull { capability ->
            transports[capability]?.let { capability to it }
        }

        if (orderedCandidates.isEmpty()) {
            throw IllegalStateException("No compatible transports found in capability list")
        }

        var lastException: Throwable? = null
        for ((_, transport) in orderedCandidates) {
            try {
                return transport.open()
            } catch (e: Throwable) {
                lastException = e
            }
        }

        throw IllegalStateException(
            "All available transports failed to open connection",
            lastException,
        )
    }
}
