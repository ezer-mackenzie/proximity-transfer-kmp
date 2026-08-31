package io.github.ezer_mackenzie.proximitytransfer.core.engine

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256
import io.github.ezer_mackenzie.proximitytransfer.core.session.TransferSession
import io.github.ezer_mackenzie.proximitytransfer.core.transport.memory.MemoryTransport
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ProximityTransferV1IntegrationTest {

    @Test
    fun testV1IntegrationPayloads1KB() = runIntegrationTransfer(1024)

    @Test
    fun testV1IntegrationPayloads64KB() = runIntegrationTransfer(64 * 1024)

    @Test
    fun testV1IntegrationPayloads512KB() = runIntegrationTransfer(512 * 1024)

    private fun runIntegrationTransfer(payloadSize: Int) = runTest {
        val (transport1, transport2) = MemoryTransport.createPair()
        val conn1 = transport1.open()
        val conn2 = transport2.open()

        val senderEngine = ProximityTransferEngine.create()
        val receiverEngine = ProximityTransferEngine.create()

        val inputPayload = Random.nextBytes(payloadSize)
        val senderSession = TransferSession()
        val receiverSession = TransferSession()

        val receiveJob = async {
            receiverEngine.receivePayload(conn2, receiverSession)
        }

        val sendJob = async {
            senderEngine.sendPayload(conn1, inputPayload, senderSession)
        }

        val (receivedPayload, bitmap) = receiveJob.await()
        sendJob.await()

        assertNotNull(bitmap)
        assertEquals(inputPayload.size, receivedPayload.size)

        val inputSha = Sha256.digest(inputPayload)
        val outputSha = Sha256.digest(receivedPayload)
        assertContentEquals(inputSha, outputSha)
    }
}
