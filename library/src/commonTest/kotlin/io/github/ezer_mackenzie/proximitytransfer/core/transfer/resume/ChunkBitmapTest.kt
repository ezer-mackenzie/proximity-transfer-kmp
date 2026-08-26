package io.github.ezer_mackenzie.proximitytransfer.core.transfer.resume

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChunkBitmapTest {

    @Test
    fun tracksChunkReceptionAndMissingIndices() {
        val bitmap = ChunkBitmap(totalChunks = 10)
        assertEquals((0 until 10).toList(), bitmap.missingChunkIndices())

        bitmap.setReceived(0)
        bitmap.setReceived(2)
        bitmap.setReceived(5)
        bitmap.setReceived(9)

        assertTrue(bitmap.isReceived(0))
        assertFalse(bitmap.isReceived(1))
        assertTrue(bitmap.isReceived(2))
        assertTrue(bitmap.isReceived(5))
        assertTrue(bitmap.isReceived(9))

        assertEquals(listOf(1, 3, 4, 6, 7, 8), bitmap.missingChunkIndices())
    }

    @Test
    fun encodeAndDecodeRoundTrip() {
        val original = ChunkBitmap(totalChunks = 16)
        original.setReceived(1)
        original.setReceived(7)
        original.setReceived(15)

        val bytes = original.encode()
        val decoded = ChunkBitmap.decode(16, bytes)

        assertEquals(original, decoded)
        assertEquals(original.missingChunkIndices(), decoded.missingChunkIndices())
    }

    @Test
    fun failsOnOutOfBoundsIndex() {
        val bitmap = ChunkBitmap(totalChunks = 5)
        assertFailsWith<IllegalArgumentException> {
            bitmap.setReceived(-1)
        }
        assertFailsWith<IllegalArgumentException> {
            bitmap.setReceived(5)
        }
    }
}
