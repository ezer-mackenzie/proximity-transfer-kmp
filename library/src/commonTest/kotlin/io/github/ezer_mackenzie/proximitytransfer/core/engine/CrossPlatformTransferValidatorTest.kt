package io.github.ezer_mackenzie.proximitytransfer.core.engine

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CrossPlatformTransferValidatorTest {

    @Test
    fun testValidatesIdenticalPayloads() {
        val input = "Cross-Platform Payload Validation".encodeToByteArray()
        val received = input.copyOf()

        assertTrue(CrossPlatformTransferValidator.validatePayloadIntegrity(input, received))
    }

    @Test
    fun testRejectsMismatchedSizePayloads() {
        val input = "Payload".encodeToByteArray()
        val received = "Payload Extra".encodeToByteArray()

        assertFalse(CrossPlatformTransferValidator.validatePayloadIntegrity(input, received))
    }

    @Test
    fun testRejectsCorruptedBytePayloads() {
        val input = byteArrayOf(1, 2, 3, 4, 5)
        val corrupted = byteArrayOf(1, 2, 99.toByte(), 4, 5)

        assertFalse(CrossPlatformTransferValidator.validatePayloadIntegrity(input, corrupted))
    }
}
