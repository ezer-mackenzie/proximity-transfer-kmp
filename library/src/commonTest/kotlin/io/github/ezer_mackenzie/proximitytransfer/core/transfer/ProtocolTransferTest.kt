package io.github.ezer_mackenzie.proximitytransfer.core.transfer

import io.github.ezer_mackenzie.proximitytransfer.core.transport.memory.MemoryTransport
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals

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
}
