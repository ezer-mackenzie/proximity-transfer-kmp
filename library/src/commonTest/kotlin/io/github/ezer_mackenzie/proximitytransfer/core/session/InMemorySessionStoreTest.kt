package io.github.ezer_mackenzie.proximitytransfer.core.session

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InMemorySessionStoreTest {

    @Test
    fun testSaveGetAndClearSnapshot() = runTest {
        val store: SessionStore = InMemorySessionStore()
        val snapshot = SessionSnapshot(
            sessionId = "sess-001",
            payloadSha256Hex = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            totalSize = 1048576L,
            chunkSize = 16384,
            bitmapBytes = byteArrayOf(0xFF.toByte(), 0x01.toByte()),
        )

        assertNull(store.getSnapshot("sess-001"))

        store.saveSnapshot(snapshot)
        assertEquals(snapshot, store.getSnapshot("sess-001"))

        store.clearSnapshot("sess-001")
        assertNull(store.getSnapshot("sess-001"))
    }
}
