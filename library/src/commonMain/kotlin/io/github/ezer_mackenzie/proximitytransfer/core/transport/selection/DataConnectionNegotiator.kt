package io.github.ezer_mackenzie.proximitytransfer.core.transport.selection

import io.github.ezer_mackenzie.proximitytransfer.core.capability.exchange.CapabilityExchange
import io.github.ezer_mackenzie.proximitytransfer.core.capability.negotiation.TransportNegotiator
import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionState
import io.github.ezer_mackenzie.proximitytransfer.core.session.TransferSession
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

/** Negotiates over a control connection and opens the selected data connection. */
class DataConnectionNegotiator(
    controlConnection: Connection,
    private val registry: DataTransportRegistry,
    transportNegotiator: TransportNegotiator = TransportNegotiator(),
    val session: TransferSession = TransferSession(),
) {
    private val capabilityExchange = CapabilityExchange(
        connection = controlConnection,
        negotiator = transportNegotiator,
        session = session,
    )

    /** Exchanges registered capabilities and opens the mutually selected transport. */
    suspend fun connect(): NegotiatedDataConnection {
        val negotiation = capabilityExchange.negotiate(registry.capabilities)
        try {
            return NegotiatedDataConnection(
                connection = registry.open(negotiation),
                negotiation = negotiation,
            )
        } catch (exception: Exception) {
            if (
                session.state.value != SessionState.COMPLETED &&
                session.state.value != SessionState.FAILED
            ) {
                session.fail()
            }
            throw exception
        }
    }
}
