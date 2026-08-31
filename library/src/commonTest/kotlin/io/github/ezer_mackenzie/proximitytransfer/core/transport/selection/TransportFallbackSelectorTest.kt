package io.github.ezer_mackenzie.proximitytransfer.core.transport.selection

import io.github.ezer_mackenzie.proximitytransfer.core.capability.model.TransportCapability
import io.github.ezer_mackenzie.proximitytransfer.core.transport.Transport
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection
import io.github.ezer_mackenzie.proximitytransfer.core.transport.memory.MemoryTransport
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class TransportFallbackSelectorTest {

    private class FailingTransport : Transport {
        override suspend fun open(): Connection {
            throw IllegalStateException("Transport unavailable")
        }
    }

    @Test
    fun testSelectsHighestPriorityAvailableTransport() = runTest {
        val (mem1, _) = MemoryTransport.createPair()
        val selector = TransportFallbackSelector()

        val map = mapOf(
            TransportCapability.BLE to FailingTransport(),
            TransportCapability.MEMORY to mem1,
        )

        val conn = selector.openFirstAvailable(map)
        assertNotNull(conn)
    }

    @Test
    fun testThrowsExceptionWhenAllTransportsFail() = runTest {
        val selector = TransportFallbackSelector()
        val map = mapOf(
            TransportCapability.BLE to FailingTransport(),
            TransportCapability.NFC_BOOTSTRAP to FailingTransport(),
        )

        assertFailsWith<IllegalStateException> {
            selector.openFirstAvailable(map)
        }
    }
}
