package io.github.ezer_mackenzie.proximitytransfer.core.transfer

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.IntegrityVerificationException
import io.github.ezer_mackenzie.proximitytransfer.core.transport.memory.MemoryTransport
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class ProtocolTransferTest {
    @Test
    fun transfersPayloadsOfMultipleSizes() = runTest {
        val sizes = listOf(0, 1, 255, 1_024, 64 * 1_024)

        sizes.forEach { size ->
            val expected = Random(size).nextBytes(size)
            val (senderTransport, receiverTransport) = MemoryTransport.createPair()
            val sender = ProtocolSender(senderTransport.open(), chunkSize = 127)
            val receiver = ProtocolReceiver(receiverTransport.open())
            val received = async { receiver.receive() }

            sender.send(expected)

            assertContentEquals(expected, received.await(), "Payload size: $size")
        }
    }

    @Test
    fun rejectsPayloadCorruptedInTransit() = runTest {
        val (senderTransport, receiverTransport) = MemoryTransport.createPair()
        val sender = ProtocolSender(CorruptingConnection(senderTransport.open()))
        val receiver = ProtocolReceiver(receiverTransport.open())

        sender.send(byteArrayOf(1, 2, 3))

        assertFailsWith<IntegrityVerificationException> { receiver.receive() }
    }
}
