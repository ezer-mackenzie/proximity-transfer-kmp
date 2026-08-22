package io.github.ezer_mackenzie.proximitytransfer.core.transfer.progress

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TransferProgressTest {
    @Test
    fun calculatesFractionAndCompletion() {
        val progress = TransferProgress(transferredBytes = 25, totalBytes = 100)

        assertEquals(0.25, progress.fraction)
        assertEquals(false, progress.isComplete)
    }

    @Test
    fun treatsEmptyPayloadAsComplete() {
        val progress = TransferProgress(transferredBytes = 0, totalBytes = 0)

        assertEquals(1.0, progress.fraction)
        assertTrue(progress.isComplete)
    }

    @Test
    fun rejectsInvalidByteCounts() {
        assertFailsWith<IllegalArgumentException> {
            TransferProgress(transferredBytes = -1, totalBytes = 1)
        }
        assertFailsWith<IllegalArgumentException> {
            TransferProgress(transferredBytes = 2, totalBytes = 1)
        }
        assertFailsWith<IllegalArgumentException> {
            TransferProgress(transferredBytes = 0, totalBytes = -1)
        }
    }
}
