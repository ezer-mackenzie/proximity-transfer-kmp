package dev.proximitytransfer.transport.memory

import dev.proximitytransfer.transport.ConnectionClosedException
import dev.proximitytransfer.transport.TransportAlreadyOpenedException
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class MemoryTransportTest {
    @Test
    fun transfersBytesInBothDirections() = runTest {
        val (firstTransport, secondTransport) = MemoryTransport.createPair()
        val first = firstTransport.open()
        val second = secondTransport.open()

        first.send(byteArrayOf(1, 2, 3))
        second.send(byteArrayOf(4, 5, 6))

        assertContentEquals(byteArrayOf(1, 2, 3), second.receive())
        assertContentEquals(byteArrayOf(4, 5, 6), first.receive())
    }

    @Test
    fun receiveWaitsForData() = runTest {
        val (firstTransport, secondTransport) = MemoryTransport.createPair()
        val first = firstTransport.open()
        val second = secondTransport.open()
        val received = async { second.receive() }

        first.send(byteArrayOf(42))

        assertContentEquals(byteArrayOf(42), received.await())
    }

    @Test
    fun copiesTransferredBytes() = runTest {
        val (firstTransport, secondTransport) = MemoryTransport.createPair()
        val first = firstTransport.open()
        val second = secondTransport.open()
        val source = byteArrayOf(1, 2, 3)

        first.send(source)
        source[0] = 9

        assertContentEquals(byteArrayOf(1, 2, 3), second.receive())
    }

    @Test
    fun endpointCanOnlyBeOpenedOnce() = runTest {
        val (transport, _) = MemoryTransport.createPair()
        transport.open()

        assertFailsWith<TransportAlreadyOpenedException> {
            transport.open()
        }
    }

    @Test
    fun closingOneEndpointClosesThePair() = runTest {
        val (firstTransport, secondTransport) = MemoryTransport.createPair()
        val first = firstTransport.open()
        val second = secondTransport.open()

        first.close()

        assertFailsWith<ConnectionClosedException> { second.receive() }
        assertFailsWith<ConnectionClosedException> { second.send(byteArrayOf(1)) }
    }
}
