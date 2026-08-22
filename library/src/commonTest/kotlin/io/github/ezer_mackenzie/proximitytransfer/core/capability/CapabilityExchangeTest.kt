package io.github.ezer_mackenzie.proximitytransfer.core.capability

import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionState
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.ProtocolReceiver
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.ProtocolSender
import io.github.ezer_mackenzie.proximitytransfer.core.transport.memory.MemoryTransport
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CapabilityExchangeTest {
    @Test
    fun exchangesCapabilitiesAndTransfersWithSameSessions() = runTest {
        val (androidTransport, iphoneTransport) = MemoryTransport.createPair()
        val androidConnection = androidTransport.open()
        val iphoneConnection = iphoneTransport.open()
        val androidExchange = CapabilityExchange(androidConnection)
        val iphoneExchange = CapabilityExchange(iphoneConnection)
        val androidCapabilities = DeviceCapabilities(
            setOf(
                TransportCapability.BLE,
                TransportCapability.LOCAL_NETWORK,
                TransportCapability.WIFI_AWARE,
                TransportCapability.QR_BOOTSTRAP,
            ),
        )
        val iphoneCapabilities = DeviceCapabilities(
            setOf(
                TransportCapability.BLE,
                TransportCapability.LOCAL_NETWORK,
                TransportCapability.MULTIPEER_CONNECTIVITY,
                TransportCapability.QR_BOOTSTRAP,
            ),
        )

        val androidNegotiation = async { androidExchange.negotiate(androidCapabilities) }
        val iphoneNegotiation = async { iphoneExchange.negotiate(iphoneCapabilities) }

        assertEquals(androidNegotiation.await(), iphoneNegotiation.await())
        assertEquals(SessionState.CONNECTED, androidExchange.session.state.value)
        assertEquals(SessionState.CONNECTED, iphoneExchange.session.state.value)

        val sender = ProtocolSender(androidConnection, session = androidExchange.session)
        val receiver = ProtocolReceiver(iphoneConnection, session = iphoneExchange.session)
        val expected = byteArrayOf(1, 2, 3, 4)
        val received = async { receiver.receive() }

        sender.send(expected)

        assertContentEquals(expected, received.await())
        assertEquals(SessionState.COMPLETED, sender.session.state.value)
        assertEquals(SessionState.COMPLETED, receiver.session.state.value)
    }

    @Test
    fun incompatiblePeersFailBothSessions() = runTest {
        val (firstTransport, secondTransport) = MemoryTransport.createPair()
        val first = CapabilityExchange(firstTransport.open())
        val second = CapabilityExchange(secondTransport.open())
        val firstResult = async {
            runCatching {
                first.negotiate(DeviceCapabilities(setOf(TransportCapability.WIFI_AWARE)))
            }
        }
        val secondResult = async {
            runCatching {
                second.negotiate(
                    DeviceCapabilities(setOf(TransportCapability.MULTIPEER_CONNECTIVITY)),
                )
            }
        }

        assertIs<NoCompatibleTransportException>(firstResult.await().exceptionOrNull())
        assertIs<NoCompatibleTransportException>(secondResult.await().exceptionOrNull())
        assertEquals(SessionState.FAILED, first.session.state.value)
        assertEquals(SessionState.FAILED, second.session.state.value)
    }

    @Test
    fun differentLocalPreferencesFailInsteadOfOpeningDifferentTransports() = runTest {
        val (firstTransport, secondTransport) = MemoryTransport.createPair()
        val capabilities = DeviceCapabilities(
            setOf(TransportCapability.BLE, TransportCapability.MEMORY),
        )
        val first = CapabilityExchange(firstTransport.open())
        val second = CapabilityExchange(
            connection = secondTransport.open(),
            negotiator = TransportNegotiator(
                dataPreference = listOf(
                    TransportCapability.MEMORY,
                    TransportCapability.BLE,
                ),
            ),
        )
        val firstResult = async { runCatching { first.negotiate(capabilities) } }
        val secondResult = async { runCatching { second.negotiate(capabilities) } }

        assertIs<NegotiationDisagreementException>(firstResult.await().exceptionOrNull())
        assertIs<NegotiationDisagreementException>(secondResult.await().exceptionOrNull())
        assertEquals(SessionState.FAILED, first.session.state.value)
        assertEquals(SessionState.FAILED, second.session.state.value)
    }
}
