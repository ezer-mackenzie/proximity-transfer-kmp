package io.github.ezer_mackenzie.proximitytransfer.core.transfer

import io.github.ezer_mackenzie.proximitytransfer.core.integrity.Sha256
import io.github.ezer_mackenzie.proximitytransfer.core.session.SessionState
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.chunk.PayloadChunk
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.resume.ChunkBitmap
import io.github.ezer_mackenzie.proximitytransfer.core.transfer.resume.ResumeRequest
import io.github.ezer_mackenzie.proximitytransfer.core.transport.memory.MemoryTransport
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ResumableTransferTest {

    @Test
    fun resumesInterruptedTransferWithMissingChunks() = runTest {
        val payload = ByteArray(50_000) { (it % 256).toByte() }
        val (transport1, transport2) = MemoryTransport.createPair()
        val senderConn = transport1.open()
        val receiverConn = transport2.open()

        // Create partial receiver state (received chunk 0 and 2, missing chunk 1 and 3)
        val totalChunks = 4
        val chunkSize = 12500
        val chunk0 = PayloadChunk(0, totalChunks, payload.copyOfRange(0, chunkSize))
        val chunk2 = PayloadChunk(2, totalChunks, payload.copyOfRange(25000, 37500))

        val bitmap = ChunkBitmap(totalChunks)
        bitmap.setReceived(0)
        bitmap.setReceived(2)

        val resumeRequest = ResumeRequest(
            sessionToken = byteArrayOf(1, 2, 3, 4),
            lastReceivedChunkIndex = 0,
            bitmap = bitmap,
        )

        val sender = ResumableProtocolSender(senderConn, chunkSize = chunkSize)
        val receiver = ResumableProtocolReceiver(receiverConn)

        val receiveJob = async {
            receiver.receive(
                existingChunks = listOf(chunk0, chunk2),
                existingBitmap = bitmap,
            )
        }

        val sendJob = async {
            sender.send(payload, resumeRequest = resumeRequest)
        }

        val (reconstructedBytes, _) = receiveJob.await()
        sendJob.await()

        assertEquals(payload.size, reconstructedBytes.size)
        assertEquals(Sha256.digest(payload).toList(), Sha256.digest(reconstructedBytes).toList())
        assertEquals(SessionState.COMPLETED, sender.session.state.value)
        assertEquals(SessionState.COMPLETED, receiver.session.state.value)
    }
}
