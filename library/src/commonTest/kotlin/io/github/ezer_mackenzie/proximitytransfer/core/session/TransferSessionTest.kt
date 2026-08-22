package io.github.ezer_mackenzie.proximitytransfer.core.session

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TransferSessionTest {
    @Test
    fun followsCompleteNegotiatedTransferLifecycle() = runTest {
        val session = TransferSession()

        session.transitionTo(SessionState.DISCOVERING)
        session.transitionTo(SessionState.NEGOTIATING)
        session.transitionTo(SessionState.CONNECTED)
        session.transitionTo(SessionState.TRANSFERRING)
        session.transitionTo(SessionState.VERIFYING)
        session.transitionTo(SessionState.COMPLETED)

        assertEquals(SessionState.COMPLETED, session.state.value)
    }

    @Test
    fun supportsAlreadyConnectedTransport() = runTest {
        val session = TransferSession()

        session.transitionTo(SessionState.CONNECTED)

        assertEquals(SessionState.CONNECTED, session.state.value)
    }

    @Test
    fun supportsNegotiationOverPreEstablishedBootstrapConnection() = runTest {
        val session = TransferSession()

        session.transitionTo(SessionState.NEGOTIATING)
        session.transitionTo(SessionState.CONNECTED)

        assertEquals(SessionState.CONNECTED, session.state.value)
    }

    @Test
    fun rejectsInvalidTransitionWithoutChangingState() = runTest {
        val session = TransferSession()

        val exception = assertFailsWith<InvalidSessionTransitionException> {
            session.transitionTo(SessionState.COMPLETED)
        }

        assertEquals(SessionState.IDLE, exception.from)
        assertEquals(SessionState.COMPLETED, exception.to)
        assertEquals(SessionState.IDLE, session.state.value)
    }

    @Test
    fun activeSessionCanFail() = runTest {
        val session = TransferSession()
        session.transitionTo(SessionState.CONNECTED)
        session.transitionTo(SessionState.TRANSFERRING)

        session.fail()

        assertEquals(SessionState.FAILED, session.state.value)
    }

    @Test
    fun terminalStateCannotTransition() = runTest {
        val session = TransferSession()
        session.fail()

        assertFailsWith<InvalidSessionTransitionException> {
            session.transitionTo(SessionState.IDLE)
        }
        assertFailsWith<InvalidSessionTransitionException> {
            session.fail()
        }
    }
}
