package io.github.ezer_mackenzie.proximitytransfer.core.event

/**
 * Default collecting implementation of [TransferSessionListener] that stores emitted events in memory.
 */
class DefaultTransferSessionListener : TransferSessionListener {
    private val eventsList = mutableListOf<TransferSessionEvent>()

    /** Returns an unmodifiable list of captured events. */
    fun getEvents(): List<TransferSessionEvent> = eventsList.toList()

    override fun onEvent(event: TransferSessionEvent) {
        eventsList.add(event)
    }

    /** Clears all recorded events. */
    fun clear() {
        eventsList.clear()
    }
}
