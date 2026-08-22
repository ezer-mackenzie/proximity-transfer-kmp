package io.github.ezer_mackenzie.proximitytransfer.core.transfer.config

import kotlin.test.Test
import kotlin.test.assertFailsWith

class TransferLimitsTest {
    @Test
    fun rejectsInvalidLimits() {
        assertFailsWith<IllegalArgumentException> {
            TransferLimits(maxPayloadBytes = -1)
        }
        assertFailsWith<IllegalArgumentException> {
            TransferLimits(maxChunkCount = 0)
        }
    }
}
