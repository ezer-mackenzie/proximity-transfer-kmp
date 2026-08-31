package io.github.ezer_mackenzie.proximitytransfer.core.engine

import io.github.ezer_mackenzie.proximitytransfer.core.event.TransferSessionListener
import io.github.ezer_mackenzie.proximitytransfer.core.session.InMemorySessionStore
import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionStore
import io.github.ezer_mackenzie.proximitytransfer.core.transport.selection.TransportRetryPolicy

/**
 * Fluent builder for configuring and constructing custom [ProximityTransferEngine] instances.
 */
class ProximityTransferClientBuilder {
    private var sessionStore: SessionStore = InMemorySessionStore()
    private var retryPolicy: TransportRetryPolicy = TransportRetryPolicy()
    private var listeners: MutableList<TransferSessionListener> = mutableListOf()

    fun withSessionStore(store: SessionStore): ProximityTransferClientBuilder = apply {
        this.sessionStore = store
    }

    fun withRetryPolicy(policy: TransportRetryPolicy): ProximityTransferClientBuilder = apply {
        this.retryPolicy = policy
    }

    fun addListener(listener: TransferSessionListener): ProximityTransferClientBuilder = apply {
        this.listeners.add(listener)
    }

    fun build(): ProximityTransferEngine {
        return DefaultProximityTransferEngine()
    }
}
