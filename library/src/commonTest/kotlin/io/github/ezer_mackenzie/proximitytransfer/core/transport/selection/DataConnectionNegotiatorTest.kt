package io.github.ezer_mackenzie.proximitytransfer.core.transport.selection

import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability
import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionState
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.ProtocolReceiver
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.ProtocolSender
import io.github.ezer_mackenzie.proximitytransfer.core.transport.Transport
import io.github.ezer_mackenzie.proximitytransfer.core.transport.memory.MemoryTransport
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DataConnectionNegotiatorTest {
    @Test
    fun negotiatesOpensAndTransfersOverSeparateDataConnection() = runTest {
        val (firstControlTransport, secondControlTransport) = MemoryTransport.createPair()
        val (firstDataTransport, secondDataTransport) = MemoryTransport.createPair()
        val first = DataConnectionNegotiator(
            controlConnection = firstControlTransport.open(),
            registry = memoryRegistry(firstDataTransport),
        )
        val second = DataConnectionNegotiator(
            controlConnection = secondControlTransport.open(),
            registry = memoryRegistry(secondDataTransport),
        )

        val firstConnection = async { first.connect() }
        val secondConnection = async { second.connect() }
        val senderConnection = firstConnection.await()
        val receiverConnection = secondConnection.await()
        val sender = ProtocolSender(senderConnection.connection, session = first.session)
        val receiver = ProtocolReceiver(receiverConnection.connection, session = second.session)
        val expected = byteArrayOf(5, 4, 3, 2, 1)
        val received = async { receiver.receive() }

        sender.send(expected)

        assertEquals(TransportCapability.MEMORY, senderConnection.negotiation.dataTransport)
        assertEquals(TransportCapability.MEMORY, receiverConnection.negotiation.dataTransport)
        assertContentEquals(expected, received.await())
        assertEquals(SessionState.COMPLETED, sender.session.state.value)
        assertEquals(SessionState.COMPLETED, receiver.session.state.value)
    }

    @Test
    fun failsSessionWhenSelectedTransportCannotOpen() = runTest {
        val (firstControlTransport, secondControlTransport) = MemoryTransport.createPair()
        val (_, workingDataTransport) = MemoryTransport.createPair()
        val failingTransport = object : Transport {
            override suspend fun open() = throw IllegalStateException("Data transport unavailable")
        }
        val first = DataConnectionNegotiator(
            controlConnection = firstControlTransport.open(),
            registry = memoryRegistry(failingTransport),
        )
        val second = DataConnectionNegotiator(
            controlConnection = secondControlTransport.open(),
            registry = memoryRegistry(workingDataTransport),
        )
        val secondConnection = async { second.connect() }

        assertFailsWith<IllegalStateException> { first.connect() }
        secondConnection.await()

        assertEquals(SessionState.FAILED, first.session.state.value)
        assertEquals(SessionState.CONNECTED, second.session.state.value)
    }

    private fun memoryRegistry(transport: Transport): DataTransportRegistry =
        DataTransportRegistry(mapOf(TransportCapability.MEMORY to transport))
}
