package io.github.ezer_mackenzie.proximitytransfer.core.transfer.batch

import io.github.ezer_mackenzie.proximitytransfer.core.transport.memory.MemoryTransport
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class PayloadBatchTransferTest {

    @Test
    fun testSendAndReceivePayloadBatch() = runTest {
        val (transport1, transport2) = MemoryTransport.createPair()
        val conn1 = transport1.open()
        val conn2 = transport2.open()

        val batchTransfer1 = PayloadBatchTransfer()
        val batchTransfer2 = PayloadBatchTransfer()

        val samplePayloads = listOf(
            "Batch Item 1".encodeToByteArray(),
            "Batch Item 2 (larger content)".encodeToByteArray(),
            "Batch Item 3".encodeToByteArray(),
        )

        var sendProgressCount = 0
        var receiveProgressCount = 0

        val receiveJob = async {
            batchTransfer2.receiveBatch(conn2, samplePayloads.size) { completed, total ->
                receiveProgressCount = completed
            }
        }

        val sendJob = async {
            batchTransfer1.sendBatch(conn1, samplePayloads) { completed, total ->
                sendProgressCount = completed
            }
        }

        val receivedList = receiveJob.await()
        sendJob.await()

        assertEquals(3, receivedList.size)
        assertEquals(3, sendProgressCount)
        assertEquals(3, receiveProgressCount)

        assertContentEquals(samplePayloads[0], receivedList[0])
        assertContentEquals(samplePayloads[1], receivedList[1])
        assertContentEquals(samplePayloads[2], receivedList[2])
    }
}
