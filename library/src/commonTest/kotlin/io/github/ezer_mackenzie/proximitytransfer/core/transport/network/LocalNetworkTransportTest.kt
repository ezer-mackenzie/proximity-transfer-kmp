package io.github.ezer_mackenzie.proximitytransfer.core.transport.network

import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability
import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256
import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionState
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.ProtocolReceiver
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.ProtocolSender
import io.github.ezer_mackenzie.proximitytransfer.core.transport.TransportAlreadyOpenedException
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LocalNetworkTransportTest {

    @Test
    fun advertisesLocalNetworkCapability() {
        val (streamA, _) = InMemoryRawSocketStream.createPair()
        val transport = LocalNetworkTransport(streamProvider = { streamA })
        assertEquals(TransportCapability.LOCAL_NETWORK, transport.capability)
    }

    @Test
    fun throwsWhenOpenedTwice() = runTest {
        val (streamA, _) = InMemoryRawSocketStream.createPair()
        val transport = LocalNetworkTransport(streamProvider = { streamA })

        transport.open()
        assertFailsWith<TransportAlreadyOpenedException> {
            transport.open()
        }
    }

    @Test
    fun transfersPayloadsOverLocalNetworkTransport() = runTest {
        val (streamA, streamB) = InMemoryRawSocketStream.createPair()
        val senderTransport = LocalNetworkTransport(streamProvider = { streamA })
        val receiverTransport = LocalNetworkTransport(streamProvider = { streamB })

        val senderConnection = senderTransport.open()
        val receiverConnection = receiverTransport.open()

        val payloadSizes = listOf(1, 1024, 64 * 1024)

        for (size in payloadSizes) {
            val randomBytes = Random.nextBytes(size)
            val expectedHash = Sha256.digest(randomBytes)

            val sender = ProtocolSender(senderConnection)
            val receiver = ProtocolReceiver(receiverConnection)

            val receiveDeferred = async { receiver.receive() }
            val sendDeferred = async { sender.send(randomBytes) }

            sendDeferred.await()
            val receivedPayload = receiveDeferred.await()

            assertEquals(randomBytes.size, receivedPayload.size)
            assertContentEquals(randomBytes, receivedPayload)
            assertContentEquals(expectedHash, Sha256.digest(receivedPayload))
            assertEquals(SessionState.COMPLETED, sender.session.state.value)
            assertEquals(SessionState.COMPLETED, receiver.session.state.value)
        }
    }
}
