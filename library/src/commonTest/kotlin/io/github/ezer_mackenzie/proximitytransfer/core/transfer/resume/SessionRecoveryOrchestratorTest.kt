package io.github.ezer_mackenzie.proximitytransfer.core.transfer.resume

import io.github.ezer_mackenzie.proximitytransfer.core.session.InMemorySessionStore
import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionSnapshot
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SessionRecoveryOrchestratorTest {

    @Test
    fun testOrchestrationFlow() = runTest {
        val store = InMemorySessionStore()
        val orchestrator = SessionRecoveryOrchestrator(store)

        assertNull(orchestrator.createResumeRequestIfAvailable("session-100"))

        val initialBitmap = ChunkBitmap(10)
        initialBitmap.setReceived(0)
        initialBitmap.setReceived(1)

        val snapshot = SessionSnapshot(
            sessionId = "session-100",
            payloadSha256Hex = "abcd",
            totalSize = 1000L,
            chunkSize = 100,
            bitmapBytes = initialBitmap.encode(),
        )

        orchestrator.saveCheckpoint(snapshot)

        val resumeRequest = orchestrator.createResumeRequestIfAvailable("session-100")
        assertNotNull(resumeRequest)
        assertEquals("session-100", resumeRequest.sessionToken.decodeToString())
        assertEquals(1, resumeRequest.lastReceivedChunkIndex)
        assertEquals(true, resumeRequest.bitmap.isReceived(0))
        assertEquals(true, resumeRequest.bitmap.isReceived(1))
        assertEquals(false, resumeRequest.bitmap.isReceived(2))

        orchestrator.clearCheckpoint("session-100")
        assertNull(orchestrator.createResumeRequestIfAvailable("session-100"))
    }
}
