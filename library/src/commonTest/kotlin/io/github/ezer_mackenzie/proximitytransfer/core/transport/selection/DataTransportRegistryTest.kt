package io.github.ezer_mackenzie.proximitytransfer.core.transport.selection

import io.github.ezer_mackenzie.proximitytransfer.core.capability.NegotiationResult
import io.github.ezer_mackenzie.proximitytransfer.core.capability.TransportCapability
import io.github.ezer_mackenzie.proximitytransfer.core.transport.memory.MemoryTransport
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DataTransportRegistryTest {
    @Test
    fun advertisesAndOpensRegisteredTransport() = runTest {
        val (localTransport, remoteTransport) = MemoryTransport.createPair()
        val registry = DataTransportRegistry(
            mapOf(TransportCapability.MEMORY to localTransport),
        )

        val local = registry.open(
            NegotiationResult(
                dataTransport = TransportCapability.MEMORY,
                bootstrapTransport = TransportCapability.MEMORY,
            ),
        )
        val remote = remoteTransport.open()
        local.send(byteArrayOf(1, 2, 3))

        assertEquals(setOf(TransportCapability.MEMORY), registry.capabilities.transports)
        assertContentEquals(byteArrayOf(1, 2, 3), remote.receive())
    }

    @Test
    fun failsWhenNegotiatedTransportIsNotRegistered() = runTest {
        val (transport, _) = MemoryTransport.createPair()
        val registry = DataTransportRegistry(mapOf(TransportCapability.MEMORY to transport))
        val negotiation = NegotiationResult(
            dataTransport = TransportCapability.BLE,
            bootstrapTransport = null,
        )

        val exception = assertFailsWith<MissingTransportImplementationException> {
            registry.open(negotiation)
        }

        assertEquals(TransportCapability.BLE, exception.capability)
    }

    @Test
    fun rejectsEmptyRegistry() {
        assertFailsWith<IllegalArgumentException> {
            DataTransportRegistry(emptyMap())
        }
    }

    @Test
    fun rejectsBootstrapOnlyTransportRegistration() {
        val (transport, _) = MemoryTransport.createPair()

        assertFailsWith<IllegalArgumentException> {
            DataTransportRegistry(mapOf(TransportCapability.QR_BOOTSTRAP to transport))
        }
    }
}
