package io.github.ezer_mackenzie.proximitytransfer.core.transfer

import io.github.ezer_mackenzie.proximitytransfer.core.engine.ProximityTransferEngine
import io.github.ezer_mackenzie.proximitytransfer.core.security.SessionKeyExchangeEngine
import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionState
import io.github.ezer_mackenzie.proximitytransfer.core.session.TransferSession
import io.github.ezer_mackenzie.proximitytransfer.core.transport.memory.MemoryTransport
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class EncryptedProtocolTransferTest {

    @Test
    fun testHandshakeAndEncryptedProtocolTransfer() = runTest {
        val (transport1, transport2) = MemoryTransport.createPair()
        val conn1 = transport1.open()
        val conn2 = transport2.open()

        val engine1 = SessionKeyExchangeEngine()
        val engine2 = SessionKeyExchangeEngine()
        val token = "test-session-token-xyz".encodeToByteArray()

        val keyJob1 = async { engine1.performInitiatorHandshake(conn1, token) }
        val keyJob2 = async { engine2.performResponderHandshake(conn2) }

        val keySpec1 = keyJob1.await()
        val keySpec2 = keyJob2.await()

        assertContentEquals(keySpec1.secretKey, keySpec2.secretKey)

        val payload = Random.nextBytes(4096)
        val sender = ProtocolSender(conn1, keySpec = keySpec1)
        val receiver = ProtocolReceiver(conn2, keySpec = keySpec2)

        val receiveJob = async { receiver.receive() }
        val sendJob = async { sender.send(payload) }

        val receivedPayload = receiveJob.await()
        sendJob.await()

        assertContentEquals(payload, receivedPayload)
        assertEquals(SessionState.COMPLETED, sender.session.state.value)
        assertEquals(SessionState.COMPLETED, receiver.session.state.value)
    }

    @Test
    fun testEngineEncryptedSendAndReceive() = runTest {
        val (transport1, transport2) = MemoryTransport.createPair()
        val conn1 = transport1.open()
        val conn2 = transport2.open()

        val keyEngine1 = SessionKeyExchangeEngine()
        val keyEngine2 = SessionKeyExchangeEngine()
        val token = "engine-encrypted-token".encodeToByteArray()

        val keyJob1 = async { keyEngine1.performInitiatorHandshake(conn1, token) }
        val keyJob2 = async { keyEngine2.performResponderHandshake(conn2) }

        val keySpec1 = keyJob1.await()
        val keySpec2 = keyJob2.await()

        val engineSender = ProximityTransferEngine.create()
        val engineReceiver = ProximityTransferEngine.create()

        val payload = "Hello Encrypted Proximity Transfer!".encodeToByteArray()

        val senderSession = TransferSession()
        val receiverSession = TransferSession()

        val receiveJob = async {
            engineReceiver.receivePayload(conn2, session = receiverSession, keySpec = keySpec2)
        }

        val sendJob = async {
            engineSender.sendPayload(conn1, payload, session = senderSession, keySpec = keySpec1)
        }

        val (receivedPayload, _) = receiveJob.await()
        sendJob.await()

        assertContentEquals(payload, receivedPayload)
        assertEquals(SessionState.COMPLETED, senderSession.state.value)
        assertEquals(SessionState.COMPLETED, receiverSession.state.value)
    }
}
