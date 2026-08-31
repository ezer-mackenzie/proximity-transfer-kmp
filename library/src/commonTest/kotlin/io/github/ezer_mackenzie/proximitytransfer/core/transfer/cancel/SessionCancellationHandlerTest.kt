package io.github.ezer_mackenzie.proximitytransfer.core.transfer.cancel

import io.github.ezer_mackenzie.proximitytransfer.core.transport.memory.MemoryTransport
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionCancellationHandlerTest {

    @Test
    fun testSendAndReceiveCancellationSignal() = runTest {
        val (transport1, transport2) = MemoryTransport.createPair()
        val conn1 = transport1.open()
        val conn2 = transport2.open()

        val handler1 = SessionCancellationHandler()
        val handler2 = SessionCancellationHandler()

        val receiveJob = async { handler2.receiveCancellation(conn2) }
        val sendJob = async { handler1.sendCancellation(conn1, "sess-cancel-999") }

        sendJob.await()
        val cancelledToken = receiveJob.await()

        assertEquals("sess-cancel-999", cancelledToken)
    }
}
