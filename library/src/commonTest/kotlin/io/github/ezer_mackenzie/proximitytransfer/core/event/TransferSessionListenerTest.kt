package io.github.ezer_mackenzie.proximitytransfer.core.event

import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionState
import kotlin.test.Test
import kotlin.test.assertEquals

class TransferSessionListenerTest {

    @Test
    fun testCollectsEventsInOrder() {
        val listener = DefaultTransferSessionListener()
        val event1 = TransferSessionEvent.StateChanged("sess-1", SessionState.IDLE, SessionState.NEGOTIATING)
        val event2 = TransferSessionEvent.Completed("sess-1", 1024L, "abc123sha")

        listener.onEvent(event1)
        listener.onEvent(event2)

        val events = listener.getEvents()
        assertEquals(2, events.size)
        assertEquals(event1, events[0])
        assertEquals(event2, events[1])

        listener.clear()
        assertEquals(0, listener.getEvents().size)
    }
}
