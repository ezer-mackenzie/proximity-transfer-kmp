package dev.proximitytransfer.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProtocolVersionTest {
    @Test
    fun acceptsValuesRepresentableByOneUnsignedByte() {
        assertEquals(1, ProtocolVersion.of(1).value)
        assertEquals(255, ProtocolVersion.of(255).value)
    }

    @Test
    fun rejectsZeroAndValuesLargerThanOneUnsignedByte() {
        assertFailsWith<IllegalArgumentException> { ProtocolVersion.of(0) }
        assertFailsWith<IllegalArgumentException> { ProtocolVersion.of(256) }
    }
}
