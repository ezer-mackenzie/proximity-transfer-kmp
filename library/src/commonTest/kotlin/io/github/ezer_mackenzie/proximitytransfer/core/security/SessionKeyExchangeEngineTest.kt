package io.github.ezer_mackenzie.proximitytransfer.core.security

import io.github.ezer_mackenzie.proximitytransfer.core.transport.memory.MemoryTransport
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

class SessionKeyExchangeEngineTest {

    @Test
    fun testPerformHandshakeDerivesMatchingKeys() = runTest {
        val (transport1, transport2) = MemoryTransport.createPair()
        val conn1 = transport1.open()
        val conn2 = transport2.open()

        val engine1 = SessionKeyExchangeEngine()
        val engine2 = SessionKeyExchangeEngine()

        val token = "session-token-test-123".encodeToByteArray()

        val job1 = async { engine1.performInitiatorHandshake(conn1, token) }
        val job2 = async { engine2.performResponderHandshake(conn2) }

        val keySpec1 = job1.await()
        val keySpec2 = job2.await()

        assertContentEquals(keySpec1.secretKey, keySpec2.secretKey)
    }
}
