package io.github.ezer_mackenzie.proximitytransfer.core.capability

import io.github.ezer_mackenzie.proximitytransfer.core.protocol.FrameType
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrame
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolFrameCodec
import io.github.ezer_mackenzie.proximitytransfer.core.protocol.ProtocolVersion
import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionState
import io.github.ezer_mackenzie.proximitytransfer.core.session.TransferSession
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

/** Exchanges runtime capabilities with one peer and negotiates a compatible route. */
class CapabilityExchange(
    private val connection: Connection,
    private val negotiator: TransportNegotiator = TransportNegotiator(),
    val session: TransferSession = TransferSession(),
) {
    suspend fun negotiate(local: DeviceCapabilities): NegotiationResult {
        session.transitionTo(SessionState.NEGOTIATING)
        try {
            send(local)
            val remote = receive()
            val result = negotiator.negotiate(local, remote)
            sendSelection(result)
            val remoteResult = receiveSelection()
            if (result != remoteResult) {
                throw NegotiationDisagreementException(result, remoteResult)
            }
            session.transitionTo(SessionState.CONNECTED)
            return result
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

    private suspend fun send(capabilities: DeviceCapabilities) {
        val frame = ProtocolFrame(
            version = ProtocolVersion.Current,
            type = FrameType.CAPABILITIES,
            payload = DeviceCapabilitiesCodec.encode(capabilities),
        )
        connection.send(ProtocolFrameCodec.encode(frame))
    }

    private suspend fun receive(): DeviceCapabilities {
        val frame = ProtocolFrameCodec.decode(connection.receive())
        require(frame.type == FrameType.CAPABILITIES) {
            "Expected CAPABILITIES but received ${frame.type}"
        }
        return DeviceCapabilitiesCodec.decode(frame.payload)
    }

    private suspend fun sendSelection(result: NegotiationResult) {
        val frame = ProtocolFrame(
            version = ProtocolVersion.Current,
            type = FrameType.TRANSPORT_SELECTION,
            payload = NegotiationResultCodec.encode(result),
        )
        connection.send(ProtocolFrameCodec.encode(frame))
    }

    private suspend fun receiveSelection(): NegotiationResult {
        val frame = ProtocolFrameCodec.decode(connection.receive())
        require(frame.type == FrameType.TRANSPORT_SELECTION) {
            "Expected TRANSPORT_SELECTION but received ${frame.type}"
        }
        return NegotiationResultCodec.decode(frame.payload)
    }
}
