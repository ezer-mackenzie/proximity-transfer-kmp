package io.github.ezer_mackenzie.proximitytransfer.core.transfer.batch

import io.github.ezer_mackenzie.proximitytransfer.core.engine.ProximityTransferEngine
import io.github.ezer_mackenzie.proximitytransfer.core.session.TransferSession
import io.github.ezer_mackenzie.proximitytransfer.core.transport.connection.Connection

/**
 * Controller for transmitting and receiving sequential batches of binary payloads over a single [Connection].
 */
class PayloadBatchTransfer(
    private val engine: ProximityTransferEngine = ProximityTransferEngine.create(),
) {
    /**
     * Transmits a list of [payloads] sequentially over [connection].
     */
    suspend fun sendBatch(
        connection: Connection,
        payloads: List<ByteArray>,
        onProgress: (completedIndex: Int, total: Int) -> Unit = { _, _ -> },
    ) {
        payloads.forEachIndexed { index, payload ->
            val session = TransferSession()
            engine.sendPayload(connection, payload, session)
            onProgress(index + 1, payloads.size)
        }
    }

    /**
     * Receives [count] sequential payloads over [connection].
     */
    suspend fun receiveBatch(
        connection: Connection,
        count: Int,
        onProgress: (completedIndex: Int, total: Int) -> Unit = { _, _ -> },
    ): List<ByteArray> {
        val result = mutableListOf<ByteArray>()
        repeat(count) { index ->
            val session = TransferSession()
            val (payload, _) = engine.receivePayload(connection, session)
            result.add(payload)
            onProgress(index + 1, count)
        }
        return result
    }
}
