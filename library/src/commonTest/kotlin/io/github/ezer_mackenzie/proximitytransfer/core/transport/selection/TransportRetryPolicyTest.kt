package io.github.ezer_mackenzie.proximitytransfer.core.transport.selection

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TransportRetryPolicyTest {

    @Test
    fun testExecutesSuccessfullyOnFirstAttempt() = runTest {
        val policy = TransportRetryPolicy(maxAttempts = 3, initialDelayMs = 10)
        var attemptsCount = 0

        val result = policy.execute { attempt ->
            attemptsCount++
            "success-$attempt"
        }

        assertEquals("success-1", result)
        assertEquals(1, attemptsCount)
    }

    @Test
    fun testRetriesAndSucceedsOnLaterAttempt() = runTest {
        val policy = TransportRetryPolicy(maxAttempts = 3, initialDelayMs = 10)
        var attemptsCount = 0

        val result = policy.execute { attempt ->
            attemptsCount++
            if (attempt < 3) {
                throw IllegalStateException("Transient connection failure")
            }
            "success-$attempt"
        }

        assertEquals("success-3", result)
        assertEquals(3, attemptsCount)
    }

    @Test
    fun testFailsWhenMaxAttemptsExceeded() = runTest {
        val policy = TransportRetryPolicy(maxAttempts = 3, initialDelayMs = 10)
        var attemptsCount = 0

        assertFailsWith<IllegalStateException> {
            policy.execute {
                attemptsCount++
                throw IllegalStateException("Persistent failure")
            }
        }

        assertEquals(3, attemptsCount)
    }
}
