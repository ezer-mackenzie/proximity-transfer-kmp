package io.github.ezer_mackenzie.proximitytransfer.core.transfer

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.IntegrityVerificationException
import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionState
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.RemoteErrorCode
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.control.RemoteTransferException
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.ConnectionClosedException
import io.github.ezer_mackenzie.proximitytransfer.core.transport.memory.MemoryTransport
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
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
            assertEquals(SessionState.COMPLETED, sender.session.state.value)
            assertEquals(SessionState.COMPLETED, receiver.session.state.value)
        }
    }

    @Test
    fun rejectsPayloadCorruptedInTransit() = runTest {
        val (senderTransport, receiverTransport) = MemoryTransport.createPair()
        val sender = ProtocolSender(CorruptingConnection(senderTransport.open()))
        val receiver = ProtocolReceiver(receiverTransport.open())
        val received = async { runCatching { receiver.receive() } }

        val exception = assertFailsWith<RemoteTransferException> {
            sender.send(byteArrayOf(1, 2, 3))
        }
        val receiverFailure = received.await().exceptionOrNull()

        assertEquals(RemoteErrorCode.INTEGRITY_FAILURE, exception.error.code)
        assertEquals(true, receiverFailure is IntegrityVerificationException)
        assertEquals(SessionState.FAILED, sender.session.state.value)
        assertEquals(SessionState.FAILED, receiver.session.state.value)
    }

    @Test
    fun senderFailsWhenReceiverDisconnects() = runTest {
        val (senderTransport, receiverTransport) = MemoryTransport.createPair()
        val sender = ProtocolSender(senderTransport.open())
        receiverTransport.open().close()

        assertFailsWith<ConnectionClosedException> {
            sender.send(byteArrayOf(1, 2, 3))
        }

        assertEquals(SessionState.FAILED, sender.session.state.value)
    }

    @Test
    fun receiverFailsWhenSenderDisconnects() = runTest {
        val (senderTransport, receiverTransport) = MemoryTransport.createPair()
        val senderConnection = senderTransport.open()
        val receiver = ProtocolReceiver(receiverTransport.open())
        val received = async { runCatching { receiver.receive() } }

        senderConnection.close()

        assertEquals(true, received.await().exceptionOrNull() is ConnectionClosedException)
        assertEquals(SessionState.FAILED, receiver.session.state.value)
    }
}
