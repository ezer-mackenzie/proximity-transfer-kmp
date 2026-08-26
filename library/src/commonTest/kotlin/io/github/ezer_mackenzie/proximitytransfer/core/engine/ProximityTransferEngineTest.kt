package io.github.ezer_mackenzie.proximitytransfer.core.engine

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256
import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionState
import io.github.ezer_mackenzie.proximitytransfer.core.transport.memory.MemoryTransport
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProximityTransferEngineTest {

    private val engine: ProximityTransferEngine = DefaultProximityTransferEngine()

    @Test
    fun formatsAndParsesQrBootstrapUri() {
        val payload = engine.createBootstrapPayload("peer-1", "192.168.1.50", 8080)
        val qrUri = engine.formatQrUri(payload)

        val parsedPayload = engine.parseQrUri(qrUri)
        assertEquals(payload.peerId, parsedPayload.peerId)
        assertEquals(payload.host, parsedPayload.host)
        assertEquals(payload.port, parsedPayload.port)
    }

    @Test
    fun formatsAndParsesNdefBytes() {
        val payload = engine.createBootstrapPayload("peer-2", "192.168.1.51", 8081)
        val ndefBytes = engine.formatNdefBytes(payload)

        val parsedPayload = engine.parseNdefBytes(ndefBytes)
        assertEquals(payload.peerId, parsedPayload.peerId)
        assertEquals(payload.host, parsedPayload.host)
        assertEquals(payload.port, parsedPayload.port)
    }

    @Test
    fun performsEndToEndPayloadTransferThroughEngine() = runTest {
        val samplePayload = "Hello Proximity Transfer Engine Facade!".encodeToByteArray()
        val (transport1, transport2) = MemoryTransport.createPair()

        val conn1 = transport1.open()
        val conn2 = transport2.open()

        val sendSession = io.github.ezer_mackenzie.proximitytransfer.core.session.TransferSession()
        val receiveSession = io.github.ezer_mackenzie.proximitytransfer.core.session.TransferSession()

        val receiveJob = async {
            engine.receivePayload(conn2, session = receiveSession)
        }

        val sendJob = async {
            engine.sendPayload(conn1, samplePayload, session = sendSession)
        }

        val (reconstructedBytes, _) = receiveJob.await()
        sendJob.await()

        assertEquals(samplePayload.size, reconstructedBytes.size)
        assertEquals(Sha256.digest(samplePayload).toList(), Sha256.digest(reconstructedBytes).toList())
        assertEquals(SessionState.COMPLETED, sendSession.state.value)
        assertEquals(SessionState.COMPLETED, receiveSession.state.value)
    }
}
