package io.github.ezer_mackenzie.proximitytransfer.core.integrity

import kotlin.test.Test
import kotlin.test.assertEquals

class Sha256Test {
    @Test
    fun matchesKnownEmptyPayloadVector() {
        val hexadecimal = Sha256.digest(byteArrayOf()).joinToString("") { byte ->
            byte.toUByte().toString(radix = 16).padStart(2, '0')
        }

        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            hexadecimal,
        )
    }
}
