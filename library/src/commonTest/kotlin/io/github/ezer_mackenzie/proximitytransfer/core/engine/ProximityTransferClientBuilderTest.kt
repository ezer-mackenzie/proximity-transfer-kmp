package io.github.ezer_mackenzie.proximitytransfer.core.engine

import io.github.ezer_mackenzie.proximitytransfer.core.event.DefaultTransferSessionListener
import io.github.ezer_mackenzie.proximitytransfer.core.session.InMemorySessionStore
import io.github.ezer_mackenzie.proximitytransfer.core.transport.selection.TransportRetryPolicy
import kotlin.test.Test
import kotlin.test.assertNotNull

class ProximityTransferClientBuilderTest {

    @Test
    fun testBuildsEngineWithCustomConfigurations() {
        val listener = DefaultTransferSessionListener()
        val store = InMemorySessionStore()
        val retryPolicy = TransportRetryPolicy(maxAttempts = 5)

        val engine = ProximityTransferClientBuilder()
            .withSessionStore(store)
            .withRetryPolicy(retryPolicy)
            .addListener(listener)
            .build()

        assertNotNull(engine)
    }
}
